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

package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.ServiceMethod;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
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

@ServiceVersion(version = "20160308")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 0)
@CyclopsMonitor("utopia")
public interface BusinessManagement extends IPingable {

    @ServiceMethod(timeout = 5, unit = TimeUnit.MINUTES, retries = 0)
    int sendRedPacks(List<RedPackMapper> dataList);

    @ServiceMethod(timeout = 5, unit = TimeUnit.MINUTES, retries = 0)
    void sendChipsRedPacks(Collection<ChipsRedPack> userRedPacks);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    Map<Long, List<TeacherActivateTeacherHistory>> findTeacherActivateTeacherHistoryMapByInviterIds(Collection<Long> inviterIds);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    Map<Long, List<TeacherActivateTeacherHistory>> findTeacherActivateTeacherHistoryMapByInviteeIds(Collection<Long> inviteeIds);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    List<UserBalanceLog> findUserBalanceLogListByUserId(Long userId);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    List<RSOralReportStat> findRSOralReportStatListExcludeSchool(String taskId, String docId, Integer acode, Long schoolId, Date searchDate);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    List<RSOralReportStat> findAreaRSOralReportStatList(String taskId, String docId, Integer ccode, Long schoolId, Date searchDate);

    void scheduleAutoResearchStaffBehaviorDataJob(String fromDateStr, String endDateStr, String jobOnly, boolean needClear);
}
