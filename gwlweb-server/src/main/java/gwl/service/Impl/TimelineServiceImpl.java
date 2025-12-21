package gwl.service.Impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import gwl.components.ChannelManager;
import gwl.constant.AWSConstant;
import gwl.context.BaseContext;
import gwl.entity.TimelineContent;
import gwl.entity.User;
import gwl.entity.event.TimelineLikeHitEvent;
import gwl.entity.event.TimelinePushEvent;
import gwl.entity.timeline.TimelineComment;
import gwl.entity.timeline.TimelineUserLike;
import gwl.exception.BaseException;
import gwl.mapper.TimelineMapper;
import gwl.mapper.UserMapper;
import gwl.pojo.DTO.PostCommentDTO;
import gwl.pojo.DTO.TimelineDTO;
import gwl.pojo.VO.LikeUserVO;
import gwl.pojo.VO.TimelineVO;
import gwl.service.CommonService;
import gwl.service.TimelineService;
import gwl.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TimelineServiceImpl implements TimelineService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TimelineMapper timelineMapper;
    @Autowired
    private ChannelManager channelManager;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private CommonService commonService;
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private AWSConstant aws;
    @Value("${fcm.server-key}")
    private String serverKey;

    @Value("${fcm.url}")
    private String fcmUrl;

    // @Autowired
    // private StringRedis redis;

    /**
     * 推送帖子
     * 
     * @throws IOException
     */
    @Override
    public void postTimeline(TimelineDTO timelineDTO) throws IOException {
        // 添加到数据库
        TimelineContent timelineContent = TimelineContent.builder()
                .userId(timelineDTO.getUserId())
                .context(timelineDTO.getContext())
                .imgUrls(null)
                .build();
        timelineMapper.postTimeline(timelineContent);
        Long postId = timelineContent.getId();
        TimelineVO timelineVO = timelineMapper.getTimelineContent(postId, BaseContext.getCurrentId());
        // mysql操作
        // timeline里先给自己加记录
        timelineMapper.addTimeline(BaseContext.getCurrentId(), postId, timelineVO.getCreatedAt());

        String publisherName = userMapper.getByUserId(timelineDTO.getUserId()).getUsername();
        String key = "timeline:post:" + postId;
        Map<String, String> timelineMap = new HashMap<>();
        List<String> imgUrls = new ArrayList<>();
        // 存图片到S3
        if (timelineDTO.getFiles() != null) {
            for (MultipartFile file : timelineDTO.getFiles()) {
                String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                String randomId = UUID.randomUUID().toString();
                String imgUrl = aws.getUrl() + "timeline/" + postId + "/" + randomId + extension;
                String uploadKey = "timeline/" + postId + "/" + randomId + extension;
                commonService.uploadToS3(file, uploadKey);
                imgUrls.add(imgUrl);
            }
            // 更新mysql的img_urls
            timelineMapper.updateTimelineImgs(imgUrls, postId);
            timelineMap.put("imgUrls", JSON.toJSONString(imgUrls));
        }
        // 写入redis timelinecontent
        timelineMap.put("postId", postId.toString());
        timelineMap.put("userId", timelineDTO.getUserId().toString());
        timelineMap.put("userName", publisherName);
        timelineMap.put("context", timelineDTO.getContext());
        timelineMap.put("createdAt", timelineVO.getCreatedAt());

        redis.opsForHash().putAll(key, timelineMap);

        // 获取好友列表 推送kafka
        List<User> friends = userMapper.getFriendListByUserId(BaseContext.getCurrentId());
        List<Long> friendIds = new ArrayList<>();
        for (User f : friends) {
            friendIds.add(f.getId());
        }
        int batchSize = 1000;

        for (int i = 0; i < friendIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, friendIds.size());
            List<String> batch = friendIds.subList(i, end).stream().map(String::valueOf).toList();
            // log.info("check gwl null {},{},{},{}", postId, batch.get(0), publisherName,
            // timelineDTO.getContext());
            // 推送一份分片
            TimelinePushEvent timelinePushEvent = TimelinePushEvent.builder()
                    .postId(postId)
                    .fanIds(batch)
                    .publisherName(publisherName)
                    .createdAt(timelineVO.getCreatedAt())
                    .content(timelineDTO.getContext()).build();

            kafkaTemplate.send(
                    "timeline_publish",
                    timelinePushEvent);
            log.info("推送消息");
        }
    }

    // 推送给粉丝的消费者
    @Override
    @KafkaListener(topics = "timeline_publish", groupId = "timeline-group")
    public void onTimelinePush(@Payload TimelinePushEvent event) throws IOException, FirebaseMessagingException {
        // 推送给多个好友
        Long postId = event.getPostId();
        List<String> fanIds = event.getFanIds();
        String publisherName = event.getPublisherName();
        String content = event.getContent();
        String createdAt = event.getCreatedAt();

        if (fanIds == null || fanIds.isEmpty()) {
            return;
        }

        // 将帖子推送到每个粉丝的 Redis 时间线
        for (String fanId : fanIds) {
            String key = "timeline:user:" + fanId;

            // 将 postId 放到用户时间线头部
            redis.opsForList().leftPush(key, postId.toString());

            // 控制时间线长度，比如最多 1000 条
            redis.opsForList().trim(key, 0, 999);

            // 数据库更新user的timeline表
            timelineMapper.addTimeline(Long.valueOf(fanId), postId, createdAt);

            // 发送 iOS / Android 推送通知
            sendPushToUser(fanId, publisherName, postId, content);

        }
    }

    public void sendPushToUser(String fanId, String publisherName, Long postId, String content)
            throws IOException, FirebaseMessagingException {
        String title = publisherName + "posted a new update";

        // 查询用户的 push token
        String token = redis.opsForValue().get("push_token:" + fanId);
        if (token == null) {
            log.info("查不到该用户devicetoken,return");
            return;
        } else {
            log.info("推送给用户:{}", fanId);

        }

        // iOS
        // if (token.startsWith("ios:")) {
        // sendAPNsPush(token.substring(4), title, body);
        // }
        // Android
        if (token.startsWith("android:")) {
            sendFCMPush(token.substring(8), title, content);
        }
    }

    /**
     * 获取帖子(刷新)
     */
    @Override
    public List<TimelineVO> getTimelinePost() {
        List<TimelineVO> timelineVOs = new ArrayList<>();
        List<Long> timelineIds = timelineMapper.getTimeline(BaseContext.getCurrentId());
        for (Long timelineId : timelineIds) {
            // redis里有数据就取redis的
            if (redis.hasKey("timeline:post:" + timelineId)) {
                Map<Object, Object> map = redis.opsForHash().entries("timeline:post:" + timelineId);
                log.info("{timelineMap=}", map);
                String userName = map.get("userName").toString();
                String context = map.get("context").toString();
                Object imgUrlsObj = map.get("imgUrls");
                List<String> imgUrls = imgUrlsObj == null
                        ? Collections.emptyList()
                        : JSON.parseArray(imgUrlsObj.toString(), String.class);
                String createdAt = map.get("createdAt").toString();
                String totalLikeStr = redis.opsForValue().get("timeline:like:totalcount:" + timelineId);
                Integer totalLikedCount = totalLikeStr == null ? 0 : Integer.parseInt(totalLikeStr);
                Map<Long, Integer> userLikeMap = redis.opsForHash().entries("timeline:like:user:" + timelineId)
                        .entrySet()
                        .stream()
                        // 按 value 倒序
                        .sorted((e1, e2) -> {
                            int v1 = Integer.parseInt(e1.getValue().toString());
                            int v2 = Integer.parseInt(e2.getValue().toString());
                            return Integer.compare(v2, v1);
                        })
                        // 只取前 20
                        .limit(20)
                        // 收集成 Map
                        .collect(Collectors.toMap(
                                e -> Long.parseLong(e.getKey().toString()),
                                e -> Integer.parseInt(e.getValue().toString()),
                                (a, b) -> a,
                                LinkedHashMap::new // 保持排序后的顺序
                        ));
                List<LikeUserVO> likeUserVOs = new ArrayList<>();
                // 用户点赞数据
                Integer likedByMeCount = 0;
                for (Map.Entry<Long, Integer> entry : userLikeMap.entrySet()) {
                    Long userId = entry.getKey();
                    Integer likeCount = entry.getValue();
                    LikeUserVO likeUserVO = LikeUserVO.builder()
                            .userId(userId)
                            .avatarUrl(redis.opsForValue().get("useravatarurl:" + userId))
                            .userLikeCount(likeCount)
                            .build();
                    if (userId == BaseContext.getCurrentId()) {
                        likedByMeCount = likeCount;
                    }
                    likeUserVOs.add(likeUserVO);
                }
                // 用户评论数据
                List<TimelineComment> timelineComments = timelineMapper.getComments(timelineId);

                TimelineVO timelineVO = TimelineVO.builder()
                        .timelineId(timelineId)
                        .topLikeUsers(likeUserVOs)
                        .userName(userName)
                        .context(context)
                        .imgUrls(imgUrls)
                        .createdAt(createdAt)
                        .likedByMeCount(likedByMeCount)
                        .totalLikeCount(totalLikedCount)
                        .comments(timelineComments)
                        .build();
                timelineVOs.add(timelineVO);
            } else {
                log.info("没有取到redis里的timeline数据，查询mysql");
                timelineVOs.add(timelineMapper.getTimelineContent(timelineId, BaseContext.getCurrentId()));
            }
        }
        return timelineVOs;
    }

    /**
     * 给帖子点赞
     */
    @Override
    public void likeHit(Long timelineId) {
        Long userId = BaseContext.getCurrentId();
        // 帖子总点赞数 +1
        Long totalLikeCount = redis.opsForValue()
                .increment("timeline:like:totalcount:" + timelineId);

        // 用户对该帖的点赞数 +1
        redis.opsForHash()
                .increment("timeline:like:user:" + timelineId,
                        userId.toString(),
                        1);

        // 标记为 dirty（发生过变化）
        redis.opsForSet()
                .add("dirty:timeline:set", timelineId.toString());

        // 触发阈值刷（例如 300）
        if (totalLikeCount != null && totalLikeCount % 300 == 0) {
            TimelineUserLike timelineUserLike = TimelineUserLike.builder()
                    .timelineId(timelineId)
                    .userLikeCount(redis.opsForHash()
                            .entries("timeline:like:user:" + timelineId)
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    e -> Long.valueOf(e.getKey().toString()),
                                    e -> Integer.valueOf(e.getValue().toString()))))
                    .build();
            timelineMapper.flushLikeToDB(timelineUserLike);
            // 已刷库，移出 dirty
            redis.opsForSet()
                    .remove("dirty:timeline:set", timelineId);
        }
    }

    /**
     * 给帖子评论
     */
    @Override
    public void postComment(PostCommentDTO postCommentDTO) {
        String comment = postCommentDTO.getComment();
        Long userId = BaseContext.getCurrentId();
        Long timelineId = postCommentDTO.getTimelineId();

        TimelineComment timelineComment = TimelineComment.builder()
                .comment(comment)
                .userId(userId)
                .timelineId(timelineId).build();

        // mysql save
        timelineMapper.postComment(timelineComment);

        // redis save
        String key = "timelinecomment:" + postCommentDTO.getTimelineId();
        Map<String, Object> value = new HashMap<>();
        value.put("userId", userId);
        value.put("comment", comment);
        value.put("createdAt", LocalDateTime.now());
        try {
            redis.opsForList().leftPush(
                    key,
                    CommonUtil.mapper.writeValueAsString(value));
        } catch (Exception e) {
            log.error("发帖子评论存redis失败", e);
            throw new BaseException("发帖子评论存redis失败");
        }

    }

    /**
     * kafka点赞消费者
     */
    @Override
    @KafkaListener(topics = "timeline_likehit", groupId = "timeline-group")
    public void onLikeHit(@Payload TimelineLikeHitEvent event) {
        Long timelineId = event.getTimelineId();
        Long userId = event.getUserId();

        // 帖子总点赞数 +1
        Long totalLikeCount = redis.opsForValue()
                .increment("timeline:like:totalcount:" + timelineId);

        // 用户对该帖的点赞数 +1
        redis.opsForHash()
                .increment("timeline:like:user:" + timelineId,
                        userId.toString(),
                        1);

        // 标记为 dirty（发生过变化）
        redis.opsForSet()
                .add("dirty:timeline:set", timelineId.toString());

        // 触发阈值刷（例如 300）
        if (totalLikeCount != null && totalLikeCount % 300 == 0) {
            TimelineUserLike timelineUserLike = TimelineUserLike.builder()
                    .timelineId(timelineId)
                    .userLikeCount(redis.opsForHash()
                            .entries("timeline:like:user:" + timelineId)
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    e -> Long.valueOf(e.getKey().toString()),
                                    e -> Integer.valueOf(e.getValue().toString()))))
                    .build();
            timelineMapper.flushLikeToDB(timelineUserLike);
            // 已刷库，移出 dirty
            redis.opsForSet()
                    .remove("dirty:timeline:set", timelineId);
        }
    }

    // 每分钟刷盘点赞的脏数据
    @Scheduled(fixedDelayString = "60s")
    @Override
    public void flushTimelineLikeToMySQL() {
        Set<Long> dirtyTimelineIds = redis.opsForSet()
                .members("dirty:timeline:set").stream().map(Long::valueOf).collect(Collectors.toSet());
        for (Long dirtyTimeline : dirtyTimelineIds) {
            TimelineUserLike timelineUserLike = TimelineUserLike.builder()
                    .timelineId(dirtyTimeline)
                    .userLikeCount(redis.opsForHash()
                            .entries("timeline:like:user:" + dirtyTimeline)
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    e -> Long.valueOf(e.getKey().toString()),
                                    e -> Integer.valueOf(e.getValue().toString()))))
                    .build();

            timelineMapper.flushLikeToDB(timelineUserLike);
            // 清除脏数据
            redis.opsForSet()
                    .remove("dirty:timeline:set", dirtyTimeline);
        }
    }

    /**
     * android推送
     * 
     * @param deviceToken
     * @param title
     * @param content
     * @throws IOException
     * @throws FirebaseMessagingException
     */
    public void sendFCMPush(String deviceToken, String title, String content)
            throws IOException, FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(deviceToken)
                .putData("type", "gettimeline")
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(content)
                                .build())
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("FCM Response: " + response);
    }
}
