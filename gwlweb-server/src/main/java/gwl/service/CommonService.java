package gwl.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;


public interface CommonService {
    /**
     * 上传到S3
     * 
     * @return
     */
    Boolean uploadToS3(MultipartFile file, String fileName, String type)
            throws IOException;
}
