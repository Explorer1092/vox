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

package com.voxlearning.utopia.service.reward.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.entity.ucenter.TeacherTaskRewardHistory;
import com.voxlearning.utopia.service.reward.api.TeacherTaskRewardHistoryService;
import com.voxlearning.utopia.service.reward.impl.persistence.TeacherTaskRewardHistoryPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.reward.impl.service.TeacherTaskRewardHistoryServiceImpl")
@ExposeService(interfaceClass = TeacherTaskRewardHistoryService.class)
public class TeacherTaskRewardHistoryServiceImpl extends SpringContainerSupport implements TeacherTaskRewardHistoryService {

    @Inject private TeacherTaskRewardHistoryPersistence teacherTaskRewardHistoryPersistence;

    @Override
    public AlpsFuture<TeacherTaskRewardHistory> insertTeacherTaskRewardHistory(TeacherTaskRewardHistory history) {
        if (history == null) {
            return ValueWrapperFuture.nullInst();
        }
        teacherTaskRewardHistoryPersistence.insert(history);
        return new ValueWrapperFuture<>(history);
    }

    @Override
    public AlpsFuture<List<TeacherTaskRewardHistory>> findTeacherTaskRewardHistories(Long teacherId, String taskType) {
        if (teacherId == null || taskType == null) {
            return ValueWrapperFuture.emptyList();
        }
        List<TeacherTaskRewardHistory> histories = teacherTaskRewardHistoryPersistence.findByTeacherIdAndTaskType(teacherId, taskType);
        return new ValueWrapperFuture<>(histories);
    }
}
