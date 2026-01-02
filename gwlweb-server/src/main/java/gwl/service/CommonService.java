package gwl.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.google.firebase.messaging.FirebaseMessagingException;

public interface CommonService {
    /**
     * 上传到S3
     * 
     * @return
     */
    Boolean uploadToS3(MultipartFile file, String key);

    /**
     * 获取s3的img地址列表
     * 
     * @param path
     * @return
     */
    public List<String> getS3ImgUrls(String path);

    /**
     * android推送
     * 
     * @param deviceToken
     * @param title
     * @param content
     * @throws IOException
     * @throws FirebaseMessagingException
     */
    void sendPush(Long userId, String title, String content, String type, Boolean silent);
}
