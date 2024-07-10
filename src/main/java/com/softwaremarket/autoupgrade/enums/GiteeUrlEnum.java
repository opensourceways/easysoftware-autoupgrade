package com.softwaremarket.autoupgrade.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GiteeUrlEnum {
    PostV5ReposOwnerRepoForksUrl("https://gitee.com/api/v5/repos/%s/%s/forks", "fork"),
    GiteeGetV5ReposOwnerRepoPullsUrl("https://gitee.com/api/v5/repos/{owner}/{repo}/pulls?access_token={access_token}&state=all&sort=created&direction=desc&page={page}&per_page=100", "获取该仓库提交的pr"),
    ContentsUrl("https://gitee.com/api/v5/repos/{owner}/{repo}/contents/{path}?access_token={access_token}&ref={ref}", "获取文件内容"),
    ReposInfoUrl("https://gitee.com/api/v5/orgs/{org}/repos?access_token={token}&type=all&per_page=20&page=", "获取仓库信息");
    private final String url;
    private final String description;
}
