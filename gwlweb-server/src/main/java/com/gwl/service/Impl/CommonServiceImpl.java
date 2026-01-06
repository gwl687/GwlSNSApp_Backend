package com.gwl.service.Impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.gwl.properties.AwsProperties;
import com.gwl.context.BaseContext;
import com.gwl.mapper.UserMapper;
import com.gwl.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {
    @Autowired
    private S3Client s3;
    @Autowired
    private AwsProperties aws;
    @Autowired
    private StringRedisTemplate templateRedis;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Boolean uploadToS3(MultipartFile file, String key) {
        // 构建上传请求
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(aws.getBucket()) //
                .key(key)
                .contentType(file.getContentType())
                .acl("public-read") // 让头像可直接访问
                .build();

        // 上传文件
        try {
            s3.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (Exception e) {
            log.error("上传到s3失败:", e);
        }
        // 拼接公开URL
        String avartarUrl = aws.getUrl() + key;
        // 如果上传的是头像，存redis
        if (key.startsWith("avatar/")) {
            System.out.println("上传头像: " + key);
            templateRedis.opsForValue().set("useravatarurl:" + BaseContext.getCurrentId(), avartarUrl);
        }
        userMapper.updateAvatarUrl(BaseContext.getCurrentId(), aws.getUrl() + key);
        System.out.println("上传成功: " + key);
        return true;
    }

    /**
     * 获取s3图片urls
     * 
     * @return
     */
    public List<String> getS3ImgUrls(String path) {
        List<String> urls = new ArrayList<>();

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(aws.getBucket())
                .prefix(path)
                .delimiter("/")
                .build();

        ListObjectsV2Response response = s3.listObjectsV2(request);

        // 文件列表
        response.contents().forEach(obj -> {
            if (!obj.key().equals(path)) { // 排除目录本身
                String url = aws.getUrl() + obj.key();
                urls.add(url);
            }
        });
        return urls;
    }

    /**
     * 推送到终端
     * 
     * @param deviceToken
     * @param title
     * @param content
     * @throws IOException
     * @throws FirebaseMessagingException
     */
    public void sendPush(
            Long userId,
            Long fromUser,
            String title,
            String content,
            String type,
            Boolean silent) {
        String deviceToken = stringRedisTemplate.opsForValue().get("push_token:" + userId);
        if (deviceToken == null) {
            log.warn("No push token for user {}", userId);
            return;
        }
        Map<String, String> data = new HashMap<>();
        data.put("type", type);
        data.put("title", title);
        data.put("content", content);
        data.put("fromUser", fromUser.toString());
        Message.Builder builder = Message.builder()
                .setToken(deviceToken)
                .putAllData(data);
        if (silent) {
            // 静默
            builder.setApnsConfig(
                    ApnsConfig.builder()
                            .setAps(
                                    Aps.builder()
                                            .setContentAvailable(true)
                                            .build())
                            .build());
        } else {
            // 普通通知
            builder
                    .setNotification(
                            Notification.builder()
                                    .setTitle(title)
                                    .setBody(content)
                                    .build())
                    .setAndroidConfig(
                            AndroidConfig.builder()
                                    .setPriority(AndroidConfig.Priority.HIGH)
                                    .setNotification(
                                            AndroidNotification.builder()
                                                    .setTitle(title)
                                                    .setBody(content)
                                                    .setChannelId("default_channel")
                                                    .setSound("default")
                                                    .build())
                                    .build())
                    .setApnsConfig(
                            ApnsConfig.builder()
                                    .setAps(
                                            Aps.builder()
                                                    .setAlert(
                                                            ApsAlert.builder()
                                                                    .setTitle(title)
                                                                    .setBody(content)
                                                                    .build())
                                                    .setSound("default")
                                                    .build())
                                    .build());
        }

        try {
            String response = FirebaseMessaging.getInstance().send(builder.build());
            log.info("FCM response: {}", response);
        } catch (Exception e) {
            log.error("FCM push error: ", e);
        }
    }
}
