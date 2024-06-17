package com.softwaremarket.collect.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "gitee.pr")
public class PulllRequestConfig {
    private String rpmPrTitle;
    private String rpmIssueNum;
    private String appPrTitle;
    private String appIssueNum;
    private String changelog;
}
