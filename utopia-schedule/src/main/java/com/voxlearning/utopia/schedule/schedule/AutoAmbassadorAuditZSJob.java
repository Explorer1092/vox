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
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.AmbassadorLevel;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorLevelDetail;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorLevelHistory;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.business.consumer.AmbassadorManagementClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Summer Yang
 * @since 2015/12/8
 */
@Named
@ScheduledJobDefinition(
        jobName = "自动审核校园大使-正式大使",
        jobDescription = "每月1日00:10执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 10 0 1 * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoAmbassadorAuditZSJob extends ScheduledJobWithJournalSupport {

    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private AmbassadorManagementClient ambassadorManagementClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;

    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    private Integer loadAmbassadorTotalScore(Long ambassadorId, Date beginDate) {
        String sql = "SELECT SUM(SCORE) AS SCORE FROM VOX_AMBASSADOR_SCORE_HISTORY WHERE AMBASSADOR_ID=? AND CREATE_DATETIME>=? AND DISABLED=FALSE";
        return utopiaSql.withSql(sql).useParamsArgs(ambassadorId, beginDate).queryValue(Integer.class);
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        // 1 2月不执行考核
        int month = MonthRange.current().previous().getMonth();
        if (month == 1 || month == 2 || month == 7 || month == 8) {
            jobJournalLogger.log("寒暑假期间不执行。");
            return;
        }
        // 获取所有正式大使
        String sxsql = "SELECT r.AMBASSADOR_ID FROM VOX_AMBASSADOR_SCHOOL_REF r, " +
                "VOX_AMBASSADOR_LEVEL_DETAIL d WHERE r.AMBASSADOR_ID=d.AMBASSADOR_ID AND d.`LEVEL`<>'SHI_XI' AND d.DISABLED = FALSE AND r.DISABLED=FALSE;";
        List<Long> zsAmbassadorIds = utopiaSql.withSql(sxsql).queryColumnValues(Long.class);
        if (CollectionUtils.isEmpty(zsAmbassadorIds)) {
            jobJournalLogger.log("没有需要考核的大使！");
            return;
        }
        progressMonitor.worked(5);

        jobJournalLogger.log("共有" + zsAmbassadorIds.size() + "个正式大使需要进行考核处理");
        ISimpleProgressMonitor monitor = progressMonitor.subTask(95, zsAmbassadorIds.size());
        for (Long ambassadorId : zsAmbassadorIds) {
            try {
                dealAmbassador(ambassadorId);
            } catch (Exception ex) {
                jobJournalLogger.log("deal Ambassador {} error, {}", ambassadorId, ex.getMessage());
            } finally {
                monitor.worked(1);
            }
        }
        progressMonitor.done();
    }

    private void dealAmbassador(Long ambassadorId) {
        TeacherDetail ambassador = teacherLoaderClient.loadTeacherDetail(ambassadorId);
        if (ambassador == null || !ambassador.isSchoolAmbassador()) {
            throw new RuntimeException("ambassador is null or is not ambassador");
        }
        AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorSchoolRefs(ambassadorId)
                .stream().findFirst().orElse(null);
        if (ref == null) {
            throw new RuntimeException("ambassador ref is null");
        }
        // 判断大使是否铜牌 铜牌大使 上个月15号之后成为的铜牌大使 不做任务考核
        AmbassadorLevelDetail levelDetail = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorLevelDetail(ambassadorId);
        if (levelDetail == null) {
            throw new RuntimeException("ambassador level is null");
        }
        MonthRange monthRange = MonthRange.current().previous();
        if (levelDetail.getLevel() == AmbassadorLevel.TONG_PAI) {
            // 上月的15号
            String lastMonth = DateUtils.dateToString(monthRange.getEndDate(), "yyyy-MM") + "-15 00:00:00";
            Date lastHalf = DateUtils.stringToDate(lastMonth);
            if (levelDetail.getBornDate() != null && levelDetail.getBornDate().after(lastHalf)) {
                // 上月的15号之后成为的正式大使 不考察 只发提醒
                String msg = "大使您好，新月份经验奖励正式开始！请打满鸡血，得经验拿园丁豆大礼包！（请登录电脑端-『校园大使』页面查看规则）";
                String pcMsg = "大使您好，新月份经验奖励正式开始！请打满鸡血，得经验拿园丁豆大礼包！<a href='/ambassador/center.vpage'>『查看规则』</a>";
                sendRemind(ambassador, msg, pcMsg);
                return;
            }
        }
        // 根据大使的上个月经验值来确定本月的等级
        Integer totalScore = loadAmbassadorTotalScore(ambassadorId, monthRange.getStartDate());
        int score = totalScore == null ? 0 : totalScore;
        if (score >= 200) {
            // 金牌大使 设置等级并奖励
            AmbassadorLevel level = AmbassadorLevel.JIN_PAI;
            doReward(levelDetail, level, ambassador, 200, "金牌");
        } else if (score >= 100 && score < 200) {
            // 银牌 设置等级并奖励
            AmbassadorLevel level = AmbassadorLevel.YIN_PAI;
            doReward(levelDetail, level, ambassador, 100, "银牌");
        } else if (score >= 40 && score < 100) {
            // 铜牌大使 设置等级并奖励
            AmbassadorLevel level = AmbassadorLevel.TONG_PAI;
            doReward(levelDetail, level, ambassador, 30, "铜牌");
        } else if (score < 40) {
            // 看是否观察期
            if (levelDetail.getIsObservation()) {
                // 直接下任
                MapMessage message = ambassadorManagementClient.getRemoteReference().resignationAmbassador(ambassador);
                if (message.isSuccess()) {
                    // 给大使发通知
                    userPopupServiceClient.createPopup(ambassadorId).content("观察期内，您未能完成规定任务，目前您已变为普通老师。")
                            .type(PopupType.AMBASSADOR_NOTICE).category(PopupCategory.LOWER_RIGHT).create();
                } else {
                    throw new RuntimeException(message.getInfo());
                }
            } else {
                // 放入观察期
                levelDetail.setIsObservation(true);
                ambassadorServiceClient.getAmbassadorService().$replaceAmbassadorLevelDetail(levelDetail);
                // 给大使发通知
                String msg = "由于上个月您未达到规定最低经验值，本月已进入『大使观察期』，请您加油啦！（请登录电脑端-『校园大使』页面查看『观察期』规则）";
                String pcMsg = "由于上个月您未达到规定最低经验值，本月已进入『大使观察期』，请您加油啦！<a href='/ambassador/center.vpage'>查看规则</a>";
                sendRemind(ambassador, msg, pcMsg);
            }
        }

        // FIXME: 2016/3/30 任务相关暂时下线，用新的考核规则
//        // 过滤 同学科老师 本月1日之前认证的老师
//        List<Teacher> teachers = ambassadorManagementClient.getRemoteReference()
//                .loadCurrentSchoolSameSubjectAuthenticationTeachers(ambassador, monthRange.getStartDate());
//        // 过滤暂停的老师
//        teachers = teachers.stream().filter(t -> t.getPending() == null || t.getPending() != 1).collect(Collectors.toList());
//        List<Long> allTeacherIds = teachers.stream().map(Teacher::getId).collect(Collectors.toList());
//        Map<Long, Long> effectiveMap = userCacheClient.getPersistUserBehaviorCountManager()
//                .getUserBehaviorCounts(UserBehaviorType.TEACHER_MONTH_EFFECTIVE_HW_COUNT, allTeacherIds);
//        Map<String, Object> missionMap = ambassadorManagementClient.getRemoteReference()
//                .getMissionMap(effectiveMap, ambassadorId);
//        boolean mT = SafeConverter.toBoolean(missionMap.get("finishMission_T"));
//        boolean mY = SafeConverter.toBoolean(missionMap.get("finishMission_Y"));
//        boolean mJ = SafeConverter.toBoolean(missionMap.get("finishMission_J"));
//        // 判断是否完成任务
//        if (!mT && !mY && !mJ) {
//            // 全部没完成  查看是否观察期
//            if (levelDetail.getIsObservation()) {
//                // 直接下任
//                MapMessage message = ambassadorManagementClient.getRemoteReference().resignationAmbassador(ambassador);
//                if (message.isSuccess()) {
//                    // 给大使发通知
//                    popupServiceClient.createPopup(ambassadorId).content("观察期内，您未能完成规定任务，目前您已变为普通老师。")
//                            .type(PopupType.AMBASSADOR_NOTICE).category(PopupCategory.LOWER_RIGHT).create();
//                } else {
//                    throw new RuntimeException(message.getInfo());
//                }
//            } else {
//                // 放入观察期
//                levelDetail.setIsObservation(true);
//                levelDetail.setUpdateDatetime(new Date());
//                ambassadorLevelDetailPersistence.update(levelDetail.getId(), levelDetail);
//                // 给大使发通知
//                popupServiceClient.createPopup(ambassadorId).content("任职期内，您未能完成规定任务，本月您已进入观察期。")
//                        .type(PopupType.AMBASSADOR_NOTICE).category(PopupCategory.LOWER_RIGHT).create();
//            }
//        } else {
//            // 看看完成了哪个
//            AmbassadorLevel level;
//            if (mJ) {
//                // 金牌的
//                level = AmbassadorLevel.JIN_PAI;
//            } else if (mY) {
//                // 银牌的
//                level = AmbassadorLevel.YIN_PAI;
//            } else {
//                // 铜牌
//                level = AmbassadorLevel.TONG_PAI;
//            }
//            // 修改大使等级
//            levelDetail.setLevel(level);
//            levelDetail.setUpdateDatetime(new Date());
//            ambassadorLevelDetailPersistence.update(levelDetail.getId(), levelDetail);
//            // 记录成就
//            AmbassadorLevelHistory history = new AmbassadorLevelHistory();
//            history.setSchoolId(ambassador.getTeacherSchoolId());
//            history.setLevel(level);
//            history.setMonth(SafeConverter.toInt(DateUtils.dateToString(monthRange.getStartDate(), "yyyyMM")));
//            history.setAmbassadorId(ambassadorId);
//            ambassadorLevelHistoryPersistence.persist(history);
//        }


    }

    private void doReward(AmbassadorLevelDetail levelDetail, AmbassadorLevel level, TeacherDetail ambassador, int integralCount, String levelName) {
        levelDetail.setLevel(level);
        levelDetail.setIsObservation(false); // 如果有观察期的 直接设置为false
        ambassadorServiceClient.getAmbassadorService().$replaceAmbassadorLevelDetail(levelDetail);
        // 记录成就
        AmbassadorLevelHistory history = new AmbassadorLevelHistory();
        history.setSchoolId(ambassador.getTeacherSchoolId());
        history.setLevel(level);
        history.setMonth(SafeConverter.toInt(DateUtils.dateToString(MonthRange.current().previous().getStartDate(), "yyyyMM")));
        history.setAmbassadorId(ambassador.getId());
        ambassadorServiceClient.getAmbassadorService().$insertAmbassadorLevelHistory(history);
        // 奖励
        IntegralHistory integralHistory = new IntegralHistory(ambassador.getId(), IntegralType.AMBASSADOR_LEVEL_MONTH_REWARD, integralCount * 10);
        integralHistory.setComment(levelName + "大使园丁豆奖励");
        if (userIntegralService.changeIntegral(integralHistory).isSuccess()) {
            // 发通知
            String msg = "恭喜您上月大使经验达到" + levelName + "标准，本月荣登" + levelName + "大使宝座。" + integralCount + "园丁豆已打入您的账户。(请登录电脑端-『校园大使』页面查看规则)";
            String pcMsg = "恭喜您上月大使经验达到" + levelName + "标准，本月荣登" + levelName + "大使宝座。" + integralCount + "园丁豆已打入您的账户。<a href='/ambassador/center.vpage'>进入大使页面</a>";
            sendRemind(ambassador, msg, pcMsg);
        }
    }

    private void sendRemind(TeacherDetail ambassador, String msg, String pcMsg) {
        // 发送微信模板消息 以及短信
        UserAuthentication authentication = userLoaderClient.loadUserAuthentication(ambassador.getId());
        if (authentication != null && authentication.isMobileAuthenticated()) {
            // 发短信
            userSmsServiceClient.buildSms().to(authentication)
                    .content(msg)
                    .type(SmsType.AMBASSADOR_REMIND_SMS)
                    .send();
        }
        // 发模板消息  本期不做了。 等待微信号功能开通后加上
        userPopupServiceClient.createPopup(ambassador.getId()).content(pcMsg)
                .type(PopupType.AMBASSADOR_NOTICE).category(PopupCategory.LOWER_RIGHT).create();
    }
}
