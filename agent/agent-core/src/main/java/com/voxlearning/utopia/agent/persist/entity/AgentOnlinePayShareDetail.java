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

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by Alex on 15-2-12.
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_ONLINE_PAY_SHARE_DETAIL")
public class AgentOnlinePayShareDetail extends AbstractDatabaseEntity {
    private static final long serialVersionUID = 4944545335752535844L;

    @UtopiaSqlColumn Date kpiEvalDate;
    @UtopiaSqlColumn Date startTime;
    @UtopiaSqlColumn Date endTime;
    @UtopiaSqlColumn String regionCode;
    @UtopiaSqlColumn String regionName;
    @UtopiaSqlColumn String productName;
    @UtopiaSqlColumn Integer payMonth;
    @UtopiaSqlColumn Double totalIncome;
    @UtopiaSqlColumn Double cardPayAmount;
    @UtopiaSqlColumn Double refundAmount;
    @UtopiaSqlColumn Double operationRate;
    @UtopiaSqlColumn Double shareableAmount;
    @UtopiaSqlColumn Integer payUserNum;
    @UtopiaSqlColumn Integer monthlyActiveUsers;
    @UtopiaSqlColumn Double monthlyPayRate;
    @UtopiaSqlColumn Long userId;
    @UtopiaSqlColumn String userName;
    @UtopiaSqlColumn Double shareAmount;
    @UtopiaSqlColumn Boolean financeCheck;           // 财务确认
    @UtopiaSqlColumn Boolean managerCheck;           // 领导确认
}