package com.voxlearning.utopia.service.workflow.api.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * WorkFlowProcessUser
 *
 * @author song.wang
 * @date 2017/1/6
 */
@Getter
@Setter
public class WorkFlowProcessUser implements Serializable {
    private String userPlatform;  // 用户平台 admin / agent / mizar
    private String account;       // 用户账号
    private String accountName;   // 用户名称

    public WorkFlowProcessUser() {

    }

    public WorkFlowProcessUser(String userPlatform, String account, String accountName) {
        this.userPlatform = userPlatform;
        this.account = account;
        this.accountName = accountName;
    }

}
