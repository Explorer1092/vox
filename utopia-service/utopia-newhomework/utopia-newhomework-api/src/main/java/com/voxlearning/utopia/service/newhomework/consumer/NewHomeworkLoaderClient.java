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

package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.support.LocationTransformer;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.homework.api.constant.CheatingTeacherStatus;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.homework.api.mapper.WechatHomeworkMapper;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLocationLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.*;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.GroupLoader;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class NewHomeworkLoaderClient implements NewHomeworkLoader {

    @ImportService(interfaceClass = NewHomeworkLoader.class)
    private NewHomeworkLoader remoteReference;

    @Inject private NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;

    @Override
    public List<WechatHomeworkMapper> loadHomeworkWithTime(Collection<Long> groupIds, Long studentId, Date startTime, Date endTime, Subject subject, GroupLoader groupLoader) {
        return remoteReference.loadHomeworkWithTime(groupIds, studentId, startTime, endTime, subject);
    }

    @Override
    public NewHomework load(String id) {
        if (!NewHomeworkUtils.isHomeworkId(id)) {
            return null;
        }
        return remoteReference.load(id);
    }

    @Override
    public Map<String, NewHomework> loads(Collection<String> ids) {
        ids = NewHomeworkUtils.filterIllegalHomeworkIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return remoteReference.loads(ids);
    }

    @Override
    public Map<Long, List<NewHomework.Location>> loadNewHomeworksByClazzGroupIds(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }
        groupIds = CollectionUtils.toLinkedList(groupIds);
        return remoteReference.loadNewHomeworksByClazzGroupIds(groupIds);
    }

    @Override
    public Map<Long, List<NewHomework.Location>> loadNewHomeworksByClazzGroupIds(Collection<Long> groupIds, Date begin, Date end) {
        return remoteReference.loadNewHomeworksByClazzGroupIds(groupIds, begin, end);
    }

    @Override
    public NewHomeworkBook loadNewHomeworkBook(String id) {
        if (id == null) {
            return null;
        }
        return remoteReference.loadNewHomeworkBook(id);
    }

    @Override
    public Map<String, NewHomeworkBook> loadNewHomeworkBooks(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadNewHomeworkBooks(ids);
    }

    @Override
    public Map<String, Object> loadHomeworkQuestions(HomeworkQuestionAnswerRequest request) {
        return remoteReference.loadHomeworkQuestions(request);
    }

    @Override
    public Map<String, Object> indexData(String homeworkId, Long studentId) {
        return remoteReference.indexData(homeworkId, studentId);
    }

    @Override
    public Map<String, Object> loadQuestionAnswer(HomeworkQuestionAnswerRequest request) {
        return remoteReference.loadQuestionAnswer(request);
    }

    @Override
    public Map<String, Object> loadWordTeachQuestionsAnswer(HomeworkQuestionAnswerRequest request) {
        return remoteReference.loadWordTeachQuestionsAnswer(request);
    }

    @Override
    public Map<String, Object> loadHomeworkCorrectQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {
        return remoteReference.loadHomeworkCorrectQuestions(homeworkId, objectiveConfigType, studentId);
    }

    @Override
    public Map<String, Object> loadCorrectQuestionAnswer(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {
        return remoteReference.loadCorrectQuestionAnswer(homeworkId, objectiveConfigType, studentId);
    }

    @Override
    public List<WechatHomeworkMapper> loadHomeworkWithTime(Collection<Long> groupIds, Long studentId, Date startTime, Date endTime, Subject subject) {
        return remoteReference.loadHomeworkWithTime(groupIds, studentId, startTime, endTime, subject);
    }

    //根据作业id查询错题/查询一个月内的错题
    @Override
    public Map<String, List<Map<String, Object>>> getStudentWrongQuestionIds(Long studentId, Subject subject, Collection<String> homeworkIds) {
        return remoteReference.getStudentWrongQuestionIds(studentId, subject, homeworkIds);
    }

    public void mathHomeworkDetail(Long studentId, String homeworkId, Collection<Long> studentIds, Map<String, Object> detail) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return;
        }
        NewHomework newHomework = loadNewHomework(homeworkId);

        //所有学生的作业结果
        Map<Long, NewHomeworkResult> homeworkResultMap = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentIds, false);
        if (MapUtils.isEmpty(homeworkResultMap)) {
            return;
        }
        Double myScore = 0d; //我的分数
        Long myDuration = 0L; //我的答题时长
        Double totalScore = 0d; //总分
        Double maxScore = 0d; //最高分
        Long totalDuration = 0L; //所有学生答题时长总和
        int hundredCount = 0; //100分的人数
        long wrongCount = 0L; //我的错题数
        int count = 0; //完成作业的人数
        List<Map<String, Object>> wrongList = new ArrayList<>(); //我的错题id
        String comment = "";
        for (Map.Entry<Long, NewHomeworkResult> result : homeworkResultMap.entrySet()) {
            LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = result.getValue().getPractices();
            if (MapUtils.isEmpty(practices)) {
                continue;
            }
            if (!result.getValue().isFinished()) {
                continue;
            }
            //累加完成作业的人数
            count++;
            //当前遍历这个记录的学生分数
            Double currentStudentScore = SafeConverter.toDouble(result.getValue().processScore());
            if (currentStudentScore == 100) hundredCount++;
            //总分
            totalScore += currentStudentScore;
            //最高分
            maxScore = currentStudentScore > maxScore ? currentStudentScore : maxScore;
            //当前遍历这个记录的学生的完成时间
            long currentStudentDuration = SafeConverter.toLong(result.getValue().processDuration());
            //总完成时间
            totalDuration += currentStudentDuration;
            if (studentId.equals(result.getKey())) {
                //评语
                comment = result.getValue().getComment();
                myDuration = currentStudentDuration;
                myScore = currentStudentScore;
                Map<String, List<Map<String, Object>>> studentWrongQuestionIds = getStudentWrongQuestionIds(studentId, null, Collections.singleton(homeworkId));
                Set<String> wrongQuestionIds = new HashSet<>();
                if (MapUtils.isNotEmpty(studentWrongQuestionIds)) {
                    String date = DateUtils.dateToString(newHomework.getCreateAt(), "yyyy.MM.dd");
                    List<Map<String, Object>> mapList = studentWrongQuestionIds.get(date);
                    if (CollectionUtils.isNotEmpty(mapList)) {
                        wrongQuestionIds = ((Set) mapList.get(0).get("qid"));
                    }
                }
                for (String id : wrongQuestionIds) {
                    Map<String, Object> wrong = new HashMap<>();
                    wrong.put("qid", id);
                    wrong.put("duration", 0);
                    wrong.put("correctRate", 0);
                    wrong.put("averageDuration", 0);
                    wrongList.add(wrong);
                }
            }
        }

        long averageDuration = 0;
        if (totalDuration > 0 && count > 0) {
            averageDuration = new BigDecimal(totalDuration).divide(new BigDecimal(count), 0, BigDecimal.ROUND_HALF_UP).longValue();
        }

        detail.put("wrongList", wrongList);
        detail.put("wrongCount", wrongCount);
        detail.put("score", myScore);
        detail.put("duration", myDuration);
        detail.put("maxScore", maxScore);
        detail.put("hundredCount", hundredCount);
        detail.put("count", count);
        detail.put("averageScore", count > 0 ? new BigDecimal(totalScore).divide(new BigDecimal(count), 0, BigDecimal.ROUND_HALF_UP).intValue() : 0);
        detail.put("averageDuration", averageDuration);
        NewHomeworkPrize newHomeworkPrize = loadNewHomeworkPrize(newHomework.toLocation());
        if (newHomeworkPrize != null && newHomeworkPrize.getDetails().containsKey(SafeConverter.toString(studentId))) {
            int beanCount = newHomeworkPrize.getDetails().get(SafeConverter.toString(studentId)).getWhat();
            detail.put("prize", beanCount);
        } else {
            detail.put("prize", 0);
        }
        detail.put("comment", comment);
    }

    @Override
    public Map<String, StudentHomeworkStat> loadStudentHomeworkStats(Collection<String> uniques) {
        if (CollectionUtils.isEmpty(uniques)) return Collections.emptyMap();
        return remoteReference.loadStudentHomeworkStats(uniques);
    }

    @Override
    public List<StudentHomeworkStat> loadClazzStudentHomeworkStats(Long clazzId) {
        if (clazzId == null) return Collections.emptyList();
        return remoteReference.loadClazzStudentHomeworkStats(clazzId);
    }

    @Override
    public List<StudentHomeworkStat> loadTeacherStudentHomeworkStats(Long teacherId) {
        if (teacherId == null) return Collections.emptyList();
        return remoteReference.loadTeacherStudentHomeworkStats(teacherId);
    }

    @Override
    public PossibleCheatingHomework getByTeacherIdAndHomeworkId(Long teacherId, String homeworkId, HomeworkType type) {
        return remoteReference.getByTeacherIdAndHomeworkId(teacherId, homeworkId, type);
    }

    @Override
    public PossibleCheatingTeacher loadByTeacherId(Long teacherId) {
        return remoteReference.loadByTeacherId(teacherId);
    }

    @Override
    public PossibleCheatingHomework loadPossibleCheatingHomeworkById(String id) {
        return remoteReference.loadPossibleCheatingHomeworkById(id);
    }

    @Override
    public List<PossibleCheatingHomework> findPossibleCheatingHomeworkListByDateRange(DateRange range) {
        return remoteReference.findPossibleCheatingHomeworkListByDateRange(range);
    }

    @Override
    public PossibleCheatingHomework loadPossibleCheatingHomeworkByTeacherIdAndHomeworkId(Long teacherId, String homeworkId, HomeworkType type) {
        return remoteReference.loadPossibleCheatingHomeworkByTeacherIdAndHomeworkId(teacherId, homeworkId, type);
    }

    @Override
    public Page<PossibleCheatingHomework> pageFindPossibleCheatingHomeworkByDateRange(DateRange range, Pageable pageable) {
        return remoteReference.pageFindPossibleCheatingHomeworkByDateRange(range, pageable);
    }

    @Override
    public List<PossibleCheatingHomework> findPossibleCheatingHomeworkListByTeacherIdAndHomeworkIds(Long teacherId, List<String> homeworkIds, HomeworkType type) {
        return remoteReference.findPossibleCheatingHomeworkListByTeacherIdAndHomeworkIds(teacherId, homeworkIds, type);
    }

    @Override
    public List<PossibleCheatingTeacher> findPossibleCheatingTeacherListByDateRangeAndStatus(DateRange range, CheatingTeacherStatus status) {
        return remoteReference.findPossibleCheatingTeacherListByDateRangeAndStatus(range, status);
    }

    @Override
    public List<PossibleCheatingTeacher> loadAllCheatingTeacher() {
        return remoteReference.loadAllCheatingTeacher();
    }

    @Override
    public List<PossibleCheatingTeacher> loadTeacherIdFieldWhereUpdateGteAndLt(Date start, Date end) {
        return remoteReference.loadTeacherIdFieldWhereUpdateGteAndLt(start, end);
    }

    @Override
    public List<PossibleCheatingTeacher> loadAllBlackTeachers() {
        return remoteReference.loadAllBlackTeachers();
    }

    @Override
    public List<PossibleCheatingTeacher> loadBlackStatusTeachers() {
        return remoteReference.loadBlackStatusTeachers();
    }

    @Override
    public PossibleCheatingTeacher loadPossibleCheatingTeacherByTeacherId(Long teacherId) {
        return remoteReference.loadByTeacherId(teacherId);
    }

    public List<StudentHomeworkStat.DataMapper> getStudentHomeworkStatByTeacherId(Long teacherId) {
        return remoteReference.getStudentHomeworkStatByTeacherId(teacherId);
    }

    public NewHomeworkPrize loadNewHomeworkPrize(NewHomework.Location location) {
        return remoteReference.loadNewHomeworkPrize(location);
    }

    @Override
    public List<NewHomeworkCard> loadNewHomeworkCard(Long teacherId) {
        return remoteReference.loadNewHomeworkCard(teacherId);
    }

    public NewHomeworkLocationLoader loadGroupHomeworks(Long groupId, Subject subject) {
        return loadGroupHomeworks(Collections.singleton(groupId), subject);
    }

    public NewHomeworkLocationLoader loadGroupHomeworks(Collection<Long> groupIds, Subject subject) {
        return loadGroupHomeworks(groupIds, Collections.singleton(subject));
    }

    public NewHomeworkLocationLoader loadGroupHomeworks(Collection<Long> groupIds, Collection<Subject> subjects) {
        Set<NewHomework.Location> locations = loadNewHomeworksByClazzGroupIds(groupIds, subjects).values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        return createNewHomeworkLocationLoader(locations);
    }

    private NewHomeworkLocationLoader createNewHomeworkLocationLoader(Collection<NewHomework.Location> locations) {
        LocationTransformer<NewHomework.Location, NewHomework> transformer = candidate -> {
            List<String> idList = candidate.stream()
                    .map(NewHomework.Location::getId)
                    .collect(Collectors.toList());
            Map<String, NewHomework> map = loads(idList);
            return idList.stream()
                    .map(map::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        };
        return new NewHomeworkLocationLoader(transformer, locations);
    }

    public Map<String, DubbingSyntheticHistory> loadDubbingSyntheticHistories(Collection<String> ids) {
        return remoteReference.loadDubbingSyntheticHistories(ids);
    }

    @Override
    public Map<String, Long> getAssignHomeWorkCount(Date todayBegin, Date todayEnd, Date yesBegin, Date yesEnd, Date weekBegin, Date weekEnd) {
        return remoteReference.getAssignHomeWorkCount(todayBegin, todayEnd, yesBegin, yesEnd, weekBegin, weekEnd);
    }
}
