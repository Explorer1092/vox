package com.voxlearning.utopia.schedule.schedule.studytogether;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.HourRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.schedule.support.ProgressedScheduleJob;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.wechat.api.entity.WechatUserRef;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatLoader;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatService;
import com.voxlearning.galaxy.service.wechat.api.util.StudyTogetherWechatInfoProvider;
import com.voxlearning.utopia.schedule.support.StudyTogetherWechatTemplateIdConstants;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.ParentJoinLessonRef;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author zhiqian.ren
 * @since 2018-10-9
 */
@Named
@ScheduledJobDefinition(
        jobName = "一起学当日上课提醒",
        jobDescription = "每天早上7:00执行一次",
        disabled = {Mode.DEVELOPMENT, Mode.STAGING},
        ENABLED = false,
        cronExpression = "0 0 7 * * ?"
)
@ProgressTotalWork(100)
public class AutoStudyTogetherLessonRemindJob extends ProgressedScheduleJob {

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
        studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getAllStudyLesson()
                .stream()
                .filter(sl -> currentDate.after(sl.getShowDate()))
                .filter(sl -> currentDate.after(sl.getOpenDate()) && currentDate.before(sl.getCloseDate()))
                .filter(sl -> sl.getCourseLessonList().stream().anyMatch(cl -> DayRange.current().contains(cl.getOpenDate())))
                .forEach(t -> {
                    try {
                        handleLesson(t);
                    } catch (Exception e) {
                        logger.error("hand lesson send remind msg error: ", e);
                    }
                });
    }


    private void handleLesson(StudyLesson studyLesson) {
        String lessonId = SafeConverter.toString(studyLesson.getLessonId());
        //从报名开始时间至今的HourRange
        String startDateStr = DayRange.newInstance(studyLesson.getShowDate().getTime()).toString();
        HourRange endFlag = HourRange.parse(DayRange.current().toString() + "08");
        HourRange hourRange = HourRange.parse(startDateStr + "00");
        Set<Long> allSendSet = new HashSet<>();
        do {
            AlpsFuture<Set<Long>> setAlpsFuture = studyTogetherServiceClient.loadActiveParentIdSetHourRange(hourRange, lessonId);

            Set<Long> allActiveSet = new HashSet<>();
            Set<Long> uninterruptibly = setAlpsFuture.getUninterruptibly();
            if (CollectionUtils.isNotEmpty(uninterruptibly)) {
                allActiveSet.addAll(uninterruptibly);
            }
            List<String> keywords = new ArrayList<>(2);
            keywords.add(studyLesson.getTitle());
            keywords.add(DateUtils.dateToString(studyLesson.getOpenDate(), "MM月dd日"));
            for (Long activePid : allActiveSet) {
                try {
                    if (allSendSet.contains(activePid)) {
                        continue;
                    }
                    ParentJoinLessonRef parentJoinLessonRef = studyTogetherServiceClient.loadParentJoinLessonRef(lessonId, activePid);
                    if (parentJoinLessonRef == null) {
                        continue;
                    }
                    List<WechatUserRef> wechatUserRefs = dpWechatLoader.getWechatUserRef(activePid, StudyTogetherWechatInfoProvider.INSTANCE.type());
                    if (CollectionUtils.isEmpty(wechatUserRefs)) {
                        continue;
                    }
                    WechatUserRef wechatUserRef = wechatUserRefs.get(0);
                    if (StringUtils.isNotBlank(wechatUserRef.getId())) {
                        dpWechatService.sendTemplateMessage(StudyTogetherWechatInfoProvider.INSTANCE.wechatInfoContext(), wechatUserRef.getId(),
                                StudyTogetherWechatTemplateIdConstants.lessonRemindTemplateId,
                                "", "亲爱的家长，孩子今天有训练营课程哦，请及时参加！", "课程需在【家长通】App中学习",
                                keywords);
                        allSendSet.add(activePid);
                    }
                } catch (Exception e) {
                    logger.error("send parent current lesson msg error, pid = " + activePid + ", lessonId = " + lessonId + " : ", e);
                }
            }
            hourRange = hourRange.next();
        } while (hourRange.getEndDate().before(endFlag.getEndDate()));
    }
}
