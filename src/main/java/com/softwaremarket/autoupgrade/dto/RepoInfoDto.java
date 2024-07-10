package com.softwaremarket.autoupgrade.dto;

import lombok.Data;

@Data
// 仓库信息
public class RepoInfoDto {
    //仓库owmer
    private String owner;
    //仓库
    private String repo;
}
