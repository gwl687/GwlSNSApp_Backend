package gwl.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@ConfigurationProperties(prefix = "aws.s3")
@Data
public class AwsProperties {
    private String region;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String url;
}
