package gwl;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.zookeeper.Environment;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
// @EnableDubbo(scanBasePackages = "gwl")
public class GwlwebServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GwlwebServerApplication.class, args);
    }
}
