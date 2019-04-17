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

package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.homework.api.constant.CheatingTeacherStatus;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.homework.api.mapper.WechatHomeworkMapper;
import com.voxlearning.utopia.service.newhomework.api.entity.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkQuestionAnswerRequest;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkCard;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.GroupLoader;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@ServiceVersion(version = "20190105")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface NewHomeworkLoader extends IPingable {

    @Idempotent
    default NewHomework loadNewHomework(String id) {
        if (!NewHomeworkUtils.isHomeworkId(id)) {
            return null;
        }
        NewHomework newHomework = load(id);
        if (newHomework != null && newHomework.isDisabledTrue()) {
            return null;
        }
        return newHomework;
    }

    @Idempotent
    default Map<String, NewHomework> loadNewHomeworks(Collection<String> ids) {
        ids = NewHomeworkUtils.filterIllegalHomeworkIds(ids);
        return loads(ids).values().stream()
                .filter(o -> !o.isDisabledTrue())
                .collect(Collectors.toMap(NewHomework::getId, Function.identity()));
    }

    @Idempotent
    NewHomework load(String id);

    @Idempotent
    Map<String, NewHomework> loads(Collection<String> ids);

    @Idempotent
    default List<NewHomework.Location> loadNewHomeworksByClazzGroupIds(Long groupId, Subject subject) {
        return loadNewHomeworksByClazzGroupIds(Collections.singleton(groupId), subject)
                .getOrDefault(groupId, Collections.emptyList());
    }

    /**
     * 根据clazzGroupIds获取相关作业（不包含已删除），CreateAt desc
     *
     * @param groupIds 班组ids
     * @return Map
     */
    @Idempotent
    Map<Long, List<NewHomework.Location>> loadNewHomeworksByClazzGroupIds(Collection<Long> groupIds);


    /**
     * 根据clazzGroupIds获取相关作业（不包含已删除），
     * 加入开始时间和结束时间限制，
     * 注意：接口不走缓存
     * @param end  作业结束时间下界
     * @param begin 作业开始时间上界
     * @return Map
     */

    @Idempotent
    Map<Long, List<NewHomework.Location>> loadNewHomeworksByClazzGroupIds(Collection<Long> groupIds, Date begin, Date end);

    /**
     * 根据groupIds获取相应的作业（不包含已删除）
     *
     * @param groupIds 班组id
     * @return Map
     */
    @Idempotent
    default Map<Long, List<NewHomework.Location>> loadNewHomeworksByClazzGroupIds(Collection<Long> groupIds, Subject subject) {
        return loadNewHomeworksByClazzGroupIds(groupIds, Collections.singleton(subject));
    }

    /**
     * 根据groupIds获取相应的作业（不包含已删除）
     *
     * @param groupIds 班组id
     * @param subjects 所包含的学科
     * @return Map
     */
    @Idempotent
    default Map<Long, List<NewHomework.Location>> loadNewHomeworksByClazzGroupIds(Collection<Long> groupIds, Collection<Subject> subjects) {
        return loadNewHomeworksByClazzGroupIds(groupIds).values()
                .stream()
                .flatMap(Collection::stream)
                .filter(o -> subjects.contains(o.getSubject()))
                .collect(Collectors.groupingBy(NewHomework.Location::getClazzGroupId));
    }

    /**
     * 根据groupIds获取相应的作业（不包含已删除）
     *
     * @param groupIds 班组id
     * @param subjects 所包含的学科
     * @param end  作业结束时间下界
     * @param begin 作业开始时间上界
     * @return Map
     */
    @Idempotent
    default Map<Long, List<NewHomework.Location>> loadNewHomeworksByClazzGroupIds(Collection<Long> groupIds, Collection<Subject> subjects, Date begin, Date end) {
        return loadNewHomeworksByClazzGroupIds(groupIds, begin, end).values()
                .stream()
                .flatMap(Collection::stream)
                .filter(o -> subjects.contains(o.getSubject()))
                .collect(Collectors.groupingBy(NewHomework.Location::getClazzGroupId));
    }


    /**
     * Load homework book of specified id.
     *
     * @param id the homework id
     * @return the homework book, return null if not found
     */
    @Idempotent
    @ServiceMethod(timeout = 5, unit = TimeUnit.SECONDS, retries = 1)
    NewHomeworkBook loadNewHomeworkBook(String id);

    @Idempotent
    Map<String, NewHomeworkBook> loadNewHomeworkBooks(Collection<String> ids);

    @Idempotent
    Map<String, Object> loadHomeworkQuestions(HomeworkQuestionAnswerRequest request);

    @Idempotent
    Map<String, Object> indexData(String homeworkId, Long studentId);

    @Idempotent
    Map<String, Object> loadQuestionAnswer(HomeworkQuestionAnswerRequest request);

    @Idempotent
    Map<String, Object> loadWordTeachQuestionsAnswer(HomeworkQuestionAnswerRequest request);

    /**
     * 获取应该订正的所有题目
     */
    @Idempotent
    Map<String, Object> loadHomeworkCorrectQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId);

    /**
     * 获取已经订正的题目
     */
    @Idempotent
    Map<String, Object> loadCorrectQuestionAnswer(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId);

    @Idempotent
    Map<String, StudentHomeworkStat> loadStudentHomeworkStats(Collection<String> uniques);

    @Idempotent
    List<StudentHomeworkStat> loadClazzStudentHomeworkStats(Long clazzId);

    @Idempotent
    List<StudentHomeworkStat> loadTeacherStudentHomeworkStats(Long teacherId);

    @Idempotent
    PossibleCheatingHomework getByTeacherIdAndHomeworkId(Long teacherId, String homeworkId, HomeworkType type);

    @Idempotent
    PossibleCheatingTeacher loadByTeacherId(Long teacherId);

    @Idempotent
    PossibleCheatingHomework loadPossibleCheatingHomeworkById(String id);

    @Idempotent
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS)
    List<PossibleCheatingHomework> findPossibleCheatingHomeworkListByDateRange(DateRange range);

    @Idempotent
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS)
    PossibleCheatingHomework loadPossibleCheatingHomeworkByTeacherIdAndHomeworkId(Long teacherId, String homeworkId, HomeworkType type);

    @Idempotent
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS)
    Page<PossibleCheatingHomework> pageFindPossibleCheatingHomeworkByDateRange(DateRange range, Pageable pageable);

    @Idempotent
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS)
    List<PossibleCheatingHomework> findPossibleCheatingHomeworkListByTeacherIdAndHomeworkIds(Long teacherId, List<String> homeworkIds, HomeworkType type);

    // PossibleCheatingTeacher

    @Idempotent
    PossibleCheatingTeacher loadPossibleCheatingTeacherByTeacherId(Long teacherId);

    @Idempotent
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS)
    List<PossibleCheatingTeacher> findPossibleCheatingTeacherListByDateRangeAndStatus(DateRange range, CheatingTeacherStatus status);

    @Idempotent
    @ServiceMethod(timeout = 10, unit = TimeUnit.MINUTES)
    List<PossibleCheatingTeacher> loadAllCheatingTeacher();

    @Idempotent
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS)
    List<PossibleCheatingTeacher> loadTeacherIdFieldWhereUpdateGteAndLt(Date start, Date end);

    @Idempotent
    @ServiceMethod(timeout = 10, unit = TimeUnit.MINUTES)
    List<PossibleCheatingTeacher> loadAllBlackTeachers();

    @Idempotent
    @ServiceMethod(timeout = 10, unit = TimeUnit.MINUTES)
    List<PossibleCheatingTeacher> loadBlackStatusTeachers();

    @Idempotent
    List<StudentHomeworkStat.DataMapper> getStudentHomeworkStatByTeacherId(Long teacherId);

    @Idempotent
    NewHomeworkPrize loadNewHomeworkPrize(NewHomework.Location location);

    @Idempotent
    Map<String, List<Map<String, Object>>> getStudentWrongQuestionIds(Long studentId, Subject subject, Collection<String> homeworkIds);

    @Idempotent
    List<WechatHomeworkMapper> loadHomeworkWithTime(Collection<Long> groupIds, Long studentId, Date startTime, Date endTime, Subject subject);

    // FIXME: 这叫什么方法？GroupLoader怎么通过远程调用传递过去？
    @Idempotent
    List<WechatHomeworkMapper> loadHomeworkWithTime(Collection<Long> groupIds, Long studentId, Date startTime, Date endTime, Subject subject, GroupLoader groupLoader);

    @Idempotent
    List<NewHomeworkCard> loadNewHomeworkCard(Long teacherId);

    @Idempotent
    Map<String, DubbingSyntheticHistory> loadDubbingSyntheticHistories(Collection<String> ids);

    @Idempotent
    Map<String, Long> getAssignHomeWorkCount(Date todayBegin, Date todayEnd, Date yesBegin, Date yesEnd, Date weekBegin, Date weekEnd);
}
