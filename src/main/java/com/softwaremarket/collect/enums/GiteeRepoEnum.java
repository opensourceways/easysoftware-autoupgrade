package com.softwaremarket.collect.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GiteeRepoEnum {
    PREMIUMAPP("openeuler", "openeuler-docker-images"),
    RPM("src-openEuler", "");
    private String owner;
    private String repo;
}
