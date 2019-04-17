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
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * AGENT用户账户历史表
 *
 * @author Shuai Huan
 * @serial
 * @since 2014-7-14
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_USER_ACCOUNT_HIS")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20160727")
public class AgentUserAccountHistory extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 7831573564555423545L;

    private Long userId;                    // 用户ID
    private Float cashBefore;               // 现金帐户变更前余额
    private Float cashAmount;               // 现金帐户变更金额
    private Float cashAfter;                // 现金帐户变更后金额
    private Float pointBefore;              // 点数帐户变更前余额
    private Float pointAmount;              // 点数帐户变更金额
    private Float pointAfter;               // 点数帐户变更后金额
    private Long orderId;                   // 关联订单ID
    private String comments;                // 账户变更说明

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("userId", userId)
        };
    }
}
