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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;

@Service
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

    @Value("${fcm.send-url}")
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
        String publisherName = userMapper.getByUserId(timelineDTO.getUserId()).getName();
        // 存图片到S3
        for (MultipartFile file : timelineDTO.getFiles()) {
            commonService.uploadToS3(file, file.getName(), "timeline/" + postId);
        }
        // 拿到s3的图片url,更新mysql
        List<String> imgUrls = commonService.getS3ImgUrls("timeline/" + postId);
        timelineMapper.updateTimelineImgs(imgUrls);
        // 写入redis timelinecontent
        String key = "timeline:post:" + postId;
        Map<String, String> timelineMap = new HashMap<>();
        timelineMap.put("postId", postId.toString());
        timelineMap.put("userId", timelineDTO.getUserId().toString());
        timelineMap.put("context", timelineDTO.getContext());
        timelineMap.put("imgUrls", JSON.toJSONString(imgUrls));
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
            // 推送一份分片
            kafkaTemplate.send(
                    "timeline_push",
                    JSON.toJSONString(Map.of(
                            "postId", postId,
                            "fanIds", batch,
                            "publisherName", publisherName,
                            "content", timelineDTO.getContext())));
        }
    }

    // 推送给粉丝的消费者
    @Override
    @KafkaListener(topics = "timeline_push", groupId = "timeline-group")
    public void onTimelinePush(TimelinePushEvent event) throws IOException {
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

    public void sendPushToUser(String fanId, String publisherName, Long postId, String content) throws IOException {
        String title = publisherName + "posted a new update";
        String body = content;

        // 查询用户的 push token
        String token = redis.opsForValue().get("push_token:" + fanId);
        if (token == null) {
            return;
        }

        // iOS
        // if (token.startsWith("ios:")) {
        // sendAPNsPush(token.substring(4), title, body);
        // }
        // Android
        if (token.startsWith("android:")) {
            sendFCMPush(token.substring(8), title, body, content);
        }
    }

    public void sendAPNsPush(String deviceToken, String title, String body) {
        //
    }

    public void sendFCMPush(String deviceToken, String title, String body, String content) throws IOException {
        OkHttpClient client = new OkHttpClient();

        JSONObject message = new JSONObject();
        message.put("to", deviceToken);

        JSONObject notification = new JSONObject();
        notification.put("title", title);
        notification.put("body", body);

        message.put("notification", notification);

        RequestBody requestBody = RequestBody.create(
                message.toString(),
                MediaType.parse("application/json; charset=utf-8"));

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(fcmUrl)
                .addHeader("Authorization", "key=" + serverKey)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        System.out.println("FCM Response: " + response.body().string());
    }
}
