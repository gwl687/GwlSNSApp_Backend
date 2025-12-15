package gwl.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface CommonService {
    /**
     * 上传到S3
     * 
     * @return
     */
    Boolean uploadToS3(MultipartFile file, String key)
            throws IOException;
    /**
     * 获取s3的img地址列表
     * @param path
     * @return
     */
    public List<String> getS3ImgUrls(String path);
}
