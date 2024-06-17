package com.softwaremarket.collect.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommitInfoEnum {
    PremiumApp("master", "%s update %s to %s"),
    RPM("master", "update %s to %s");
    private final String branch;

    private final String message;
}
