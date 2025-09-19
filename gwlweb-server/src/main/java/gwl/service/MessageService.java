package gwl.service;

import com.alibaba.dubbo.config.annotation.Service;

import io.swagger.v3.oas.annotations.servers.Server;

@org.springframework.stereotype.Service
public class MessageService {
    public void saveMessage(String from, String to, String content) {
        // 这里可以接 MySQL 或 Redis
        System.out.printf("保存消息：%s -> %s : %s%n", from, to, content);
    }
}
