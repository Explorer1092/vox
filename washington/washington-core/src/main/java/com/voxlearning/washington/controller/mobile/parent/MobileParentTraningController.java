package com.voxlearning.washington.controller.mobile.parent;

import com.google.common.collect.Sets;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSpu;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.utopia.service.parent.api.constants.StudyTogetherJoinActiveConstants;
import com.voxlearning.utopia.service.parent.api.constants.StudyTogetherSummerPackageActivity;
import com.voxlearning.utopia.service.parent.api.constants.StudyTogetherXBPayConstants;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.ParentJoinLessonRef;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.CardLessonMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.LiveCastCourseService;
import com.voxlearning.washington.controller.mobile.parent.studytogether.AbstractMobileParentStudyTogetherController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.REQ_STUDENT_ID;

/**
 * @author xin.xin
 * @since 7/6/18
 **/
@Deprecated
@Controller
@RequestMapping(value = "/parentMobile/")
public class MobileParentTraningController extends AbstractMobileParentStudyTogetherController {
    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;

    @ImportService(interfaceClass = LiveCastCourseService.class)
    private LiveCastCourseService liveCastCourseService;

    /**
     * 学习进度页训练营
     *
     * @since v3.0
     */
    @Deprecated
    @RequestMapping(value = "/studyprogress/training.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage training() {
        return MapMessage.errorMessage("活动已结束，请返回首页！");
    }


    @RequestMapping(value = "/studyresource/lessons.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lessons() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = currentParent();
        Long parentId = parent == null ? null : parent.getId();
        if (parent == null)
            studentId = 0L;
        try {
            List<CardLessonMapper> mappers = new ArrayList<>();
            List<StudyLesson> lessons = new ArrayList<>();
            boolean showSummerEntry = StudyTogetherSummerPackageActivity.inPeriod();
            MapMessage mapMessage = MapMessage.successMessage().add("show_summer_entry", showSummerEntry);
            List<CourseStructSpu> courseStructSpus = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().loadAllCourseStructSpu();
            if (CollectionUtils.isEmpty(courseStructSpus)) {
                return mapMessage;
            }
            Set<Integer> skuIds = courseStructSpus.stream().map(t -> SafeConverter.toInt(t.getId())).collect(Collectors.toSet());
            Map<Integer, List<ParentJoinLessonRef>> parentJoinSkuLessons = parentId == null ? Collections.emptyMap() : studyTogetherServiceClient.loadParentJoinSkuLessons(parentId, skuIds);
            Map<Integer, List<StudyGroup>> studentSkuGroups = studentId == 0L ? Collections.emptyMap() : studyTogetherServiceClient.loadStudentSkuGroups(studentId, skuIds);
            Integer clazzLevel = studentId == 0L ? 0 : studyTogetherServiceClient.getStudentClazzLevel(studentId);
            int runningLesson = 0;
            int finishedLesson = 0;
            if (null == parentId) {
                //未登录，取当前可报名且可显示的所有sku的最新一期
                skuIds.forEach(skuId -> {
                    StudyLesson skuLatestSignLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getSpuCardShowLatestSignUpLesson(Long.valueOf(skuId));
                    if (skuLatestSignLesson != null)
                        lessons.add(skuLatestSignLesson);
                });
                if (lessons.size() == 0) {
                    for (Integer skuId : skuIds) {
                        if (skuId != null) {
                            StudyLesson skuLatestLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getSpuCardShowLatestLesson(Long.valueOf(skuId));
                            StudyLesson skuLatestSignLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getSpuCardShowLatestSignUpLesson(Long.valueOf(skuId));
                            if (skuLatestSignLesson == null) {
                                if (skuLatestLesson != null) {
                                    lessons.add(skuLatestLesson);
                                }
                            } else {
                                lessons.add(skuLatestSignLesson);
                            }
                            if (lessons.size() > 0)
                                break;
                        }
                    }

                }
            } else {
                haha:
                for (Integer skuId : skuIds) {
                    List<ParentJoinLessonRef> parentJoinLessonRefs = parentJoinSkuLessons.get(skuId);
                    List<StudyGroup> studyGroups = studentSkuGroups.get(skuId);
                    boolean skuHasLesson = false;
                    if (CollectionUtils.isNotEmpty(studyGroups)) {
                        //已激活过
                        for (StudyGroup g : studyGroups) {
                            StudyLesson studyLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(SafeConverter.toLong(g.getLessonId()));
                            if (null == studyLesson) {
                                continue;
                            }
                            if (studyLesson.getCloseDate().after(new Date())) {
                                lessons.add(studyLesson);
                                skuHasLesson = true;
                                runningLesson += 1;
                            } else {
                                finishedLesson += 1;
                                continue haha;
                            }
                        }
                    } else if (CollectionUtils.isNotEmpty(parentJoinLessonRefs)) {
                        //报过名
                        for (ParentJoinLessonRef ref : parentJoinLessonRefs) {
                            StudyLesson studyLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(SafeConverter.toLong(ref.getStudyLessonId()));
                            if (null != studyLesson) {
                                if (DateUtils.calculateDateDay(studyLesson.getOpenDate(), 5).after(new Date())) {
                                    lessons.add(studyLesson);
                                    skuHasLesson = true;
                                }
                            }
                        }
                        ;
                    }
                    if (!skuHasLesson) {
                        StudyLesson skuLatestLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getSpuCardShowLatestLesson(Long.valueOf(skuId));
                        StudyLesson skuLatestSignLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getSpuCardShowLatestSignUpLesson(Long.valueOf(skuId));
                        if (skuLatestSignLesson == null) {
                            if (skuLatestLesson != null) {
                                lessons.add(skuLatestLesson);
                            }
                        } else {
                            lessons.add(skuLatestSignLesson);
                        }
                    }
                }
            }

            Set<String> lessonIds = lessons.stream().map(x -> SafeConverter.toString(x.getLessonId())).collect(Collectors.toSet());
            Map<String, Integer> lessonFinishProgressMap = studyTogetherServiceClient.loadStudentLessonFinishProgress(studentId, lessonIds);

            Set<String> closedLessonIdSet = Sets.newHashSet();
            Set<String> activeLessonIdSet = Sets.newHashSet();
            Set<String> joinLessonIdSet = Sets.newHashSet();
            Set<String> otherLessonIdSet = Sets.newHashSet();

            Date t = new Date();
            for (StudyLesson lesson : lessons) {
                if (null == lesson) {
                    continue;
                }
                if (null != lesson.getShowDate() && lesson.getShowDate().after(t)) {
                    continue;
                }
                String lessonId = SafeConverter.toString(lesson.getLessonId());
                int spuId = lesson.getSpuId().intValue();
                Long joinCount = studyTogetherServiceClient.loadLessonJoinCount(lessonId).getUninterruptibly();
                boolean isJoin = parentJoinSkuLessons.containsKey(spuId) && parentJoinSkuLessons.get(spuId).stream().anyMatch(ref -> ref.getStudyLessonId().equals(lessonId));
                boolean isOpen = studentSkuGroups.containsKey(spuId) && studentSkuGroups.get(spuId).stream().anyMatch(g -> g.getLessonId().equals(lessonId));
                Map<String, Integer> finishInfoMap = studyTogetherServiceClient.loadStudentTodayFinishInfo(lessonId, studentId).getUninterruptibly();
                CardLessonMapper mapper = processMapper(lesson, parentJoinSkuLessons, studentSkuGroups, lessonFinishProgressMap, finishInfoMap, joinCount);
                if (mapper == null) {
                    continue;
                }
                mappers.add(mapper);

                if (lesson.isClosed()) {
                    closedLessonIdSet.add(lessonId);
                } else if (isOpen) {
                    activeLessonIdSet.add(lessonId);
                } else if (isJoin) {
                    joinLessonIdSet.add(lessonId);
                } else
                    otherLessonIdSet.add(lessonId);
            }

            mappers.sort((o1, o2) -> {
                int score1 = o1.calculateSortScore(closedLessonIdSet, activeLessonIdSet, joinLessonIdSet, otherLessonIdSet);
                int score2 = o2.calculateSortScore(closedLessonIdSet, activeLessonIdSet, joinLessonIdSet, otherLessonIdSet);
                return Integer.compare(score1, score2);
            });
            Set<String> studentActiveLessonIdSet = studentSkuGroups.values().stream().flatMap(Collection::stream).map(StudyGroup::getLessonId).collect(Collectors.toSet());
            StudyLesson payLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(SafeConverter.toLong(StudyTogetherJoinActiveConstants.payTestSkuId));
            boolean showPayLessonEntry = payLesson.inSignUpPeriod() && studentActiveLessonIdSet.contains(StudyTogetherJoinActiveConstants.payTestOldSkuId);

            payLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(SafeConverter.toLong(StudyTogetherJoinActiveConstants.newPaySkuId));
            if (payLesson != null) {
//                boolean isCorrectPeotryUser = studentActiveLessonIdSet.stream().anyMatch(e -> Arrays.asList(StudyTogetherJoinActiveConstants.newPayOldPeotrySkuIds).contains(e)) && clazzLevel >= 2 && clazzLevel <= 5;
//                boolean isCorrectAnalectsUser = studentActiveLessonIdSet.stream().anyMatch(e -> Arrays.asList(StudyTogetherJoinActiveConstants.newPayOldAnalectsSkuIds).contains(e));
//                showPayLessonEntry = payLesson.inSignUpPeriod() && (isCorrectPeotryUser || isCorrectAnalectsUser);
                showPayLessonEntry = payLesson.inSignUpPeriod() && (clazzLevel == 0 || (clazzLevel >= 1 && clazzLevel <= 4));
            }
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            return mapMessage
                    .add("lessons", mappers)
                    .add("running_lesson_count", runningLesson)
                    .add("finished_lesson_count", finishedLesson)
                    .add("show_pay_lesson_entry", showPayLessonEntry)
                    .add("discount_price_countdown", StudyTogetherXBPayConstants.countDown())
                    .add("junior_student", studentDetail != null && (studentDetail.isSeniorStudent() || studentDetail.isJuniorStudent()));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }


    @RequestMapping(value = "/studyresource/livecast.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage liveCastCourseList() {
        User user = currentParent();
        if (user == null) {
            return liveCastCourseService.loadLiveCastCardList(0L);
        }
        long studentId = getRequestLong("sid");
        return liveCastCourseService.loadLiveCastCardList(studentId);
    }

}