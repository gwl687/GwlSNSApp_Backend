package gwl.service.Impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import gwl.constant.AWSConstant;
import gwl.context.BaseContext;
import gwl.pojo.DTO.UserInfoDTO;
import gwl.service.CommonService;
import io.jsonwebtoken.io.IOException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class CommonServiceImpl implements CommonService {

    @Override
    public Boolean uploadToS3(MultipartFile file, String fileName, String type) throws java.io.IOException {

        // 创建 S3 客户端
        try (
                S3Client s3 = S3Client.builder()
                        .region(software.amazon.awssdk.regions.Region.of(AWSConstant.region))
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(
                                                AWSConstant.accessKey, AWSConstant.secretKey)))
                        .build()) {

            // 上传路径
            String key = type + "/" + fileName;

            // 构建上传请求
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(AWSConstant.bucketOrAccessPoint) //
                    .key(key)
                    .contentType(file.getContentType())
                    .acl("public-read") // 让头像可直接访问
                    .build();

            // 上传文件
            s3.putObject(request, RequestBody.fromBytes(file.getBytes()));
            // 拼接公开URL
            String url = "https://gwltest-01.s3.ap-northeast-1.amazonaws.com/" + key;
            System.out.println("✅ 上传成功: " + key);

            return true;

        } catch (IOException e) {
            System.err.println("❌ 上传失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取s3图片urls
     * 
     * @return
     */
    public List<String> getS3ImgUrls(String path) {
        List<String> urls = new ArrayList<>();
        try (
                S3Client s3 = S3Client.builder()
                        .region(software.amazon.awssdk.regions.Region.of(AWSConstant.region))
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(
                                                AWSConstant.accessKey, AWSConstant.secretKey)))
                        .build()) {

            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(AWSConstant.bucketOrAccessPoint)
                    .prefix(path)
                    .delimiter("/")
                    .build();

            ListObjectsV2Response response = s3.listObjectsV2(request);

            //文件列表
            response.contents().forEach(obj -> {
                if (!obj.key().equals(path)) { // 排除目录本身
                    String url = "https://gwltest-01.s3.ap-northeast-1.amazonaws.com/" + obj.key();
                    urls.add(url);
                }
            });

        } catch (IOException e) {
            System.err.println("❌ 获取失败: " + e.getMessage());
            e.printStackTrace();
        }
        return urls;
    }
}
