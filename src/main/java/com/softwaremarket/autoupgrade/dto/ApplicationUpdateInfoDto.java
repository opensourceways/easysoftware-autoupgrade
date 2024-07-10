package com.softwaremarket.autoupgrade.dto;

import lombok.Data;

import java.util.Objects;

@Data
public class ApplicationUpdateInfoDto {
    //app名字
    private String appName;

    //APP上游最新版本
    private String upAppLatestVersion;

    //APP欧拉社区最新版本
    private String oeAppLatestVersion;

    //欧拉当前版本
    private String communityCurrentOsVersion;

    //欧拉添加版本
    private String communityOtherOsVersion;

    public Boolean checkInfoIsComplete() {
        return Objects.nonNull(this.appName) && Objects.nonNull(this.upAppLatestVersion) && Objects.nonNull(this.oeAppLatestVersion) && Objects.nonNull(this.communityCurrentOsVersion);
    }
}
