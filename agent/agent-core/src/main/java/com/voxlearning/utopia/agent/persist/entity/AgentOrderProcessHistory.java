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
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * AGENT订单处理历史表
 *
 * @author Shuai Huan
 * @serial
 * @since 2014-7-14
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_ORDER_PROCESS_HIS")
@UtopiaCacheExpiration
public class AgentOrderProcessHistory extends AbstractDatabaseEntity {

    public static final int RESULT_APPROVED = 0;
    public static final int RESULT_REJECTED = 1;
    private static final long serialVersionUID = 8944545345754535846L;

    @UtopiaSqlColumn
    Long orderId;                   // 订单ID
    @UtopiaSqlColumn
    Long processor;                 // 处理者ID
    @UtopiaSqlColumn
    Integer result;                 // 订单处理结果 0：同意，1：拒绝
    @UtopiaSqlColumn
    String processNotes;            // 处理备注

    @DocumentFieldIgnore
    AgentOrder order;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(AgentOrderProcessHistory.class, id);
    }

    public static String ck_orderId(Long orderId) {
        return CacheKeyGenerator.generateCacheKey(AgentOrderProcessHistory.class, "orderId", orderId);
    }

    public static String ck_processor(Long processor) {
        return CacheKeyGenerator.generateCacheKey(AgentOrderProcessHistory.class, "processor", processor);
    }
}
