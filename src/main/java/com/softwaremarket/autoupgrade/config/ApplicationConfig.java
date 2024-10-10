package com.softwaremarket.autoupgrade.config;

import com.softwaremarket.autoupgrade.dto.ForkInfoDto;
import com.softwaremarket.autoupgrade.dto.MailInfoDto;
import com.softwaremarket.autoupgrade.dto.PrInfoDto;
import com.softwaremarket.autoupgrade.dto.RepoInfoDto;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "softcenter.application")
@Data
// 精品应用配置
public class ApplicationConfig {
    RepoInfoDto repoInfo;
    ForkInfoDto forkInfo;
    PrInfoDto prInfo;
    MailInfoDto mailInfo;
    //需要补全的欧拉版本
    List<String> eulerversion;
}
