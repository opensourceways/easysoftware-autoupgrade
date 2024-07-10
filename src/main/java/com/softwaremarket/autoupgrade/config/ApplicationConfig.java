package com.softwaremarket.autoupgrade.config;

import com.softwaremarket.autoupgrade.dto.ForkInfoDto;
import com.softwaremarket.autoupgrade.dto.PrInfoDto;
import com.softwaremarket.autoupgrade.dto.RepoInfoDto;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "softcenter.application")
@Data
// 精品应用配置
public class ApplicationConfig {
    RepoInfoDto repoInfo;
    ForkInfoDto forkInfo;
    PrInfoDto prInfo;
}
