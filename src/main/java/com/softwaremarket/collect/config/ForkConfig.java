package com.softwaremarket.collect.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "gitee.fork")
public class ForkConfig {
    // 会将代码fork到该token用户的仓库
    String accessToken;
    String owner;
    String email;
    String name;
}
