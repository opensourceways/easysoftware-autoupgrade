package com.softwaremarket.autoupgrade.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@Data
@ConfigurationProperties(prefix = "collectconfig")
//采集版本信息配置
public class CollectConfig {
    String sotfwareInfoUrl;
    String apppkgInfoUrl;
    String projectsInfoUrl;
    String versionsInfoUrl;
    String openEulerOsVersionInfoUrl;
    String dockerHubOpeneulerOsVersionInfoUrl;

    String dockerHubOpeneulerHomepage;
    String dockerHubOpeneulerAuthorization;
}
