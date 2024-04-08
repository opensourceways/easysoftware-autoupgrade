package com.softwaremarket.collect.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "gitee.pr")
public class PulllRequestConfig {
    private String titleTemplate;
    private String bodyTemplate;
    private String issueTitleTemplate;
}
