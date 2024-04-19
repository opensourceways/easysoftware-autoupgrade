package com.softwaremarket.collect.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "softwareconfig.rpm")
public class RpmConfig {
    //仓库
    String repo;
    //
    private String owner;
}
