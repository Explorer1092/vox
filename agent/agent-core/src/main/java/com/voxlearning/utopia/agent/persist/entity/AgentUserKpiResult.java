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

package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * AGENT用户KPI考核结果表
 *
 * @author Zhilong Hu
 * @serial
 * @since 2014-7-4
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_USER_KPI_RESULT")
@UtopiaCacheExpiration
public class AgentUserKpiResult extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 2269025113244531305L;

    @UtopiaSqlColumn Date kpiEvalDate;               // KPI考核日期
    @UtopiaSqlColumn Date startTime;                 // 实际开始日期
    @UtopiaSqlColumn Date endTime;                   // 实际结束日期
    @UtopiaSqlColumn Long kpiEvalId;                 // KPI考核ID
    @UtopiaSqlColumn Long kpiId;                     // KPIID
    @UtopiaSqlColumn String kpiName;                 // KPI名
    @UtopiaSqlColumn String regionCode;              // 区域代码
    @UtopiaSqlColumn String regionName;              // 区域名
    @UtopiaSqlColumn Long userId;                    // 用户ID
    @UtopiaSqlColumn String userName;                // 用户名
    @UtopiaSqlColumn Long kpiTarget;                 // KPI目标
    @UtopiaSqlColumn Long kpiResult;                 // KPI实际结果
    @UtopiaSqlColumn Integer studentAuthNumLv;       // 1,2年级的新增认证数量
    @UtopiaSqlColumn Float cashReward;               // 考核结果的现金奖励
    @UtopiaSqlColumn Float pointReward;              // 考核结果的点数奖励
    @UtopiaSqlColumn Boolean financeCheck;           // 财务确认
    @UtopiaSqlColumn Boolean managerCheck;           // 领导确认

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("userId:").append(userId).append("\n");
        builder.append("userName:").append(userName).append("\n");
        builder.append("kpiEvalId:").append(kpiEvalId).append("\n");
        builder.append("kpiId:").append(kpiId).append("\n");
        builder.append("kpiName:").append(kpiName).append("\n");
        builder.append("regionCode:").append(regionCode).append("\n");
        builder.append("regionName:").append(regionName).append("\n");
        builder.append("kpiEvalDate:").append(kpiEvalDate).append("\n");
        builder.append("kpiTarget:").append(kpiTarget).append("\n");
        builder.append("kpiResult:").append(kpiResult).append("\n");
        builder.append("cashReward:").append(cashReward).append("\n");
        builder.append("pointReward:").append(pointReward).append("\n");
        builder.append("financeCheck:").append(financeCheck).append("\n");
        builder.append("managerCheck:").append(managerCheck).append("\n");
        return builder.toString();
    }

}
