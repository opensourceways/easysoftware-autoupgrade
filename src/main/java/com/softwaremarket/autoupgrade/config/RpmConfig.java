package com.softwaremarket.autoupgrade.config;

import com.softwaremarket.autoupgrade.dto.ForkInfoDto;
import com.softwaremarket.autoupgrade.dto.MailInfoDto;
import com.softwaremarket.autoupgrade.dto.PrInfoDto;
import com.softwaremarket.autoupgrade.dto.RepoInfoDto;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "softcenter.rpm")
public class RpmConfig {
    RepoInfoDto repoInfo;
    ForkInfoDto forkInfo;
    PrInfoDto prInfo;
    MailInfoDto mailInfo;
}
