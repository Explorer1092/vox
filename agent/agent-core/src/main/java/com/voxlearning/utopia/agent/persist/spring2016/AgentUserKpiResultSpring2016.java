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

package com.voxlearning.utopia.agent.persist.spring2016;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * AGENT系统 用户CPA 计算明细表
 * FIXME 这个要不要保留给以后查记录用。。。 或者 先把静态保存下来。。。
 *
 * @author yuechen.wang
 * @serial
 * @since 2015-03-15
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_USER_KPI_RESULT_S2016")
public class AgentUserKpiResultSpring2016 extends AbstractDatabaseEntityWithDisabledField implements Serializable {
    private static final long serialVersionUID = 1801211610665863083L;

    @UtopiaSqlColumn Long regionId;              // 大区组ID
    @UtopiaSqlColumn String regionName;          // 大区组名
    @UtopiaSqlColumn Long provinceId;           // 区域组ID
    @UtopiaSqlColumn String provinceName;        // 区域组名
    @UtopiaSqlColumn Integer countyCode;         // 地区编码
    @UtopiaSqlColumn String countyName;          // 地区名称
    @UtopiaSqlColumn Long schoolId;              // 学校ID
    @UtopiaSqlColumn String schoolName;          // 学校名称
    @UtopiaSqlColumn Integer schoolLevel;        // 1-小学,2-中学
    @UtopiaSqlColumn Integer salaryMonth;        // 结算月
    @UtopiaSqlColumn Long userId;                // 用户ID
    @UtopiaSqlColumn String userName;            // 用户名称
    @UtopiaSqlColumn Date startDate;             // 绩效开始日期
    @UtopiaSqlColumn Date endDate;               // 绩效结束日期
    @UtopiaSqlColumn String cpaType;             // CPA类型
    @UtopiaSqlColumn Long cpaTarget;             // CPA目标
    @UtopiaSqlColumn Long cpaResult;             // 业绩
    @UtopiaSqlColumn Long cpaSalary;             // 工资
    @UtopiaSqlColumn String cpaNote;             // 备注
    @UtopiaSqlColumn Boolean financeCheck;       // 财务确认
    @UtopiaSqlColumn Boolean marketCheck;        // 市场确认
    @UtopiaSqlColumn Boolean disabled;           // 是否已被删除

}
