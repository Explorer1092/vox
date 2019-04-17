package com.voxlearning.utopia.schedule.schedule.studytogether;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
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
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructLesson;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.galaxy.service.wechat.api.entity.WechatUserRef;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatLoader;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatService;
import com.voxlearning.galaxy.service.wechat.api.util.StudyTogetherWechatInfoProvider;
import com.voxlearning.utopia.schedule.support.StudyTogetherWechatTemplateIdConstants;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;

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
        jobName = "一起学当日未完成课程提醒",
        jobDescription = "每天20点40执行一次",
        disabled = {Mode.DEVELOPMENT, Mode.STAGING},
        ENABLED = false,
        cronExpression = "0 40 20 * * ?"
)
@ProgressTotalWork(100)
public class AutoStudyTogetherNoFinishRemindJob extends ProgressedScheduleJob {

    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;
    @Inject
    private ParentLoaderClient parentLoaderClient;

    @ImportService(interfaceClass = DPWechatLoader.class)
    private DPWechatLoader dpWechatLoader;
    @ImportService(interfaceClass = DPWechatService.class)
    private DPWechatService dpWechatService;
    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    @Override
    protected void executeJob(long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) {
        Date current = new Date();
        List<StudyLesson> studyLessons = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getAllStudyLesson()
                .stream()
                .filter(sl -> current.after(sl.getShowDate()))
                .filter(sl -> current.after(sl.getOpenDate()) && current.before(sl.getCloseDate()))
                .filter(sl -> sl.getCourseLessonList().stream().anyMatch(cl -> DayRange.current().contains(cl.getOpenDate())))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(studyLessons)) {
            logger.info("--------AutoStudyTogetherNoFinishRemindJob studyLessons count:" + studyLessons.size() + "-------");
            int progressStep = 100 / studyLessons.size();
            for (StudyLesson t : studyLessons) {
                try {
                    handleLesson(t);
                    progressMonitor.worked(progressStep);
                } catch (Exception e) {
                    logger.error("AutoStudyTogetherNoFinishRemindJob lesson send remind msg error: ", e);
                }
            }
        }
        progressMonitor.done();
        logger.info("--------AutoStudyTogetherNoFinishRemindJob send msg job finished");
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

            List<Long> allActiveSet = new ArrayList<>();
            Set<Long> uninterruptibly = setAlpsFuture.getUninterruptibly();
            if (CollectionUtils.isNotEmpty(uninterruptibly)) {
                allActiveSet.addAll(uninterruptibly);
            }
            List<String> keywords = new ArrayList<>(2);
            keywords.add(studyLesson.getTitle());
            keywords.add(DateUtils.dateToString(new Date(), "MM月dd日"));
            List<List<Long>> splitList = CollectionUtils.splitList(allActiveSet, 10);
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
        String lessonId = SafeConverter.toString(studyLesson.getLessonId());
        try {
            if (allSendSet.contains(activePid)) {
                return;
            }
            //验证今天学生课程是否完成
            List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(activePid);
            if (CollectionUtils.isEmpty(studentParentRefs)) {
                return;
            }
            List<Long> studentIds = new ArrayList<>(studentParentRefs.size());
            for (StudentParentRef s : studentParentRefs) {
                Map<String, StudyGroup> studyGroupMap = studyTogetherServiceClient.loadStudentGroupByLessonId(s.getStudentId(), Collections.singleton(lessonId));
                if (MapUtils.isEmpty(studyGroupMap)) {
                    continue;
                }
                List<CourseStructLesson> courseLessonList = studyLesson.getCourseLessonList();
                //今天是否有课
                boolean hasLesson = CollectionUtils.isNotEmpty(courseLessonList) && courseLessonList.stream().anyMatch(courseLesson -> DayRange.current().contains(courseLesson.getOpenDate()));
                if (hasLesson) {
                    Map<String, Integer> finishInfo = studyTogetherServiceClient.loadStudentTodayFinishInfo(lessonId, s.getStudentId()).getUninterruptibly();
                    if (SafeConverter.toInt(finishInfo.get("star")) == -1) {
                        studentIds.add(s.getStudentId());
                    }
                }
            }
            if (CollectionUtils.isEmpty(studentIds)) {
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
                        "", "亲爱的家长，今天孩子的训练营课程还未完成哦", "请及时参加！课程需在【家长通】APP中学习",
                        keywords);
                allSendSet.add(activePid);
            }
        } catch (Exception e) {
            logger.error("send parent active msg error, pid = " + activePid + ", lessonId = " + lessonId + " : ", e);
        }
    }
}
