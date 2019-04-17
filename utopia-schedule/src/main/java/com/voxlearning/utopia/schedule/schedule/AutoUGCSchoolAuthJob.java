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

package com.voxlearning.utopia.schedule.schedule;

/**
 * @author Jia HuanYin
 * @since 2016/1/13
 */

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchool;
import com.voxlearning.utopia.schedule.dao.CrmUGCSchoolDao;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.user.api.constants.AuthenticationSource;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.consumer.DeprecatedSchoolServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * UGC学校自动认证
 *
 * @author Jia HuanYin
 * @since 2015/7/21
 */
@Named
@ScheduledJobDefinition(
        jobName = "UGC学校自动认证任务",
        jobDescription = "将大数据收集并处理的UGC学校数据中，答案已确定的学校更新认证状态和认证来源",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 6 * * ?"
)
@ProgressTotalWork(100)
public class AutoUGCSchoolAuthJob extends ScheduledJobWithJournalSupport {

    private static final int LIMIT = 1000;
    private static final List<Integer> TRIGGER_TYPES = Arrays.asList(3, 4, 5);

    @Inject private RaikouSystem raikouSystem;
    @Inject private CrmUGCSchoolDao crmUGCSchoolDao;

    @Deprecated
    @Inject private DeprecatedSchoolServiceClient deprecatedSchoolServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        long total = 0;
        long success = 0;
        long failure = 0;
        int page = 0;
        List<CrmUGCSchool> ugcSchools;
        while (true) {
            int skip = page * LIMIT;
            ugcSchools = crmUGCSchoolDao.findTriggerTypeIn(TRIGGER_TYPES, LIMIT, skip);
            if (CollectionUtils.isEmpty(ugcSchools)) {
                break;
            }
            for (CrmUGCSchool ugcSchool : ugcSchools) {
                if (authSchool(ugcSchool.getSchoolId())) {
                    success++;
                } else {
                    failure++;
                }
            }
            int size = ugcSchools.size();
            total += size;
            if (size < LIMIT) {
                break;
            }
            ugcSchools.clear();
            page++;
            Thread.sleep(10);
        }

        logger.info("AutoUGCSchoolAuthJob Finished with total = {}, success = {}, failure = {}", total, success, failure);
        progressMonitor.done();
    }

    private boolean authSchool(Long schoolId) {
        School school = raikouSystem.loadSchoolIncludeDisabled(schoolId);
        if (school == null || school.isDisabledTrue()) { // 被删除的学校不做处理
            return false;
        }
        AuthenticationState state = AuthenticationState.safeParse(school.getAuthenticationState());
        AuthenticationSource source = school.getAuthenticationSource();
        if (AuthenticationState.SUCCESS == state && AuthenticationSource.UGC_AUTO == source) {
            return true;
        }
        school.setAuthenticationState(AuthenticationState.SUCCESS.getState());
        school.setAuthenticationSource(AuthenticationSource.UGC_AUTO);
        try {
            return deprecatedSchoolServiceClient.getRemoteReference().upsertSchool(school, null).isSuccess();
        } catch (Exception ex) {
            logger.error("Failed to update school", ex);
            return false;
        }
    }
}
