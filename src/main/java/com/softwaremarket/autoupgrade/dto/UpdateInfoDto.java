package com.softwaremarket.autoupgrade.dto;

import lombok.Data;

import java.util.Objects;

@Data
public class UpdateInfoDto {
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

    //pr Body;
    private StringBuilder prBody = new StringBuilder();

    private String prTitle;

    private String branch;

    private String sourceUrl;

    public Boolean checkInfoIsComplete() {
        return Objects.nonNull(this.appName) && Objects.nonNull(this.upAppLatestVersion) && Objects.nonNull(this.oeAppLatestVersion) && Objects.nonNull(this.communityCurrentOsVersion);
    }


    public Boolean checkAppVersion() {
        return String.valueOf(this.upAppLatestVersion).equals(String.valueOf(this.oeAppLatestVersion));
    }


    public Boolean checkRpmInfoIsComplete() {
        return Objects.nonNull(this.oeAppLatestVersion) && Objects.nonNull(this.sourceUrl) && Objects.nonNull(this.upAppLatestVersion);
    }
}
