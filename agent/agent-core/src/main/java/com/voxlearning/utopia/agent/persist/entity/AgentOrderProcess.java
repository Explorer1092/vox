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

/**
 * AGENT订单处理表
 *
 * @author Shuai Huan
 * @serial
 * @since 2014-7-14
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_ORDER_PROCESS")
public class AgentOrderProcess extends AbstractDatabaseEntity {
    private static final long serialVersionUID = 2741325413223423231L;

    @UtopiaSqlColumn
    private Long orderId;                   // 订单ID
    @UtopiaSqlColumn
    private Long targetGroup;               // 处理组ID
    @UtopiaSqlColumn
    private Long targetUser;                // 处理用户ID
}
