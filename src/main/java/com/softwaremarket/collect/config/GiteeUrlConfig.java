package com.softwaremarket.collect.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "gitee.url")
public class GiteeUrlConfig {
    String postV5ReposOwnerRepoForksUrl;
    String giteeGetV5ReposOwnerRepoPullsUrl;

    String contentsUrl;

}
