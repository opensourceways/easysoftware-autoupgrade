package com.softwaremarket.autoupgrade.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum CollectEnum {

    DOCKER_UP_STREAM("%s/query/software/info?community=openeuler&repo=redis&tag=docker_up", "docker上游"),
    DOCKER_OPENEULER("%s/query/software/info?community=openeuler&repo=redis&tag=docker_openeuler", "docker openeuler"),


    DOCKER("%s/api/v2/projects/?name=", "获取版本信息"),
    APPKG("%s/api-query/domain?timeOrder=asc&name=apppkg&pageSize=", "获取精品应用");
    private final String url;

    private final String msg;

}
