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

package com.voxlearning.utopia.admin.persist;

import com.voxlearning.alps.dao.jdbc.persistence.AbstractEntityPersistence;
import com.voxlearning.utopia.admin.persist.entity.AdminAuditKnowledgePoint;

import javax.inject.Named;
import java.util.List;

/**
 * @author shuai.huan
 * @since 2014-04-30
 */
@Named
public class AdminKnowledgePointAuditPersistence extends AbstractEntityPersistence<Long, AdminAuditKnowledgePoint> {

    public AdminAuditKnowledgePoint getById(Long id) {
        return withSelectFromTable("WHERE ID=? AND DISABLED = FALSE").useParamsArgs(id).queryObject();
    }

    public AdminAuditKnowledgePoint getByNameAndType(String name, String pointType, Integer auditType, Integer subjectId, boolean isNew) {
        return withSelectFromTable("WHERE POINT_NAME= BINARY ? AND POINT_TYPE= BINARY ? AND TYPE = ? AND SUBJECT_ID = ? AND NEW_KNOWLEDGE = ? AND DISABLED = FALSE").useParamsArgs(name, pointType, auditType, subjectId, isNew).queryObject();
    }

    public AdminAuditKnowledgePoint getByPointIdAndType(Long pointId, Integer type) {
        return withSelectFromTable("WHERE POINT_ID=? AND TYPE=? AND DISABLED=FALSE").useParamsArgs(pointId, type).queryObject();
    }

    public List<AdminAuditKnowledgePoint> getByOperator(String operator, Integer subjectId, boolean isNew) {
        if (subjectId != -1) {
            return withSelectFromTable("WHERE OPERATOR= BINARY ? AND SUBJECT_ID=? AND NEW_KNOWLEDGE=? AND DISABLED=FALSE").useParamsArgs(operator, subjectId, isNew).queryAll();
        } else {
            return withSelectFromTable("WHERE OPERATOR= BINARY ? AND NEW_KNOWLEDGE=? AND DISABLED=FALSE").useParamsArgs(operator, isNew).queryAll();
        }
    }

    public Long saveAuditKnowledgePoint(Long pointId, String pointName, String pointType, Integer type, String operator, Long parentId, Integer subjectId, boolean isNew) {
        AdminAuditKnowledgePoint point = new AdminAuditKnowledgePoint();
        point.setPointId(pointId);
        point.setPointName(pointName);
        point.setPointType(pointType);
        point.setType(type);
        point.setOperator(operator);
        point.setParentId(parentId);
        point.setSubjectId(subjectId);
        point.setNewKnowledge(isNew);
        return persist(point);
    }

    public void delById(Long id) {
        final String sql = "UPDATE ADMIN_KNOWLEDGE_POINT_AUDIT SET DISABLED=TRUE,UPDATE_DATETIME = NOW() WHERE ID = ?";
        getUtopiaSql().withSql(sql).useParamsArgs(id).executeUpdate();
    }

    //审核页面
    public List<AdminAuditKnowledgePoint> getBySubjectId(Integer subjectId, boolean isNew) {
        return withSelectFromTable("WHERE SUBJECT_ID=? AND NEW_KNOWLEDGE=? AND DISABLED=FALSE").useParamsArgs(subjectId, isNew).queryAll();
    }
}
