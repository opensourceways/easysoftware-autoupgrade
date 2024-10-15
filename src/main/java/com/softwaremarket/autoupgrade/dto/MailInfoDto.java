package com.softwaremarket.autoupgrade.dto;

import lombok.Data;

@Data
public class MailInfoDto {
    //邮件发送的服务器 比如qq、163，Outlook等
    String host;
    //发送者邮箱
    String from;
    //发送者用户名
    String senderUsername;
    //发送者授权码
    String senderPassword;
    // 容器镜像邮件默认接收者
    String applicationDefaultReveiver;
}
