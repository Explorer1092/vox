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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
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
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.business.consumer.AmbassadorManagementClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.UserTag;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserTagLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer Yang on 2015/11/18.
 */
@Named
@ScheduledJobDefinition(
        jobName = "自动审核校园大使-实习大使",
        jobDescription = "每天00:10执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 10 0 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoAmbassadorAuditSXJob extends ScheduledJobWithJournalSupport {

    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private AmbassadorManagementClient ambassadorManagementClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserTagLoaderClient userTagLoaderClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;

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
        // 获取所有实习大使
        String sxsql = "SELECT AMBASSADOR_ID FROM VOX_AMBASSADOR_LEVEL_DETAIL WHERE `LEVEL` = 'SHI_XI' AND DISABLED = FALSE;";
        List<Long> sxAmbassadorIds = utopiaSql.withSql(sxsql).queryColumnValues(Long.class);
        if (CollectionUtils.isEmpty(sxAmbassadorIds)) {
            jobJournalLogger.log("没有需要考核的实习大使！");
            return;
        }
        jobJournalLogger.log("共有" + sxAmbassadorIds.size() + "个实习大使需要进行考核处理");
        progressMonitor.worked(5);

        ISimpleProgressMonitor monitor = progressMonitor.subTask(95, sxAmbassadorIds.size());
        for (Long ambassadorId : sxAmbassadorIds) {
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
//        List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(ambassador.getTeacherSchoolId());
//        // 过滤 同学科老师 成为大使之后认证的老师， 实习期内不计算到任务里
//        teachers = teachers.stream().filter(t -> t.getSubject() == ambassador.getSubject())
//                .filter(t -> t.fetchCertificationState() == AuthenticationState.SUCCESS)
//                .filter(t -> t.getLastAuthDate() == null || t.getLastAuthDate().before(ref.getCreateDatetime()))
//                .filter(t -> t.getPending() == null || t.getPending() != 1).collect(Collectors.toList());
//        Map<Long, UserTag> userTagMap = userTagLoaderClient.loadUserTags(teachers.stream().map(Teacher::getId).collect(Collectors.toList()));
        // 根据有没有认证老师 来判断执行的考核逻辑
        UserTag userTag = userTagLoaderClient.loadUserTag(ambassadorId);
        boolean selfFlag = isLight(userTag, ref, AmbassadorLevel.SHI_XI);
//        boolean teacherFlag = false;
//        teachers = teachers.stream().filter(t -> !Objects.equals(t.getId(), ambassadorId)).collect(Collectors.toList());
//        int lc = 0;
//        if (CollectionUtils.isNotEmpty(teachers)) {
//            // 判断 两个任务是否满足
//            for (Teacher teacher : teachers) {
//                //查询tag
//                int lightCount = getLightIconInfo(userTagMap.get(teacher.getId()), ref, AmbassadorLevel.SHI_XI);
//                if (lightCount >= 5) {
//                    lc += 1;
//                }
//            }
//        }
//        if (RuntimeMode.le(Mode.STAGING)) {
//            if (lc >= 1) {
//                teacherFlag = true;
//            }
//        } else {
//            if (Objects.equals(lc, teachers.size())) {
//                teacherFlag = true;
//            }
//        }
        DateRange range = getDateRange(ref, AmbassadorLevel.SHI_XI);
        // 新规则 积分 >= 40
        Integer totalScore = loadAmbassadorTotalScore(ambassadorId, range.getStartDate());
        if (selfFlag && totalScore >= 40) {
            // 满足了实习大使的任务 直接转为铜牌
            AmbassadorLevelDetail ambassadorLevelDetail = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorLevelDetail(ambassadorId);
            if (ambassadorLevelDetail == null) {
                // 如果为空 插入一条数据 防止数据错误
                ambassadorLevelDetail = new AmbassadorLevelDetail();
                ambassadorLevelDetail.setSchoolId(ambassador.getTeacherSchoolId());
                ambassadorLevelDetail.setLevel(AmbassadorLevel.TONG_PAI);
                ambassadorLevelDetail.setAmbassadorId(ambassadorId);
                ambassadorLevelDetail.setBornDate(new Date());
                ambassadorLevelDetail.setIsObservation(false);
                ambassadorServiceClient.getAmbassadorService().$insertAmbassadorLevelDetail(ambassadorLevelDetail);
            } else {
                ambassadorLevelDetail.setBornDate(new Date());
                ambassadorLevelDetail.setLevel(AmbassadorLevel.TONG_PAI);
                ambassadorServiceClient.getAmbassadorService().$replaceAmbassadorLevelDetail(ambassadorLevelDetail);
            }
            // 发送通知
            String msg = "恭喜您通过实习期考核任务，现已晋升为铜牌大使，赶快到电脑端『校园大使』页面看看奖励规则吧！";
            String pcMsg = "恭喜您通过实习期考核任务，现已晋升为铜牌大使！<a href='/ambassador/center.vpage'>进入大使页面</a>";
            sendRemind(ambassador, msg, pcMsg);
        } else {
            // 不满足任务
            Date endDate = getDateRange(ref, AmbassadorLevel.SHI_XI).getEndDate();
            if (new Date().after(endDate)) {
                // 取消大使
                MapMessage message = ambassadorManagementClient.getRemoteReference().resignationAmbassador(ambassador);
                if (message.isSuccess()) {
                    // 给大使发通知
                    String msg = "实习期内，您未能完成规定任务，目前您已变为普通老师。";
                    sendRemind(ambassador, msg, msg);
                }
            } else {
                // 7天  14天 发两次提醒
                long diff = DateUtils.dayDiff(endDate, new Date());
                if (diff == 7 || diff == 14) {
                    String pcMsg = "大使您好，距离大使实习期结束还有" + diff + "天，您尚未完成规定任务；如实习期内无法完成，将变回普通老师哦。";
                    // 发送通知
                    String msg = "距离大使实习期结束还有" + diff + "天，您尚未完成考核；如实习期内无法完成，将变回普通老师哦。（请登录电脑端-『校园大使』页面查看规则）";
                    sendRemind(ambassador, msg, pcMsg);
                }
            }
        }
    }

    // 获取当前老师图标点亮个数
//    private int getLightIconInfo(UserTag tag, AmbassadorSchoolRef ref, AmbassadorLevel level) {
//        if (tag == null) {
//            return 0;
//        }
//        List<String> mentorTagKey = Arrays.asList(UserTagType.AMBASSADOR_MENTOR_BBS.name(),
//                UserTagType.AMBASSADOR_MENTOR_COMMENT.name(),
//                UserTagType.AMBASSADOR_MENTOR_DO_LOTTERY.name(),
//                UserTagType.AMBASSADOR_MENTOR_HOMEWORK.name(),
//                UserTagType.AMBASSADOR_MENTOR_QUIZ.name(),
//                UserTagType.AMBASSADOR_MENTOR_READING.name(),
//                UserTagType.AMBASSADOR_MENTOR_REWARD_ORDER.name(),
//                UserTagType.AMBASSADOR_MENTOR_REWARD_STAR.name(),
//                UserTagType.AMBASSADOR_MENTOR_WECHAT_HOMEWORK.name(),
//                UserTagType.AMBASSADOR_MENTOR_SMART_CLAZZ.name());
//        Map<String, UserTag.Tag> tagMap = tag.getTags();
//        int count = 0;
//        for (String key : tagMap.keySet()) {
//            if (mentorTagKey.contains(key)) {
//                String mentorJson = tagMap.get(key).getValue();
//                Map<String, Object> dataMap = JsonUtils.fromJson(mentorJson);
//                Long longDate = (Long) dataMap.get("monthFirstDate");
//                Date monthFirstDate = new Date(longDate);
//                if (getDateRange(ref, level).contains(monthFirstDate)) {
//                    count++;
//                }
//            }
//        }
//        return count;
//    }

    // 大使本人是否满足考核条件
    private boolean isLight(UserTag tag, AmbassadorSchoolRef ref, AmbassadorLevel level) {
        if (tag == null) {
            return false;
        }
        List<String> mentorTagKey = Arrays.asList(
                UserTagType.AMBASSADOR_MENTOR_BBS.name(),
//                UserTagType.AMBASSADOR_MENTOR_COMMENT.name(),
//                UserTagType.AMBASSADOR_MENTOR_DO_LOTTERY.name(),
                UserTagType.AMBASSADOR_MENTOR_HOMEWORK.name(),
//                UserTagType.AMBASSADOR_MENTOR_QUIZ.name(),
//                UserTagType.AMBASSADOR_MENTOR_WECHAT_HOMEWORK.name(),
                UserTagType.AMBASSADOR_BIND_WECHAT.name());
        Map<String, UserTag.Tag> tagMap = tag.getTags();
        int count = 0;
        for (String key : tagMap.keySet()) {
            if (mentorTagKey.contains(key)) {
                String mentorJson = tagMap.get(key).getValue();
                Map<String, Object> dataMap = JsonUtils.fromJson(mentorJson);
                Long longDate = (Long) dataMap.get("monthFirstDate");
                Date monthFirstDate = new Date(longDate);
                if (getDateRange(ref, level).contains(monthFirstDate)) {
                    count++;
                }
            }
        }
        return count >= 3;
    }

    // FIXME: copied from AmbassadorService
    // 根据不同的大使级别 获取对应的 图标统计时间
    public DateRange getDateRange(AmbassadorSchoolRef ref, AmbassadorLevel level) {
        if (level == AmbassadorLevel.SHI_XI) {
            // 实习大使统计时间 是 大使的实习期
            return new DateRange(ref.getCreateDatetime(), DateUtils.calculateDateDay(ref.getCreateDatetime(), 30));
        } else {
            return MonthRange.current();
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
