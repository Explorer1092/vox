/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by XiaoPeng.Yang on 15-3-16.
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_ACTIVITY_RECHARGE_TEACHER")
public class ActivityRechargeTeacher extends AbstractDatabaseEntity {
    private static final long serialVersionUID = -6876431764241097786L;

    @DocumentField("TEACHER_ID") private Long teacherId;
    @DocumentField("MONTH") private Integer month;
    @DocumentField("RECHARGE_AMOUNT") private Integer rechargeAmount;//充值金额
    @DocumentField("STU_COUNT") private Integer stuCount;//完成作业学生数量
    @DocumentField("RECHARGED") private Boolean recharged;//是否完成奖励
    @DocumentField("STATUS") private Integer status;//数据状态
}
