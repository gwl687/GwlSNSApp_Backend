package gwl.constant;

public class AWSConstant {
    // AWS property
    @org.springframework.beans.factory.annotation.Value("${aws.accessKey}")
    public static String accessKey;

    @org.springframework.beans.factory.annotation.Value("${aws.secretKey}")
    public static String secretKey;

    @org.springframework.beans.factory.annotation.Value("${aws.region}")
    public static String region;

    @org.springframework.beans.factory.annotation.Value("${aws.s3.avatar}")
    public static String bucketOrAccessPoint;
}
