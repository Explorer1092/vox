/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * AGENT用户表
 *
 * @author Zhilong Hu
 * @serial
 * @since 2014-7-4
 */
@Data
@EqualsAndHashCode(callSuper = false)
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_USER")
@UtopiaCacheRevision("20170420")
@UtopiaCacheExpiration
public class AgentUser extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 2169025113244531307L;

    @UtopiaSqlColumn String accountName;             // 用户登录名
    @UtopiaSqlColumn String realName;                // 用户真实姓名
    @UtopiaSqlColumn String passwd;                  // 用户密码
    @UtopiaSqlColumn String passwdSalt;              // 用户密码Salt
    @UtopiaSqlColumn String userComment;             // 备注
    @UtopiaSqlColumn Date contractStartDate;         // 合同开始时间
    @UtopiaSqlColumn Date contractEndDate;           // 合同结束时间
    @UtopiaSqlColumn String contractNumber;           // 合同编号
    @UtopiaSqlColumn Float cashAmount;               // 现金账户余额
    @UtopiaSqlColumn Float pointAmount;              // 点数账户余额
    @UtopiaSqlColumn Float usableCashAmount;         // 现金账户可用余额
    @UtopiaSqlColumn Float usablePointAmount;        // 点数账户可用余额
    @UtopiaSqlColumn String tel;                     // 电话
    @UtopiaSqlColumn String email;                   // 邮箱
    @UtopiaSqlColumn String imAccount;               // QQ
    @UtopiaSqlColumn String address;                 // 地址
    @UtopiaSqlColumn Integer cashDeposit;            // 保证金金额
    @UtopiaSqlColumn Boolean cashDepositReceived;    // 保证金是否已到帐
    @UtopiaSqlColumn String bankName;                // 开户行名称
    @UtopiaSqlColumn String bankHostName;            // 开户人姓名
    @UtopiaSqlColumn String bankAccount;             // 银行帐号
    @UtopiaSqlColumn Integer status;                 // 用户状态，0:新建，强制更新密码，1:有效，9:关闭
    @UtopiaSqlColumn String deviceId;                 // 设备ID
    @UtopiaSqlColumn String avatar;                 // 头像
    @UtopiaSqlColumn Float materielBudget;          // 物料预算
    @UtopiaSqlColumn String accountNumber;           // 工号

//    List<AgentGroup> userGroups;                      // 用户所属的群组列表

    public boolean isValidUser() {
        return (status != null) && (status != 9);
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(AgentUser.class, "ALL");
    }

    public static String ck_name(String accountName) {
        return CacheKeyGenerator.generateCacheKey(AgentUser.class, "name", accountName);
    }

    public static String ck_id(Long userId) {
        return CacheKeyGenerator.generateCacheKey(AgentUser.class, userId);
    }

}
