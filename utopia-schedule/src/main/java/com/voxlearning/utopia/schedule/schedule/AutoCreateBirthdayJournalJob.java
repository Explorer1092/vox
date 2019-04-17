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
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.schedule.support.userquery.UserDataQuerier;
import com.voxlearning.utopia.schedule.support.userquery.UserDataQueryStatement;
import com.voxlearning.utopia.service.user.api.constants.SystemRobot;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "生日任务",
        jobDescription = "为过生日的学生创建班级动态，每天6:15运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 15 6 * * ?")
@ProgressTotalWork(100)
public class AutoCreateBirthdayJournalJob extends ScheduledJobWithJournalSupport {

    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;

    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserDataQuerier userDataQuerier;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        // 3天后过生日的同学
        String date = DateUtils.dateToString(DateUtils.calculateDateDay(new Date(), 3), "M-d");
        logger.info("TARGET DATE: " + date);

        String[] strs = StringUtils.split(date, "-");
        String month = strs[0];
        String day = strs[1];
//        List<User> users = userManagementClient
//                .findUserListForAutoCreateBirthdayJournalJob(month, day);

        UserDataQueryStatement statement = UserDataQueryStatement.build()
                .queryFileds("ID", "REALNAME")
                .where("USER_TYPE=3 AND MONTH=? AND DAY=?")
                .params(month, day);
        List<Map<String, Object>> users = userDataQuerier.query(statement);
//
//
//        List<User> users = studentLoaderClient.loadStudentsForBirthday(month, day);
//        if (users.isEmpty()) {
//            return;
//        }
        progressMonitor.worked(20);
        ISimpleProgressMonitor monitor = progressMonitor.subTask(80, users.size());
//        for (User user : users) {
        for (Map<String, Object> user : users) {
            long userId = SafeConverter.toLong(user.get("ID"));
            String realname = SafeConverter.toString(user.get("REALNAME"));
            try {
                Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(userId);
                if (clazz == null) {
                    continue;
                }
                String content = "<span class=\"w-green\"> " + realname + " </span>3天后（" + date + "）过生日，送份小礼物表达心意吧~";
                List<GroupMapper> groups = groupLoaderClient.loadStudentGroups(userId, false);
                //TODO currently send to one group for the student
                //TODO or one message will be saw multi-times for other students
                if (groups.size() > 0) {
                    zoneQueueServiceClient.createClazzJournal(clazz.getId())
                            .withUser(SystemRobot.getInstance().getId())
                            .withUser(SystemRobot.getInstance().fetchUserType())
                            .withClazzJournalType(ClazzJournalType.BIRTHDAY)
                            .withClazzJournalCategory(ClazzJournalCategory.MISC)
                            .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content, "studentId", userId)))
                            .withGroup(groups.get(0).getId())
                            .commit();
                }
            } finally {
                monitor.worked(1);
            }
        }
        progressMonitor.done();
    }
}
