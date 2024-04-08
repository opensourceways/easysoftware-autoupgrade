package com.softwaremarket.collect.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TreeTypeEnum {
    BLOB("blob", "具体文件"),
    TREE("tree", "文件夹");

    private final String type;

    private final String message;
}
