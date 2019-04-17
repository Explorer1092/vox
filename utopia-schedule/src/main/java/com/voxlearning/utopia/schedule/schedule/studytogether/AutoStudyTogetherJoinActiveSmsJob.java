package com.voxlearning.utopia.schedule.schedule.studytogether;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.ThreadUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.HourRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.ParentJoinLessonRef;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyOpWechatAccount;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author jiangpeng
 * @since 2018-04-20 下午7:40
 **/
@Named
@ScheduledJobDefinition(
        jobName = "一起学激活短信提醒job",
        jobDescription = "一起学激活短信提醒job，每天18点运行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 18 * * ?")
@ProgressTotalWork(100)
public class AutoStudyTogetherJoinActiveSmsJob extends ScheduledJobWithJournalSupport {

    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;

    @Inject
    private SmsServiceClient smsServiceClient;

    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        //取出前一天的24个小时
        List<HourRange> joinHourList = new ArrayList<>();
        List<HourRange> activeHourList = new ArrayList<>();
        DayRange currentDay = DayRange.current();
        DayRange yesterdayRange = currentDay.previous();
        String yesterdayStr = yesterdayRange.toString();
        HourRange hourRange = HourRange.parse(yesterdayStr + "00");
        for (int i = 0 ; i <42 ;i ++){
            if (i < 24) {
                joinHourList.add(hourRange);
                System.out.println(hourRange.toString());
            }
            activeHourList.add(hourRange);
            hourRange = hourRange.next();
        }

        List<StudyLesson> allStudyLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getAllStudyLesson();

        Date date = new Date();
        allStudyLesson.stream().filter(t ->
                t.getShowDate() != null && t.getSighUpEndDate() != null &&
            t.getShowDate().before(date) && DateUtils.calculateDateDay(t.getSighUpEndDate(), 1).after(date)
        ).forEach(t -> {
            try {
                handleLesson(joinHourList, activeHourList, t);
            }catch (Exception e){
                logger.error("hand lesson send sms error: ", e);
            }
        });

    }

    private void handleLesson(List<HourRange> joinHourList, List<HourRange> activeHourList, StudyLesson studyLesson){
        boolean lightSpu = studyLesson.safeIsLightSpu() || studyLesson.safeIsRealLightSku();
        if (lightSpu){
            return;
        }
        String lessonId = SafeConverter.toString(studyLesson.getLessonId());
        Map<HourRange, AlpsFuture<Set<Long>>> joinParentIdSetFutureMap = new HashMap<>();
        Map<HourRange, AlpsFuture<Set<Long>>> activeParentIdFutureMap = new HashMap<>();
        for (HourRange range : joinHourList) {
            AlpsFuture<Set<Long>> setAlpsFuture = studyTogetherServiceClient.loadJoinParentIdSetHourRange(range, lessonId);
            joinParentIdSetFutureMap.put(range, setAlpsFuture);
        }

        for (HourRange range : activeHourList) {
            AlpsFuture<Set<Long>> setAlpsFuture = studyTogetherServiceClient.loadActiveParentIdSetHourRange(range, lessonId);
            activeParentIdFutureMap.put(range, setAlpsFuture);
        }

        Set<Long> allJoinSet = new HashSet<>();
        Set<Long> allActiveSet = new HashSet<>();

        for (AlpsFuture<Set<Long>> setAlpsFuture : activeParentIdFutureMap.values()) {
            Set<Long> uninterruptibly = setAlpsFuture.getUninterruptibly();
            if (CollectionUtils.isNotEmpty(uninterruptibly)){
                allActiveSet.addAll(uninterruptibly);
            }
        }

        for (AlpsFuture<Set<Long>> setAlpsFuture : joinParentIdSetFutureMap.values()) {
            Set<Long> uninterruptibly = setAlpsFuture.getUninterruptibly();
            if (CollectionUtils.isNotEmpty(uninterruptibly)){
                allJoinSet.addAll(uninterruptibly);
            }
        }

        allJoinSet.removeAll(allActiveSet);
        for (Long noActivePid : allJoinSet) {
            try {
                ParentJoinLessonRef parentJoinLessonRef = studyTogetherServiceClient.loadParentJoinLessonRef(lessonId, noActivePid);
                if (parentJoinLessonRef == null)
                    continue;
                String sourceOpWechatId = parentJoinLessonRef.getSourceOpWechatId();
                StudyOpWechatAccount studyOpWechatAccount = studyTogetherServiceClient.getStudyTogetherBuffer().getStudyOpWechatAccount(sourceOpWechatId);
                if (studyOpWechatAccount == null)
                    continue;
                String phone = sensitiveUserDataServiceClient.showUserMobile(noActivePid, "sch:AutoStudyTogetherJoinActiveSmsJob", "9999");
                if (StringUtils.isBlank(phone))
                    continue;
                String title = studyLesson.getTitle();
                String content ;
                if (lightSpu){
                    content = "家长您昨天报名的《"+title+"》课程还未激活，课程已经开课1天，请尽快进入【家长通】根据提示进行激活。有问题请咨询客服电话：400-160-1717";
                }else {
                    content = "您报名的《" + title + "》还未激活，现添加老师微信激活，微信号：" + studyOpWechatAccount.getWechatNumber();
                    if (SafeConverter.toInt(studyLesson.getQrcodeType(), 2) == 2) {
                        content = "您报名的《" + title + "》还未激活，请登陆家长通app完成激活，感谢您对一起学的支持！";
                    }
                }
                smsServiceClient.createSmsMessage(phone)
                        .type(SmsType.PARENT_YIQIXUE_NOACTIVE_NOTIFY.name())
                        .content(content)
                        .send();
                ThreadUtils.sleepCurrentThread(10);
            }catch (Exception e){
                logger.error("send parent no active sms error, pid = "+noActivePid+", lessonId = "+ lessonId +" : ", e);
            }
        }
    }
}
