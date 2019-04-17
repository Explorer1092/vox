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

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceMethod;
import com.voxlearning.utopia.service.business.api.BusinessManagement;
import com.voxlearning.utopia.service.business.api.entity.RSOralReportStat;
import com.voxlearning.utopia.service.business.api.entity.TeacherActivateTeacherHistory;
import com.voxlearning.utopia.service.business.api.entity.UserBalanceLog;
import com.voxlearning.utopia.service.business.api.mapper.ChipsRedPack;
import com.voxlearning.utopia.service.business.api.mapper.RedPackMapper;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BusinessManagementClient implements BusinessManagement {

    @ImportService(interfaceClass = BusinessManagement.class)
    private BusinessManagement remoteReference;

    @Override
    @ServiceMethod(timeout = 5, unit = TimeUnit.MINUTES, retries = 0)
    public int sendRedPacks(List<RedPackMapper> dataList) {
        return remoteReference.sendRedPacks(dataList);
    }

    @Override
    public void sendChipsRedPacks(Collection<ChipsRedPack> userRedPacks) {
        remoteReference.sendChipsRedPacks(userRedPacks);
    }

    @Override
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    public Map<Long, List<TeacherActivateTeacherHistory>> findTeacherActivateTeacherHistoryMapByInviterIds(Collection<Long> inviterIds) {
        return remoteReference.findTeacherActivateTeacherHistoryMapByInviterIds(inviterIds);
    }

    @Override
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    public Map<Long, List<TeacherActivateTeacherHistory>> findTeacherActivateTeacherHistoryMapByInviteeIds(Collection<Long> inviteeIds) {
        return remoteReference.findTeacherActivateTeacherHistoryMapByInviteeIds(inviteeIds);
    }

    @Override
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    public List<UserBalanceLog> findUserBalanceLogListByUserId(Long userId) {
        return remoteReference.findUserBalanceLogListByUserId(userId);
    }

    @Override
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    public List<RSOralReportStat> findRSOralReportStatListExcludeSchool(String taskId, String docId, Integer acode, Long schoolId, Date searchDate) {
        return remoteReference.findRSOralReportStatListExcludeSchool(taskId, docId, acode, schoolId, searchDate);
    }

    @Override
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    public List<RSOralReportStat> findAreaRSOralReportStatList(String taskId, String docId, Integer ccode, Long schoolId, Date searchDate) {
        return remoteReference.findAreaRSOralReportStatList(taskId, docId, ccode, schoolId, searchDate);
    }

    @Override
    public void scheduleAutoResearchStaffBehaviorDataJob(String fromDateStr, String endDateStr, String jobOnly, boolean needClear) {
        remoteReference.scheduleAutoResearchStaffBehaviorDataJob(fromDateStr, endDateStr, jobOnly, needClear);
    }
}
