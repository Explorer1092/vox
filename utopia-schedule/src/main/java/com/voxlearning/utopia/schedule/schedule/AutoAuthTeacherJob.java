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
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.schedule.dao.PremiseAuthenticateTeacherPersistence;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.business.consumer.CertificationManagementClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrgLoaderClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.UserAuthQueryServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserTagQueueClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * Created by Summer Yang on 2016/6/16.
 */
@Named
@ScheduledJobDefinition(
        jobName = "自动认证老师任务（新）",
        jobDescription = "自动认证老师，每天21:40运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 40 21 * * ?"
)
@ProgressTotalWork(100)
public class AutoAuthTeacherJob extends ScheduledJobWithJournalSupport {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject private EmailServiceClient emailServiceClient;

    @Inject private CertificationManagementClient certificationManagementClient;
    @Inject private UserAuthQueryServiceClient userAuthQueryServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserServiceClient userServiceClient;

    @ImportService(interfaceClass = CrmSummaryService.class) private CrmSummaryService crmSummaryService;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        /* 自动认证，调用大数据接口获取所有当天分数满足的老师ID， 然后校验前置条件
         * 符合前端认证条件 (本人绑定手机，8人3次完成作业，3人以上绑定自己或者家长手机)
         */
        // 确定数据是否到位
        String date = DateUtils.dateToString(DayRange.current().previous().getStartDate(), "yyyy-MM-dd");
        // 校验大数据提供的数据是否已经就位
        boolean hasData = userAuthQueryServiceClient.isExistTodayData(date);
        if (!hasData) {
            logger.error("自动认证老师任务执行结束，大数据提供的数据未就位， 日期：" + date);
            progressMonitor.done();
            return;
        }
        // 调用大数据接口获取今天的所有满足条件但是未认证的老师ID
        MapMessage message = userAuthQueryServiceClient.getAuthTeacherCandidatesNewV2(date);
        if (!message.isSuccess()) {
            logger.error("获取待认证老师数据失败，{}", message.getInfo());
            progressMonitor.done();
            return;
        }
        Map<String, Double> teacherScoreMap = (Map<String, Double>) message.get("data");
        if (MapUtils.isEmpty(teacherScoreMap)) {
            logger.error("获取待认证老师列表不存在，date:{}，score:{} ", date);
            progressMonitor.done();
            return;
        }

        ISimpleProgressMonitor monitor = progressMonitor.subTask(98, teacherScoreMap.size());
        logger.info("开始检查老师是否符合前置认证条件, 共{}条", teacherScoreMap.size());
        final AtomicLong authenticatedTeacherCount = new AtomicLong(0);
        for (Map.Entry<String, Double> entry : teacherScoreMap.entrySet()) {
            Long teacherId = SafeConverter.toLong(entry.getKey());
            try {
                dealWithTeacher(teacherId, authenticatedTeacherCount, entry.getValue());
            } catch (Exception ex) {
                logger.warn("Failed to deal with teacher '{}' authentication, ignore", teacherId, ex);
            } finally {
                monitor.worked(1);
            }
        }

        String msg = StringUtils.formatMessage("共验证{}位老师，认证成功{}位", teacherScoreMap.size(), authenticatedTeacherCount.get());
        logger.info(msg);

        if (RuntimeMode.isProduction()) {
            //发邮件
            Map<String, Object> content = new HashMap<>();
            content.put("allCount", teacherScoreMap.size());
            content.put("successCount", authenticatedTeacherCount.get());
            emailServiceClient.createTemplateEmail(EmailTemplate.autoauthenticateteacher)
                    .to("xiaojuan.jia@17zuoye.com;lei.wang@17zuoye.com")
                    .cc("yizhou.zhang@17zuoye.com;xiaopeng.yang@17zuoye.com;zhilong.hu@17zuoye.com;yan.liu@17zuoye.com")
                    .subject("自动认证老师任务执行结果（新）")
                    .content(content).send();
        }
        progressMonitor.done();
    }

    private void dealWithTeacher(Long teacherId,
                                 AtomicLong authenticatedTeacherCount,
                                 Double teacherScore) throws Exception {
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        // 如果已经认证成功， 则继续
        if (teacher == null || teacher.fetchCertificationState() == AuthenticationState.SUCCESS) {
            return;
        }
        // 姓名过滤
        if (StringUtils.isBlank(teacher.fetchRealname())) {
            return;
        }
        // 判断老师是否绑定手机
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        if (mainTeacherId == null) {
            // 主账号 判断自己是否绑定手机
            UserAuthentication authentication = userLoaderClient.loadUserAuthentication(teacherId);
            if (!authentication.isMobileAuthenticated()) {
                return;
            }
        } else {
            // 副账号 判断主账号是否绑定手机
            UserAuthentication authentication = userLoaderClient.loadUserAuthentication(mainTeacherId);
            if (!authentication.isMobileAuthenticated()) {
                return;
            }
        }

        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        if (null == school || school.isTraingingSchool() || school.isInfantSchool()) {
            return;
        }

        // 学前/培训学校的老师不进行自动认证
        if (school.isTraingingSchool() || school.isInfantSchool() || Objects.equals(school.getAuthenticationState(), AuthenticationState.FAILURE.getState())) {
            return;
        }

        // 3人以上绑定家长手机或者自己的手机   （最新调整 和平台统一）
        if (!certificationManagementClient.getRemoteReference().hasEnoughStudentsBindParentMobileOrBindSelfMobile(teacherId))
            return;

        // 满足条件 认证  任意一个账号认证 所有关联的全部认证
        Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
        for (Long tid : teacherIds) {
            // 当前认证状态
            Teacher currentTeacher = teacherMap.get(tid);
            if (currentTeacher == null) {
                continue;
            }
            // 认证老师
            certificationManagementClient.getRemoteReference().changeUserAuthenticationState(tid, AuthenticationState.SUCCESS, 91090L, "自动认证");
            // 记录日志
            String operation = "system-更新用户认证状态,操作前状态：" + currentTeacher.fetchCertificationState().toString() + "，新状态：SUCCESS";
            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(tid);
            userServiceRecord.setOperatorId("自动认证任务");
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("更新认证状态");
            userServiceRecord.setComments(operation + "；说明[自动为符合认证条件的老师认证]");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
            // 增加认证成功数量
            authenticatedTeacherCount.incrementAndGet();
            // 从记录表中删除
            logger.info("老师（{}）自动认证成功", tid);
        }

    }

}
