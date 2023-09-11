package com.mailer.mailer.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties(prefix = "credentials")
@Getter
@Setter
public class CredentialProperties {

    private Path path;
}
