package com.voxlearning.utopia.schedule.schedule.studytogether;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.HourRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.schedule.support.ProgressedScheduleJob;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.wechat.api.entity.WechatUserRef;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatLoader;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatService;
import com.voxlearning.galaxy.service.wechat.api.util.StudyTogetherWechatInfoProvider;
import com.voxlearning.utopia.schedule.support.StudyTogetherWechatTemplateIdConstants;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @author zhiqian.ren
 * @since 2018-10-9
 */
@Named
@ScheduledJobDefinition(
        jobName = "一起学开营提醒",
        jobDescription = "每天20:30执行一次",
        disabled = {Mode.DEVELOPMENT, Mode.STAGING},
        ENABLED = false,
        cronExpression = "0 30 20 * * ?"
)
@ProgressTotalWork(100)
public class AutoStudyTogetherOpenRemindJob extends ProgressedScheduleJob {

    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;

    @ImportService(interfaceClass = DPWechatLoader.class)
    private DPWechatLoader dpWechatLoader;
    @ImportService(interfaceClass = DPWechatService.class)
    private DPWechatService dpWechatService;

    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    @Override
    protected void executeJob(long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) {
        Date currentDate = new Date();
        List<StudyLesson> studyLessons = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getAllStudyLesson()
                .stream()
                .filter(sl -> currentDate.after(sl.getShowDate()))
                .filter(sl -> DayRange.current().next().contains(sl.getOpenDate())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(studyLessons)) {
            logger.info("--------AutoStudyTogetherOpenRemindJob studyLessons count:" + studyLessons.size() + "-------");
            int progressStep = 100 / studyLessons.size();
            for (StudyLesson t : studyLessons) {
                try {
                    handleLesson(t);
                    progressMonitor.worked(progressStep);
                } catch (Exception e) {
                    logger.error("AutoStudyTogetherOpenRemindJob lesson send remind msg error: ", e);
                }
            }
        }
        progressMonitor.done();
        logger.info("--------AutoStudyTogetherOpenRemindJob send msg job finished");
    }

    private void handleLesson(StudyLesson studyLesson) {

        String lessonId = SafeConverter.toString(studyLesson.getLessonId());
        //从报名开始时间至今的HourRange
        String startDateStr = DayRange.newInstance(studyLesson.getShowDate().getTime()).toString();
        HourRange endFlag = HourRange.parse(DayRange.current().toString() + "21");
        HourRange hourRange = HourRange.parse(startDateStr + "00");
        Set<Long> allSendSet = new ConcurrentHashSet<>();
        do {
            AlpsFuture<Set<Long>> setAlpsFuture = studyTogetherServiceClient.loadActiveParentIdSetHourRange(hourRange, lessonId);

            List<Long> allActiveList = new ArrayList<>();
            Set<Long> uninterruptibly = setAlpsFuture.getUninterruptibly();
            if (CollectionUtils.isNotEmpty(uninterruptibly)) {
                allActiveList.addAll(uninterruptibly);
            }
            List<String> keywords = new ArrayList<>(2);
            keywords.add(studyLesson.getTitle());
            keywords.add(DateUtils.dateToString(studyLesson.getOpenDate(), "MM月dd日"));
            List<List<Long>> splitList = CollectionUtils.splitList(allActiveList, 10);
            int threadCount = splitList.size();
            CountDownLatch latch = new CountDownLatch(splitList.size());
            for (int i = 0; i < threadCount; i++) {
                List<Long> dataList = splitList.get(i);
                AlpsThreadPool.getInstance().submit(() -> {
                    try {
                        for (Long activePid : dataList) {
                            dealSendMessage(allSendSet, activePid, studyLesson, keywords);
                        }
                    } catch (Exception e) {
                    } finally {
                        latch.countDown();
                    }
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hourRange = hourRange.next();
        } while (hourRange.getEndDate().before(endFlag.getEndDate()));
    }

    private void dealSendMessage(Set<Long> allSendSet, Long activePid, StudyLesson studyLesson, List<String> keywords) {
        try {
            if (allSendSet.contains(activePid)) {
                return;
            }
            List<WechatUserRef> wechatUserRefs = dpWechatLoader.getWechatUserRef(activePid, StudyTogetherWechatInfoProvider.INSTANCE.type());
            if (CollectionUtils.isEmpty(wechatUserRefs)) {
                return;
            }
            WechatUserRef wechatUserRef = wechatUserRefs.get(0);
            if (StringUtils.isNotBlank(wechatUserRef.getId())) {
                dpWechatService.sendTemplateMessage(StudyTogetherWechatInfoProvider.INSTANCE.wechatInfoContext(), wechatUserRef.getId(),
                        StudyTogetherWechatTemplateIdConstants.lessonRemindTemplateId,
                        "", "家长您好，您报名的一起学课程将于明天开课，请准时参加！", "课程需在【家长通】App中学习",
                        keywords);
                allSendSet.add(activePid);
            }
        } catch (Exception e) {
            logger.error("send parent open lesson msg error, pid = " + activePid + ", lessonId = " + studyLesson.getLessonId() + " : ", e);
        }
    }
}
