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

package com.voxlearning.utopia.admin.data;

import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Sadi.Wan on 2014/12/3.
 */
@Getter
@Setter
@DocumentTable(table = "VOX_P2P_TEACHER_BOOSTER")
public class P2pTeacherBooster implements Serializable {
    private static final long serialVersionUID = 262413836094005579L;

    @UtopiaSqlColumn(name = "TEACHER_ID", primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.NONE) protected Long teacherId;

    /**
     * 跟踪人crm账号
     */
    @UtopiaSqlColumn
    private String followerName;

    @UtopiaSqlColumn
    private P2pTeacherActiveLevel p2pTeacherActiveLevel;

    @UtopiaSqlColumn
    private String note;
}
