package com.gwl.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@ConfigurationProperties(prefix = "app.mail")
@Data
public class MailProperties {
    private String senderEmailaddress;
}
