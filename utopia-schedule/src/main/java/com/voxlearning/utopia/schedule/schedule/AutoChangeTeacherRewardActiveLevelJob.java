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

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.schedule.support.userquery.UserDataQuerier;
import com.voxlearning.utopia.schedule.support.userquery.UserDataQueryStatement;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Auto change teacher reward active level job implementation.
 *
 * @author Xiaopeng Yang
 * @since Sep 22, 2014
 */
@Named
@ScheduledJobDefinition(
        jobName = "每月第一天更改老师上月奖品中心活跃等级",
        jobDescription = "每月第一天更改老师上月奖品中心活跃等级，每月第一天03：00运行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 3 1 * ?"
)
@ProgressTotalWork(100)
public class AutoChangeTeacherRewardActiveLevelJob extends ScheduledJobWithJournalSupport {

    @Inject private RaikouSDK raikouSDK;

    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserServiceClient userServiceClient;
    @Inject private UserDataQuerier userDataQuerier;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        //查询所有认证老师
        UserDataQueryStatement statement = UserDataQueryStatement.build()
                .queryFileds("ID")
                .where("USER_TYPE=1 AND AUTHENTICATION_STATE=1 AND DISABLED=0");
        List<Long> teacherIdList = userDataQuerier.query(statement).stream().map(e -> SafeConverter.toLong(e.get("ID"))).collect(Collectors.toList());
//        String queryTeacher = "SELECT ID FROM UCT_USER WHERE USER_TYPE=1 AND AUTHENTICATION_STATE=1 AND DISABLED=0";
//        List<Long> teacherIdList = utopiaSql.withSql(queryTeacher).queryColumnValues(Long.class);
        jobJournalLogger.log("Total {} authentication teachers found", teacherIdList.size());
        progressMonitor.worked(5);
        if (CollectionUtils.isEmpty(teacherIdList)) {
            return;
        }
        ISimpleProgressMonitor monitor = progressMonitor.subTask(95, teacherIdList.size());
        // 这里逻辑进行变更，对于老师奖品中心活跃等级 不分123级了，也没有折扣了， 只保留是否冻结的逻辑， 如果老师上个月没有检查过作业， 直接冻结，也就是把活跃等级设成0 20160325
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
        // 中学的不处理
        if (!teacher.isPrimarySchool() && !teacher.isInfantTeacher()) {
            return;
        }
        //上个月第一天 到目前为止 检查的作业
        Date beginDate = MonthRange.current().previous().getStartDate();
        int monthHomeworkCount = 0;
        List<GroupTeacherTuple> groupTeacherRefs = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByTeacherId(teacherId);
        if (CollectionUtils.isNotEmpty(groupTeacherRefs)) {
            List<Long> groupIds = groupTeacherRefs.stream().map(GroupTeacherTuple::getGroupId).collect(Collectors.toList());
            List<NewHomework.Location> locations = newHomeworkLoaderClient.loadNewHomeworksByClazzGroupIds(groupIds, teacher.getSubject()).values().stream().flatMap(Collection::stream).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(locations)) {
                // 过滤
                locations = locations.stream().filter(NewHomework.Location::isChecked)
                        .filter(l -> l.getCheckedTime() != 0 && new Date(l.getCheckedTime()).after(beginDate))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(locations)) {
                    monthHomeworkCount = locations.size();
                }
            }
        }

        int rewardActiveLevel = 0;
        if (monthHomeworkCount > 0) {
            rewardActiveLevel = 1;
        }
        //update user and cache
        userServiceClient.updateRewardActiveLevel(teacherId, rewardActiveLevel);
    }
}
