package gwl.service.Impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.DocFlavor.STRING;
import javax.print.attribute.standard.MediaTray;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import gwl.components.ChannelManager;
import gwl.context.BaseContext;
import gwl.entity.TimelineContent;
import gwl.entity.User;
import gwl.entity.event.TimelinePublishEvent;
import gwl.entity.event.TimelinePushEvent;
import gwl.mapper.TimelineMapper;
import gwl.mapper.UserMapper;
import gwl.pojo.DTO.TimelineDTO;
import gwl.service.CommonService;
import gwl.service.TimelineService;
import gwl.service.UserService;
import io.micrometer.core.ipc.http.HttpSender.Request;
import io.micrometer.observation.Observation.CheckedRunnable;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;

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
                .createTime(timelineDTO.getCreateTime())
                .build();
        Long postId = timelineMapper.postTimeline(timelineContent);
        String publisherName = userMapper.getByUserId(timelineDTO.getUserId()).getUsername();
        String key = "timeline:post:" + postId;
        Map<String, String> timelineMap = new HashMap<>();
        // 存图片到S3
        if (timelineDTO.getFiles() != null) {
            for (MultipartFile file : timelineDTO.getFiles()) {
                commonService.uploadToS3(file, file.getName(), "timeline/" + postId);
            }
            // 拿到s3的图片url,更新mysql
            List<String> imgUrls = commonService.getS3ImgUrls("timeline/" + postId);
            timelineMapper.updateTimelineImgs(imgUrls);
            timelineMap.put("imgUrls", JSON.toJSONString(imgUrls));
        }
        // 写入redis timelinecontent
        timelineMap.put("postId", postId.toString());
        timelineMap.put("userId", timelineDTO.getUserId().toString());
        timelineMap.put("context", timelineDTO.getContext());
        timelineMap.put("createTime", timelineDTO.getCreateTime());
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
                    .content(timelineDTO.getContext()).build();
            kafkaTemplate.send(
                    "timeline_publish",
                    timelinePushEvent);
            log.info("推送消息");
            // JSON.toJSONString(Map.of(
            // "postId", postId,
            // "fanIds", batch,
            // "publisherName", publisherName,
            // "content", timelineDTO.getContext())));
        }
    }

    // 推送给粉丝的消费者
    @Override
    @KafkaListener(topics = "timeline_publish", groupId = "timeline-group")
    public void onTimelinePush(@Payload TimelinePushEvent event) throws IOException, FirebaseMessagingException {
        log.info("消费消息");
        // 推送给多个好友
        Long postId = event.getPostId();
        List<String> fanIds = event.getFanIds();
        String publisherName = event.getPublisherName();
        String content = event.getContent();

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

    public void sendAPNsPush(String deviceToken, String title, String body) {
        //
    }

    public void sendFCMPush(String deviceToken, String title, String content)
            throws IOException, FirebaseMessagingException {
        // OkHttpClient client = new OkHttpClient();

        // JSONObject message = new JSONObject();
        // message.put("to", deviceToken);

        // JSONObject notification = new JSONObject();
        // notification.put("title", title);
        // notification.put("body", body);

        // message.put("notification", notification);

        // RequestBody requestBody = RequestBody.create(
        // message.toString(),
        // MediaType.parse("application/json; charset=utf-8"));

        // okhttp3.Request request = new okhttp3.Request.Builder()
        // .url(fcmUrl)
        // .addHeader("Authorization", "key=" + serverKey)
        // .post(requestBody)
        // .build();
        Message message = Message.builder()
                .setToken(deviceToken)
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
