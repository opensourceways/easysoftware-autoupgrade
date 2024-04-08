package com.softwaremarket.collect.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "gitee.premiumapp")
public class PremiumAppConfig {
    private String owner;
    private String repo;
}
