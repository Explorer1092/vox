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

package com.voxlearning.utopia.agent.persist.entity.statistics;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by XiaoPeng.Yang on 15-3-10.
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_APP_STATISTICS_MONTH")
public class AppStatisticsMonth extends AbstractDatabaseEntity {
    private static final long serialVersionUID = 5675174218324137931L;

    @UtopiaSqlColumn Integer month;
    @UtopiaSqlColumn String appKey;
    @UtopiaSqlColumn Double totalRevenue;//总收入
    @UtopiaSqlColumn Double procedureFees;//手续费
    @UtopiaSqlColumn Double paidRate;//付费率
    @UtopiaSqlColumn Double refundAmount;//退费金额
    @UtopiaSqlColumn Double sharedRevenue;//分成收入
    @UtopiaSqlColumn Double deservedRevenue;//应得收入
    @UtopiaSqlColumn Boolean status;//0不开放  1开放
}
