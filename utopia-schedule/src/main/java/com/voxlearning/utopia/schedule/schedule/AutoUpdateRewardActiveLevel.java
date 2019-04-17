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

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.schedule.support.userquery.UserDataQuerier;
import com.voxlearning.utopia.schedule.support.userquery.UserDataQueryStatement;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/4/1.
 */
@Named
@ScheduledJobDefinition(
        jobName = "更改奖品中心中学老师冻结数据",
        jobDescription = "更改奖品中心中学老师冻结数据，手动运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 0 1 * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoUpdateRewardActiveLevel extends ScheduledJobWithJournalSupport {

    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserServiceClient userServiceClient;
    @Inject private UserDataQuerier userDataQuerier;


    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
//        String queryTeacher = "SELECT ID FROM UCT_USER WHERE USER_TYPE=1 AND AUTHENTICATION_STATE=1 AND DISABLED=0";
//        List<Long> teacherIdList = utopiaSql.withSql(queryTeacher).queryColumnValues(Long.class);
        UserDataQueryStatement statement = UserDataQueryStatement.build()
                .queryFileds("ID")
                .where("USER_TYPE=1 AND AUTHENTICATION_STATE=1 AND DISABLED=0");
        List<Long> teacherIdList = userDataQuerier.query(statement).stream().map(e -> SafeConverter.toLong(e.get("ID"))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(teacherIdList)) {
            return;
        }
        progressMonitor.worked(10);
        ISimpleProgressMonitor monitor = progressMonitor.subTask(90, teacherIdList.size());
        for (Long teacherId : teacherIdList) {
            try {
                dealWithTeacher(teacherId);
            } catch (Exception ex) {
                jobJournalLogger.log("Failed to deal with teacher '{}' reward active level, ignore", teacherId, ex);
            } finally {
                monitor.worked(1);
            }
        }
        progressMonitor.done();
    }

    private void dealWithTeacher(Long teacherId) {
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return;
        }
        if (teacher.isPrimarySchool() || teacher.isInfantTeacher()) {
            return;
        }
        //update user and cache
        userServiceClient.updateRewardActiveLevel(teacherId, 1);
    }
}
