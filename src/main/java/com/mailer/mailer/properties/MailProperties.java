package com.mailer.mailer.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "email")
@Getter
@Setter
public class MailProperties {

    private String from;
    private String to;
    private String subject;
}
