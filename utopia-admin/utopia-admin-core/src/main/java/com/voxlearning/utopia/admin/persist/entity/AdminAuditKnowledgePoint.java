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

package com.voxlearning.utopia.admin.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * @author shuai.huan
 * @since 2014-04-30
 */
@Getter
@Setter
@DocumentConnection(configName = "admin")
@DocumentTable(table = "ADMIN_KNOWLEDGE_POINT_AUDIT")
public class AdminAuditKnowledgePoint extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = 1625463955837150809L;

    @UtopiaSqlColumn(name = "POINT_ID") Long pointId;
    @UtopiaSqlColumn(name = "PARENT_ID") Long parentId;
    @UtopiaSqlColumn(name = "SUBJECT_ID") Integer subjectId;
    @UtopiaSqlColumn(name = "POINT_NAME") String pointName;
    @UtopiaSqlColumn(name = "POINT_TYPE") String pointType;
    @UtopiaSqlColumn(name = "TYPE") Integer type;
    @UtopiaSqlColumn(name = "OPERATOR") String operator;
    @UtopiaSqlColumn(name = "NEW_KNOWLEDGE") boolean newKnowledge;

    //view用，不对应具体字段
    @DocumentFieldIgnore
    String nodeInfo;

    @Override
    public String toString() {
        return "AdminAuditKnowledgePoint{" +
                "pointId=" + pointId +
                ", parentId=" + parentId +
                ", subjectId=" + subjectId +
                ", pointName='" + pointName + '\'' +
                ", pointType='" + pointType + '\'' +
                ", type=" + type +
                ", userId=" + operator +
                ", nodeInfo='" + nodeInfo + '\'' +
                '}';
    }
}
