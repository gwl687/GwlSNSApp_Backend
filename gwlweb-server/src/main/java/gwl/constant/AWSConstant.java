package gwl.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "aws.s3")
public class AWSConstant {

    private String region;

    private String accessKey;

    private String secretKey;

    private String bucket;

    private String url;
}
