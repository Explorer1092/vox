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
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.mutable.MutableBoolean;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.schedule.support.userquery.UserDataQuerier;
import com.voxlearning.utopia.schedule.support.userquery.UserDataQueryStatement;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 每天早上7点30分发送该消息，统计前天注册后创建班级、但少于2学生登录的老师
 *
 * @author RuiBao
 * @version 0.1
 * @since 7/23/2015
 */
@Named
@ScheduledJobDefinition(
        jobName = "教学生加入一起作业",
        jobDescription = "每天早上7点30分运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 30 7 * * ?"
)
@ProgressTotalWork(100)
public class AutoFewStudentLoginNoticeJob extends ScheduledJobWithJournalSupport {

    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserLoginServiceClient userLoginServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    @Inject private WechatLoaderClient wechatLoaderClient;
    @Inject private WechatServiceClient wechatServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private UserDataQuerier userDataQuerier;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        DayRange range = DayRange.current().previous().previous();
        Date start = range.getStartDate();
        Date end = range.getEndDate();
        if (parameters.containsKey("date")) {
            start = DateUtils.stringToDate(ConversionUtils.toString(parameters.get("date")) + " 00:00:00");
            end = DateUtils.stringToDate(ConversionUtils.toString(parameters.get("date")) + " 23:59:59");
        }
        logger.info("Date range {} ~ {}", DateUtils.dateToString(start), DateUtils.dateToString(end));

        // 查询前天注册的教师Id
        List<Long> teacherIds = new ArrayList<>();
        Map<Long, String> names = new HashMap<>();

//        String sql = "SELECT ID, REALNAME FROM UCT_USER WHERE USER_TYPE=1 AND CREATETIME>=:start AND CREATETIME<=:end AND DISABLED=FALSE";
//        utopiaSql.withSql(sql).useParams(MiscUtils.map("start", start, "end", end)).queryAll()
        UserDataQueryStatement statement = UserDataQueryStatement.build()
                .queryFileds("ID", "REALNAME")
                .where("USER_TYPE=1 AND CREATETIME>=? AND CREATETIME<=? AND DISABLED=FALSE")
                .params(start, end);
        userDataQuerier.query(statement)
                .forEach(e -> {
                    Long tid = ConversionUtils.toLong(e.get("ID"));
                    String name = ConversionUtils.toString(e.get("REALNAME"));
                    if (tid != 0) {
                        teacherIds.add(tid);
                        names.put(tid, name);
                    }
                });

        if (CollectionUtils.isEmpty(teacherIds)) return;
        logger.info("Total {} teacher found.", teacherIds.size());

        progressMonitor.worked(20);

        ISimpleProgressMonitor monitor = progressMonitor.subTask(80, teacherIds.size());

        List<List<Long>> sources = CollectionUtils.splitList(teacherIds, 2);
        int threadCount = sources.size();
        logger.info("Split teacher ids into {} threads", threadCount);

        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (List<Long> source : sources) {
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    handleTeacher(source, names, monitor);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(3, TimeUnit.DAYS);
        } catch (InterruptedException ignored) {
            logger.warn(ignored.getMessage(), ignored);
        }

        progressMonitor.done();
    }

    private void handleTeacher(List<Long> teacherIds, Map<Long, String> names, ISimpleProgressMonitor monitor) {
        Map<Long, List<GroupMapper>> teacher_group_map = groupLoaderClient.loadTeacherGroupsByTeacherId(teacherIds, false);
        Map<Long, UserAuthentication> teacher_mobile_map = userLoaderClient.loadUserAuthentications(teacherIds);
        Map<Long, List<UserWechatRef>> teacher_wechat_map = wechatLoaderClient.loadUserWechatRefs(teacherIds, WechatType.TEACHER);

        for (Long teacherId : teacherIds) {
            try {
                User user = userLoaderClient.loadUser(teacherId);
                //导入了快乐学数据,避免向这些用户发送信息
                if (user != null && (Objects.equals(user.getWebSource(), UserWebSource.happy_study.toString()) || Objects.equals(user.getWebSource(), UserWebSource.happy_job.toString()))) {
                    continue;
                }
                if (user != null && user.isTeacher()) {
                    Teacher teacher = teacherLoaderClient.loadTeacher(user.getId());
                    if (teacher != null && teacher.isKLXTeacher()) {
                        continue;
                    }
                }

                List<GroupMapper> groups = teacher_group_map.get(teacherId);
                if (CollectionUtils.isEmpty(groups)) continue;
                Set<Long> groupIds = groups.stream().map(GroupMapper::getId).collect(Collectors.toSet());

                List<User> students = studentLoaderClient.loadGroupStudents(groupIds)
                        .values().stream()
                        .filter(CollectionUtils::isNotEmpty)
                        .flatMap(List::stream)
                        .filter(e -> e != null)
                        .collect(Collectors.toList());
//                if (userLoaderClient.validateLoginUserCount(students, 2)) {
                if (userLoginServiceClient.validateLoginUserCount(
                        students.stream().map(User::getId).collect(Collectors.toList()), 2)) {
                    continue;
                }
                // 发送微信
                final MutableBoolean wechatNoticeSended = new MutableBoolean(false);
                List<UserWechatRef> refs = teacher_wechat_map.get(teacherId);
                if (CollectionUtils.isNotEmpty(refs)) {
                    refs.stream()
                            .filter(source -> !source.isDisabledTrue())
                            .forEach(e -> {
                                wechatServiceClient.processWechatNotice(WechatNoticeProcessorType.MoreStudentNotice,
                                        teacherId, e.getOpenId(), MiscUtils.m("teacherId", teacherId));
                                wechatNoticeSended.setTrue();
                            });
                }
                // 如果没有微信，看看能不能发个短信
                if (wechatNoticeSended.isFalse()) {
                    UserAuthentication ua = teacher_mobile_map.get(teacherId);
                    if (ua == null || !ua.isMobileAuthenticated()) continue;
                    String name = StringUtils.defaultString(names.get(teacherId));
                    userSmsServiceClient.buildSms().to(ua)
                            .content(name + "老师您好，您的学生还没加入一起作业，把您的手机号告诉学生，让学生注册www.17zuoye.com后才可以完成作业")
                            .type(SmsType.STU_LOGIN_COUNT_NOT_ENOUGH)
                            .send();
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        // ignore;
                    }
                }
            } finally {
                monitor.worked(1);
            }
        }
    }
}
