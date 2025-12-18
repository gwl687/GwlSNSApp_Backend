package gwl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// @EnableDubbo(scanBasePackages = "gwl")
public class GwlwebServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GwlwebServerApplication.class, args);
    }
}
