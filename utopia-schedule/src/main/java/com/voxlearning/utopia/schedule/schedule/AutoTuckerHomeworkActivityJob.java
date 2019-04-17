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
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.ChargeType;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.entity.activity.tuckerhomework.ClazzTuckerHomeworkInfo;
import com.voxlearning.utopia.entity.activity.tuckerhomework.TuckerActivityRecord;
import com.voxlearning.utopia.entity.activity.tuckerhomework.TuckerHomeworkInfo;
import com.voxlearning.utopia.entity.activity.tuckerhomework.TuckerWeeklyHomeworkReport;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.business.constant.Teacher51ActRegion;
import com.voxlearning.utopia.service.business.consumer.TeacherActivityServiceClient;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.finance.client.WirelessChargingServiceClient;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.consumer.NewAccomplishmentLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.lang.convert.SafeConverter.toInt;
import static com.voxlearning.utopia.entity.activity.tuckerhomework.TuckerWeeklyHomeworkReport.CalculateRange;

/**
 * 2018年春季开学老师活动
 * 活动二 - 数学老师每周作业任务
 * 检查老师上一周（周一00:00:00-周日23:59:59）的布置&检查作业情况，挑出达成任务老师充10元话费&发送短信
 * <p>
 * 改造为2018五一活动
 */
@Named
@ScheduledJobDefinition(
        jobName = "2018年五一老师活动数学老师每周作业任务",
        jobDescription = "每天零点10分运行一次",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING, /*Mode.PRODUCTION*/},
        cronExpression = "0 10 0 * * ?",
        ENABLED = true
)
@ProgressTotalWork(100)
public class AutoTuckerHomeworkActivityJob extends ScheduledJobWithJournalSupport {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject private NewAccomplishmentLoaderClient newAccomplishmentLoaderClient;
    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject private TeacherActivityServiceClient teacherActivityServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private WirelessChargingServiceClient wirelessChargingServiceClient;
    @Inject private AppMessageServiceClient appMsgSrvCli;

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigSrv;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        logger.info("Start Send Reward for Tucker Teacher Activity.");
        List<TuckerActivityRecord> records = teacherActivityServiceClient.getRemoteReference().loadAllTuckerParticipateTeacher();

        logger.info("Total {} participate in this activity. Let's do it.", records.size());
        progressMonitor.worked(5);

        Date now = new Date();
        DateRange range = TuckerActivityRecord.ActivityRange;
        if (!range.contains(now)) {
            logger.info("Activity is finished");
            progressMonitor.done();
            return;
        }

        // 发push的时间走配置
        long sendPushTime = 0;
        String sendPushTimeStr = crmConfigSrv.$loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "TCH51ACT_SEND_PUSH_TIME");
        if (StringUtils.isNotEmpty(sendPushTimeStr)) {
            String[] timeParts = sendPushTimeStr.split(":");
            if (timeParts.length == 3) {
                // 延迟到当天的14:30发送
                Calendar pushTimeCal = Calendar.getInstance();
                pushTimeCal.setTime(now);
                pushTimeCal.set(Calendar.HOUR_OF_DAY, toInt(timeParts[0]));
                pushTimeCal.set(Calendar.MINUTE, toInt(timeParts[1]));
                pushTimeCal.set(Calendar.SECOND, toInt(timeParts[2]));

                sendPushTime = pushTimeCal.getTimeInMillis();
            }
        }

        ISimpleProgressMonitor monitor = progressMonitor.subTask(80, records.size());
        for (TuckerActivityRecord record : records) {
            // 取出数学账号
            Long mathTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(record.getTeacherId(), Subject.MATH);
            if (mathTeacherId == null) continue;

            // 最老师的作业报告
            TuckerWeeklyHomeworkReport report = teacherActivityServiceClient.getRemoteReference()
                    .loadTeacherLastWeekReport(record.getTeacherId());

            if (report == null) {
                // 班级作业信息
                report = new TuckerWeeklyHomeworkReport(record.getTeacherId());
                report.setMathTeacherId(mathTeacherId);
                report.setSchoolId(record.getSchoolId());
            }

            // 每次跑批都重新生成
            report.setClassHomeworkInfo(new LinkedList<>());

            // 获取老师所有班级名称及学生人数
            Map<Long, GroupTeacherMapper> teacherGroups = deprecatedGroupLoaderClient.loadTeacherGroups(mathTeacherId, true)
                    .stream().collect(Collectors.toMap(GroupTeacherMapper::getId, Function.identity()));
            Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzs(teacherGroups.values().stream().map(GroupTeacherMapper::getClazzId).collect(Collectors.toSet()))
                    .stream()
                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));
            // 获取作业
            Map<Long, List<NewHomework.Location>> homeworkMap = newHomeworkLoaderClient.loadNewHomeworksByClazzGroupIds(teacherGroups.keySet(), Subject.MATH);

            for (GroupTeacherMapper group : teacherGroups.values()) {
                // 班级信息
                Clazz clazz = clazzMap.get(group.getClazzId());
                if (clazz == null || clazz.isDisabledTrue() || clazz.isTerminalClazz()) {
                    continue;
                }
                ClazzTuckerHomeworkInfo groupInfo = new ClazzTuckerHomeworkInfo();
                groupInfo.setClazzId(group.getClazzId());
                groupInfo.setClazzName(clazz.formalizeClazzName());
                groupInfo.setGroupId(group.getId());
                groupInfo.setHomeworkList(new LinkedList<>());

                List<GroupMapper.GroupUser> students = group.getStudents() == null ? Collections.emptyList() : group.getStudents();
                // 取[班级学生数>=20]&[本周至少布置过1次作业]的班级
                if (students.size() < ClazzTuckerHomeworkInfo.AccomplishCount) {
                    continue;
                }

                Set<Long> studentIds = students.stream().map(GroupMapper.GroupUser::getId).collect(Collectors.toSet());
                groupInfo.setStudentCount(students.size());
                // 作业信息
                final DateRange dateRange = range;
                // 只查看上周检查过的作业
                List<NewHomework.Location> homeworks = homeworkMap.getOrDefault(group.getId(), Collections.emptyList())
                        .stream()
                        .filter(homework -> dateRange.contains(homework.getCreateTime()) && homework.isChecked())
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(homeworks)) {
                    continue;
                }

                // 作业信息
                List<TuckerHomeworkInfo> homeworkList = new LinkedList<>();
                for (NewHomework.Location homework : homeworks) {
                    TuckerHomeworkInfo homeworkInfo = new TuckerHomeworkInfo();
                    homeworkInfo.setAssignTime(new Date(homework.getCreateTime()));
                    homeworkInfo.setAssignTimeStr(DateUtils.dateToString(homeworkInfo.getAssignTime(), "M/d HH:mm"));

                    int accomplishCount = 0;
                    NewAccomplishment acc = newAccomplishmentLoaderClient.loadNewAccomplishment(homework);
                    if (acc != null && acc.size() > 0) {
                        accomplishCount = (int) acc.getDetails().keySet()
                                .stream()
                                .filter(sid -> studentIds.contains(SafeConverter.toLong(sid)))
                                .count();
                    }
                    homeworkInfo.setAccomplishCount(accomplishCount);
                    homeworkInfo.setAccomplished(accomplishCount >= ClazzTuckerHomeworkInfo.AccomplishCount);
                    homeworkList.add(homeworkInfo);
                }
                groupInfo.setHomeworkList(homeworkList);
                report.getClassHomeworkInfo().add(groupInfo);
            }

            // 发放奖励
            sendTeacherReward(record.getTeacherId(), record, report, sendPushTime);

            // 更新称号
            teacherActivityServiceClient.getRemoteReference().saveTuckerWeeklyHomeworkReport(report);
            teacherActivityServiceClient.getRemoteReference().updateTuckerActivityRecord(record);
            monitor.worked(1);

            // 发通知
//            String templateMessage = StringUtils.formatMessage(
//                    "您参加的“连熊孩子都爱的数学作业”活动上周报告在这里， <a href=\"{}\" class=\"w-blue\" target=\"_blank\">点击查看>></a>",
//                    ProductConfig.getMainSiteBaseUrl() + "/teacher/activity/term2017/acttwo/report.vpage"
//            );
//            teacherLoaderClient.sendTeacherMessage(record.getId(), templateMessage);
            // 发送app消息
            /*AppUserMessage appMessage = new AppUserMessage();
            appMessage.setUserId(record.getId());
            appMessage.setMessageType(TeacherMessageType.WEEKREPORTNOTICE.getType());
            appMessage.setContent("您参加的“连熊孩子都爱的数学作业”活动上周报告在这里");
            appMessage.setTitle("数学作业活动周报告");
            appMessage.setCreateTime(sendTime);
            appMessage.setExpiredTime(expireTime);
            appMessage.setLinkUrl(ProductConfig.getMainSiteBaseUrl() + "/view/mobile/teacher/weektask_report");
            teacherLoaderClient.sendTeacherAppMessage(appMessage);*/
        }

        progressMonitor.done();
    }


    private void sendTeacherReward(Long teacherId,
                                   TuckerActivityRecord record,
                                   TuckerWeeklyHomeworkReport report,
                                   long sendPushTime) {
        /*if (!report.accomplishTuckerTask()) {
            return;
        }*/
        // 如果已经领取则直接返回
        if (SafeConverter.toBoolean(record.getAwardReceived()))
            return;

        TeacherDetail td = teacherLoaderClient.loadTeacherDetail(teacherId);
        long accomplishNum = report.getAccomplishNum();

        // 获取老师手机号
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacherId);
        if (ua == null || StringUtils.isBlank(ua.getSensitiveMobile())) {
            return;
        }

        Set<Integer> regionList;
        for (Teacher51ActRegion tchActRegion : Teacher51ActRegion.values()) {
            // 属于哪组地区的，就走哪几档配置
            regionList = raikouSystem.getRegionBuffer().findByTag(tchActRegion.name());
            if (!regionList.isEmpty() && !regionList.contains(td.getRegionCode()))
                continue;

            if (accomplishNum >= tchActRegion.getCondition()) {
                try {
                    wirelessChargingServiceClient.getWirelessChargingService()
                            .saveWirelessCharging(
                                    teacherId,
                                    ChargeType.TUCKER_HOMEWORK_ACTIVITY,
                                    ua.getSensitiveMobile(),
                                    tchActRegion.getAwardMoney() * 100, // 金额是分
                                    "尊敬老师您好，您参加“劳动最光荣，集勋章，得奖励”活动获得的" + tchActRegion.getAwardMoney()
                                            + "元布置作业流量费充值成功，感谢您的支持与信任！敬请期待更多活动.",
                                    null
                            ).awaitUninterruptibly();
                } catch (Exception e) {
                    logger.error("Send flow error!,teacherId:{}", teacherId, e);
                } finally {
                    // 无论怎么样，都要置上领取的状态，不能让用户多领
                    record.setAwardReceived(true);
                }
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                // 一、三、 五给发push，测试环境不限制
                if (!RuntimeMode.isProduction() || (dayOfWeek == 2 || dayOfWeek == 4 || dayOfWeek == 6)) {
                    Date now = new Date();
                    long dayDiff = Optional.ofNullable(record.getLastPushTime())
                            .map(lpt -> DateUtils.dayDiff(now, new Date(lpt)))
                            .orElse(100L);
                    if (dayDiff >= 0) {
                        Map<String, Object> jpushExtInfo = new HashMap<>();
                        jpushExtInfo.put("link", "/view/mobile/teacher/activity/labourday/index");
                        jpushExtInfo.put("s", TeacherMessageType.ACTIVIY.name());
                        jpushExtInfo.put("key", "j");
                        jpushExtInfo.put("t", "h5");

                        String content = "五一献礼，任意布置3天作业最高得" + tchActRegion.getAwardMoney() + "元流量费奖励，活动正在进行中，即刻布置!";
                        appMsgSrvCli.sendAppJpushMessageByIds(content, AppMessageSource.PRIMARY_TEACHER, Collections.singletonList(teacherId), jpushExtInfo, sendPushTime);

                        // 记录发push这一天的零点
                        record.setLastPushTime(DateUtils.truncate(now, Calendar.DATE).getTime());
                    }
                }
            }

            break;
        }

        // 不知所云的一些东西
        //report.levelUp();
        //record.updateCurrentLevel(report.getCurrentLevel());
        //record.updateFinishWeek(calculateCurrentWeek());
    }

    private int calculateCurrentWeek() {
        Date date = new Date();
        if (RuntimeMode.isUsingTestData()) {
            return (int) DateUtils.dayDiff(date, DateUtils.stringToDate("2018-02-26 00:00:00"));
        }
        return (int) DateUtils.dayDiff(date, CalculateRange.getStartDate()) / 7;
    }

    public static void main(String[] args) {
        Calendar pushTimeCal = Calendar.getInstance();
        pushTimeCal.setTime(new Date());
        pushTimeCal.set(Calendar.HOUR_OF_DAY, 14);
        pushTimeCal.set(Calendar.MINUTE, 30);
        System.out.println(DateUtils.dateToString(pushTimeCal.getTime()));
    }

}
