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

/**
 * Created by Shuai.Huan on 2014/7/22.
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_OPERATION_LOG")
@UtopiaCacheExpiration
public class AgentOperationLog extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 5535475654534645463L;

    @UtopiaSqlColumn
    Long operatorId;               // 操作者ID
    @UtopiaSqlColumn
    String operatorName;           // 操作者real name
    @UtopiaSqlColumn
    String operationType;          // 操作类型,login/logout,create/close group, create/close account, update user kpi...
    @UtopiaSqlColumn
    String actionUrl;              // 操作动作的URL
    @UtopiaSqlColumn
    String operationResult;         // 操作结果
    @UtopiaSqlColumn
    String operationNotes;          // 处理备注

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("[");
        buffer.append("operatorId:").append(operatorId).append(",");
        buffer.append("operatorName:").append(operatorName).append(",");
        buffer.append("operationType:").append(operationType).append(",");
        buffer.append("actionUrl:").append(actionUrl).append(",");
        buffer.append("operationResult:").append(operationResult).append(",");
        buffer.append("operationNotes:").append(operationNotes).append("]");
        return buffer.toString();
    }
}
