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

package com.voxlearning.utopia.service.reward.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.entity.ucenter.TeacherTaskRewardHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Summer Yang on 2015/7/20.
 */
@Named("com.voxlearning.utopia.service.reward.impl.persistence.TeacherTaskRewardHistoryPersistence")
@CacheBean(type = TeacherTaskRewardHistory.class)
public class TeacherTaskRewardHistoryPersistence extends StaticMySQLPersistence<TeacherTaskRewardHistory, Long> {

    @Override
    protected void calculateCacheDimensions(TeacherTaskRewardHistory document, Collection<String> dimensions) {
        dimensions.add(TeacherTaskRewardHistory.ck_teacherId_taskType(document.getTeacherId(), document.getTaskType()));
    }

    @CacheMethod
    public List<TeacherTaskRewardHistory> findByTeacherIdAndTaskType(@CacheParameter("teacherId") Long teacherId,
                                                                     @CacheParameter("taskType") String taskType) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId).and("TASK_TYPE").is(taskType);
        return query(Query.query(criteria));
    }
}
