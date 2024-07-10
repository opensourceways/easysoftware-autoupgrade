package com.softwaremarket.autoupgrade.dto;

import lombok.Data;

@Data
//操作者信息
public class ForkInfoDto {
    // 会将代码fork到该token用户的仓库,后续都会以该用户去操作
    String accessToken;
    String owner;
    String email;
    String name;


    String password;
    //账号权限范围
    String scope;
    String clientId;
    String clientSecret;


}
