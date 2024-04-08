package com.softwaremarket.collect.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommitInfoEnum {
    PremiumApp("master", "%s版本新增支持%s容器镜像");
    private final String branch;

    private final String message;
}
