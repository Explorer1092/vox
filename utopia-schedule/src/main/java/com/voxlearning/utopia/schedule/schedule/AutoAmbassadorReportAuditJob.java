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

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.AmbassadorReportStatus;
import com.voxlearning.utopia.api.constant.AmbassadorReportType;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorReportInfo;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorReportStudentFeedback;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.business.consumer.BusinessTeacherServiceClient;
import com.voxlearning.utopia.service.business.consumer.MiscServiceClient;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2015/12/14.
 */
@Named
@ScheduledJobDefinition(
        jobName = "自动审核校园大使举报认证老师任务",
        jobDescription = "每天02:30运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 30 2 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoAmbassadorReportAuditJob extends ScheduledJobWithJournalSupport {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;

    @Inject private BusinessTeacherServiceClient businessTeacherServiceClient;
    @Inject private InternalAmbassadorReportInfoDao internalAmbassadorReportInfoDao;
    @Inject private MiscServiceClient miscServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserServiceClient userServiceClient;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;

    private UtopiaSql utopiaSqlAdmin;

    @ImportService(interfaceClass = CrmSummaryService.class) private CrmSummaryService crmSummaryService;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSqlAdmin = utopiaSqlFactory.getUtopiaSql("admin");
    }

    @Named("autoAmbassadorReportAuditJob.internalAmbassadorReportInfoDao")
    @CacheBean(type = AmbassadorReportInfo.class)
    public static class InternalAmbassadorReportInfoDao extends StaticCacheDimensionDocumentJdbcDao<AmbassadorReportInfo, Long> {
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        // 获取所有待处理数据
        Date beginDate = DateUtils.calculateDateDay(new Date(), -3);
        Criteria criteria = Criteria.where("CREATE_DATETIME").lte(beginDate)
                .and("STATUS").is(AmbassadorReportStatus.REPORTING)
                .and("TYPE").ne(1);
        List<AmbassadorReportInfo> infos = internalAmbassadorReportInfoDao.query(Query.query(criteria));
        if (CollectionUtils.isEmpty(infos)) {
            jobJournalLogger.log("没有数据");
            return;
        }
        progressMonitor.worked(5);
        jobJournalLogger.log("共有" + infos.size() + "条数据待处理");
        ISimpleProgressMonitor monitor = progressMonitor.subTask(95, infos.size());
        for (AmbassadorReportInfo info : infos) {
            try {
                dealReportInfo(info);
            } catch (Exception ex) {
                jobJournalLogger.log("deal Info {} error, {}", info.getId(), ex.getMessage());
            } finally {
                monitor.worked(1);
            }
        }
        progressMonitor.done();
    }

    private void dealReportInfo(AmbassadorReportInfo info) {
        // 学生反馈信息
        List<AmbassadorReportStudentFeedback> feedbacks = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorReportStudentFeedbacks(info.getTeacherId());
        // 取消认证
        if (info.getType() == AmbassadorReportType.APPLY_CANCLE_TEACHER_AUTH.getType()) {
            // 虚假老师
            if ("虚假老师".equals(info.getReason())) {
                CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(info.getTeacherId());
                if (teacherSummary != null && SafeConverter.toBoolean(teacherSummary.getFakeTeacher()) &&
                        CrmTeacherFakeValidationType.MANUAL_VALIDATION.getName().equals(teacherSummary.getValidationType())) {
                    cancelAuth(info, false, "老师已是手动判假老师");
                    return;
                }
            } else if ("转校/转校区".equals(info.getReason())) {
                School teacherSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                        .loadTeacherSchool(info.getTeacherId())
                        .getUninterruptibly();
                School ambassadorSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                        .loadTeacherSchool(info.getReportId())
                        .getUninterruptibly();
                if (teacherSchool != null && ambassadorSchool != null && !Objects.equals(teacherSchool.getId(), ambassadorSchool.getId())) {
                    info.setComment("老师已转校");
                    info.setStatus(AmbassadorReportStatus.NO_CANCEL_AUTH);
                    info.setUpdateDatetime(new Date());
                    ambassadorServiceClient.getAmbassadorService().$replaceAmbassadorReportInfo(info);
                    return;
                }
            } else if ("同校换科目".equals(info.getReason())) {
                Subject teacherSubject = userLoaderClient.loadUserSubject(info.getTeacherId());
                Subject ambassadorSubject = userLoaderClient.loadUserSubject(info.getReportId());
                if (teacherSubject != null && ambassadorSubject != null && teacherSubject != ambassadorSubject) {
                    info.setComment("老师已换科目");
                    info.setStatus(AmbassadorReportStatus.NO_CANCEL_AUTH);
                    info.setUpdateDatetime(new Date());
                    ambassadorServiceClient.getAmbassadorService().$replaceAmbassadorReportInfo(info);
                    return;
                }
            }
            if (CollectionUtils.isNotEmpty(feedbacks)) {
                feedbacks = feedbacks.stream().filter(AmbassadorReportStudentFeedback::getConfirm).collect(Collectors.toList());
                if (feedbacks.size() >= 5) {
                    info.setComment("有足够的学生反馈确认");
                    info.setStatus(AmbassadorReportStatus.NO_CANCEL_AUTH);
                    info.setUpdateDatetime(new Date());
                    ambassadorServiceClient.getAmbassadorService().$replaceAmbassadorReportInfo(info);
                } else {
                    cancelAuth(info, true, "没有足够的学生反馈");
                }
            } else {
                cancelAuth(info, true, "没有足够的学生反馈");
            }
        } else if (info.getType() == AmbassadorReportType.APPLY_PENDING_TEACHER.getType()) {
            // 暂停老师
            if (CollectionUtils.isNotEmpty(feedbacks)) {
                feedbacks = feedbacks.stream().filter(AmbassadorReportStudentFeedback::getConfirm).collect(Collectors.toList());
                if (feedbacks.size() >= 5) {
                    info.setComment("有足够的学生反馈确认");
                    info.setStatus(AmbassadorReportStatus.NO_PENDING);
                    info.setUpdateDatetime(new Date());
                    ambassadorServiceClient.getAmbassadorService().$replaceAmbassadorReportInfo(info);
                } else {
                    pendingTeacher(info);
                }
            } else {
                pendingTeacher(info);
            }
        }
    }

    private void pendingTeacher(AmbassadorReportInfo info) {
//        Teacher teacher = teacherLoaderClient.loadTeacher(info.getTeacherId());
//        if (teacher != null) {
//            userServiceClient.updatePending(info.getTeacherId(), 1);
//            Date currentDatetime = new Date();
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
//            utopiaSqlAdmin.withSql("INSERT INTO ADMIN_CUSTOMER_SERVICE_RECORD (USER_ID, ADMIN_USER_NAME, RECORD_TYPE, QUESTION_DESC, OPERATION, CREATE_DATETIME, UPDATE_DATETIME) " +
//                    "VALUES ( :teacherId, :adminName, :recordType, :questionDesc, :operation, :createDatetime, :updateDatetime)")
//                    .useParams(MiscUtils.map().add("teacherId", info.getTeacherId())
//                            .add("adminName", "系统自动")
//                            .add("recordType", "老师操作")
//                            .add("questionDesc", "校园大使举报暂停老师")
//                            .add("operation", "system-暂停老师，原因：" + info.getReason())
//                            .add("createDatetime", sdf.format(currentDatetime))
//                            .add("updateDatetime", sdf.format(currentDatetime)))
//                    .executeUpdate();
//        }
        info.setStatus(AmbassadorReportStatus.PENDING);
        info.setUpdateDatetime(new Date());
        info.setComment("系统暂停");
        ambassadorServiceClient.getAmbassadorService().$replaceAmbassadorReportInfo(info);
    }

    private void cancelAuth(AmbassadorReportInfo info, boolean fakeFlag, String comment) {
        Teacher teacher = teacherLoaderClient.loadTeacher(info.getTeacherId());
        if (teacher != null && teacher.fetchCertificationState() == AuthenticationState.SUCCESS) {
            businessTeacherServiceClient.changeUserAuthenticationState(info.getTeacherId(), AuthenticationState.FAILURE, 91090L, "系统取消");
            Date currentDatetime = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            utopiaSqlAdmin.withSql("INSERT INTO ADMIN_CUSTOMER_SERVICE_RECORD (USER_ID, ADMIN_USER_NAME, RECORD_TYPE, QUESTION_DESC, OPERATION, CREATE_DATETIME, UPDATE_DATETIME) " +
                    "VALUES ( :teacherId, :adminName, :recordType, :questionDesc, :operation, :createDatetime, :updateDatetime)")
                    .useParams(MiscUtils.map().add("teacherId", info.getTeacherId())
                            .add("adminName", "系统自动")
                            .add("recordType", "老师操作")
                            .add("questionDesc", "校园大使举报取消老师认证")
                            .add("operation", "system-更新用户认证状态,操作前状态：SUCCESS，新状态：FAILURE，原因：" + info.getReason())
                            .add("createDatetime", sdf.format(currentDatetime))
                            .add("updateDatetime", sdf.format(currentDatetime)))
                    .executeUpdate();
            UserAuthentication authentication = userLoaderClient.loadUserAuthentication(info.getTeacherId());
            if (authentication != null && authentication.isMobileAuthenticated()) {
                userSmsServiceClient.buildSms().to(authentication)
                        .type(SmsType.AMBASSADOR_REPORT_NOTICE)
                        .content("经校园大使反馈，您已被取消认证老师资格。如有疑问，请联系客服400-160-1717。")
                        .send();
            }
        }
        info.setComment(comment);
        info.setStatus(AmbassadorReportStatus.CANCEL_AUTH);
        info.setUpdateDatetime(new Date());
        info = ambassadorServiceClient.getAmbassadorService().$replaceAmbassadorReportInfo(info);
        if ("虚假老师".equals(info.getReason()) && fakeFlag) {
            MapMessage message = crmSummaryService
                    .updateTeacherFakeType(info.getTeacherId(), CrmTeacherFakeValidationType.MANUAL_VALIDATION, "校园大使举报老师认证（虚假老师）");
            if (message.isSuccess()) {
                // 发送申诉消息
                miscServiceClient.sendFakeAppealMessage(info.getTeacherId());
            }
        }
    }
}
