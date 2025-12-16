package gwl.service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import gwl.constant.AWSConstant;
import gwl.context.BaseContext;
import gwl.service.CommonService;
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
    private AWSConstant aws;
    @Autowired
    private StringRedisTemplate templateRedis;

    @Override
    public Boolean uploadToS3(MultipartFile file, String key) throws java.io.IOException {
        // 构建上传请求
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(aws.getBucket()) //
                .key(key)
                .contentType(file.getContentType())
                .acl("public-read") // 让头像可直接访问
                .build();

        // 上传文件
        s3.putObject(request, RequestBody.fromBytes(file.getBytes()));

        // 拼接公开URL
        String avartarUrl = aws.getUrl() + key;
        // 如果上传的是头像，存redis
        if (key.startsWith("avartar/")) {
             System.out.println("上传头像: " + key);
            templateRedis.opsForValue().set("useravatarurl:" + BaseContext.getCurrentId(), avartarUrl);
        }
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
}
