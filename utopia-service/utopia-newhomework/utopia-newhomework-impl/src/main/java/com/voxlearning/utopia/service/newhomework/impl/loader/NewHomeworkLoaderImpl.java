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

package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.support.LocationTransformer;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.homework.api.constant.CheatingTeacherStatus;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.homework.api.mapper.WechatHomeworkMapper;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLocationLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskStatus;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.*;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomeworkPractice;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkPractice;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.util.HomeworkTaskUtils;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.*;
import com.voxlearning.utopia.service.newhomework.impl.dao.shard.ShardHomeworkBookDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.shard.ShardHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.shard.ShardHomeworkPracticeDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkBookDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkPracticeDao;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.DoHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.HomeworkIndexDataContext;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.HomeworkIndexDataProcessor;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkCardTaskFilter;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkTransform;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.GroupLoader;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = NewHomeworkLoader.class)
@ExposeService(interfaceClass = NewHomeworkLoader.class)
public class NewHomeworkLoaderImpl implements NewHomeworkLoader {

    @Inject private RaikouSystem raikouSystem;

    @Inject private DoHomeworkProcessor doHomeworkProcessor;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private SubHomeworkBookDao subHomeworkBookDao;
    @Inject private SubHomeworkDao subHomeworkDao;
    @Inject private SubHomeworkPracticeDao subHomeworkPracticeDao;
    @Inject private NewHomeworkPartLoaderImpl newHomeworkPartLoader;
    @Inject private NewHomeworkPrizeDao newHomeworkPrizeDao;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private PossibleCheatingHomeworkDao possibleCheatingHomeworkDao;
    @Inject private PossibleCheatingTeacherDao possibleCheatingTeacherDao;
    @Inject private StudentHomeworkStatPersistence studentHomeworkStatPersistence;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Inject private HomeworkCardTaskFilter homeworkCardTaskFilter;
    @Inject private HomeworkTaskRecordDao homeworkTaskRecordDao;

    @Inject private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject private HomeworkIndexDataProcessor homeworkIndexDataProcessor;
    @Inject private DubbingSyntheticHistoryDao dubbingSyntheticHistoryDao;
    @Inject private ShardHomeworkDao shardHomeworkDao;
    @Inject private ShardHomeworkPracticeDao shardHomeworkPracticeDao;
    @Inject private ShardHomeworkBookDao shardHomeworkBookDao;

    @Inject private RaikouSDK raikouSDK;

    public NewHomework load(String id) {
        if (!NewHomeworkUtils.isHomeworkId(id)) {
            return null;
        }

        if (NewHomeworkUtils.isSubHomework(id)) {
            SubHomework homework = subHomeworkDao.load(id);
            SubHomeworkPractice practice = subHomeworkPracticeDao.load(id);

            return HomeworkTransform.SubHomeworkToNew(homework, practice);
        } else if (NewHomeworkUtils.isShardHomework(id)) {
            ShardHomework homework = shardHomeworkDao.load(id);
            ShardHomeworkPractice practice = shardHomeworkPracticeDao.load(id);
            return HomeworkTransform.ShardHomeworkToNew(homework, practice);
        } else {
            return null;
        }
    }

    @Override
    public Map<String, NewHomework> loads(Collection<String> ids) {
        ids = NewHomeworkUtils.filterIllegalHomeworkIds(ids);
        if (CollectionUtils.isEmpty(ids))
            return Collections.emptyMap();
        Set<String> subIds = new HashSet<>();
        Set<String> shardIds = new HashSet<>();
        ids.forEach(o -> {
            if (NewHomeworkUtils.isSubHomework(o)) {
                subIds.add(o);
            } else if (NewHomeworkUtils.isShardHomework(o)) {
                shardIds.add(o);
            }
        });
        Map<String, NewHomework> newHomeworkMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(subIds)) {
            Map<String, SubHomework> subHomeworkMap = subHomeworkDao.loads(subIds);
            if (CollectionUtils.isNotEmpty(subHomeworkMap.keySet())) {
                Map<String, SubHomeworkPractice> homeworkPracticeMap = subHomeworkPracticeDao.loads(subHomeworkMap.keySet());
                subHomeworkMap.keySet().forEach(id -> newHomeworkMap.put(id, HomeworkTransform.SubHomeworkToNew(subHomeworkMap.get(id), homeworkPracticeMap.get(id))));
            }
        }
        if (CollectionUtils.isNotEmpty(shardIds)) {
            Map<String, ShardHomework> shardHomeworkMap = shardHomeworkDao.loads(shardIds);
            if (CollectionUtils.isNotEmpty(shardHomeworkMap.keySet())) {
                Map<String, ShardHomeworkPractice> homeworkPracticeMap = shardHomeworkPracticeDao.loads(shardHomeworkMap.keySet());
                shardHomeworkMap.keySet().forEach(id -> newHomeworkMap.put(id, HomeworkTransform.ShardHomeworkToNew(shardHomeworkMap.get(id), homeworkPracticeMap.get(id))));
            }
        }
        return newHomeworkMap;
    }

    @Override
    public Map<Long, List<NewHomework.Location>> loadNewHomeworksByClazzGroupIds(Collection<Long> groupIds) {
        Set<Long> gids = CollectionUtils.toLinkedHashSet(groupIds);
        gids.remove(0L);    // FIXME: remove incorrect groupId zero from argument
        if (gids.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<SubHomework.Location>> subLocationMap = subHomeworkDao.loadSubHomeworksByClazzGroupIds(gids);
        Map<Long, List<ShardHomework.Location>> shardLocationMap = shardHomeworkDao.loadShardHomeworksByClazzGroupIds(gids);

        Set<NewHomework.Location> locationSet = new HashSet<>();
        if (MapUtils.isNotEmpty(subLocationMap)) {
            subLocationMap.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(o -> !o.isDisabled())
                    .forEach(o -> {
                        NewHomework.Location location = HomeworkTransform.SubHomeworkLocationToNew(o);
                        if (location != null) {
                            locationSet.add(location);
                        }
                    });
        }

        if (MapUtils.isNotEmpty(shardLocationMap)) {
            shardLocationMap.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(o -> !o.isDisabled())
                    .forEach(o -> {
                        NewHomework.Location location = HomeworkTransform.ShardHomeworkLocationToNew(o);
                        if (location != null) {
                            locationSet.add(location);
                        }
                    });
        }

        return locationSet.stream()
                .filter(o -> o.getType() != null)
                .filter(o -> o.getType().getTypeId() == NewHomeworkType.PlatformType)
                .filter(o -> o.getType() != NewHomeworkType.BasicReview) // 期末基础复习作业是直接根据homeworkId查，groupId维度过滤掉
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .collect(Collectors.groupingBy(NewHomework.Location::getClazzGroupId));
    }

    @Override
    public Map<Long, List<NewHomework.Location>> loadNewHomeworksByClazzGroupIds(Collection<Long> groupIds, Date begin, Date end) {
        Set<Long> gids = CollectionUtils.toLinkedHashSet(groupIds);
        gids.remove(0L);    // FIXME: remove incorrect groupId zero from argument
        if (gids.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<SubHomework.Location>> subLocationMap = subHomeworkDao.loadSubHomeworksByClazzGroupIdsWithTimeLimit(gids, begin, end);
        Map<Long, List<ShardHomework.Location>> shardLocationMap = shardHomeworkDao.loadShardHomeworksByClazzGroupIdsWithTimeLimit(gids, begin, end);

        Set<NewHomework.Location> locationSet = new HashSet<>();
        if (MapUtils.isNotEmpty(subLocationMap)) {
            subLocationMap.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(o -> !o.isDisabled())
                    .forEach(o -> {
                        if (HomeworkTransform.SubHomeworkLocationToNew(o) != null) {
                            locationSet.add(HomeworkTransform.SubHomeworkLocationToNew(o));
                        }
                    });
        }

        if (MapUtils.isNotEmpty(shardLocationMap)) {
            shardLocationMap.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(o -> !o.isDisabled())
                    .forEach(o -> {
                        if (HomeworkTransform.ShardHomeworkLocationToNew(o) != null) {
                            locationSet.add(HomeworkTransform.ShardHomeworkLocationToNew(o));
                        }
                    });
        }

        return locationSet.stream()
                .filter(o -> o.getType() != null)
                .filter(o -> o.getType().getTypeId() == NewHomeworkType.PlatformType)
                .filter(o -> o.getType() != NewHomeworkType.BasicReview) // 期末基础复习作业是直接根据homeworkId查，groupId维度过滤掉
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .collect(Collectors.groupingBy(NewHomework.Location::getClazzGroupId));
    }


    @Override
    public NewHomeworkBook loadNewHomeworkBook(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        if (NewHomeworkUtils.isSubHomework(id)) {
            return NewHomeworkBook.of(subHomeworkBookDao.load(id));
        } else if (NewHomeworkUtils.isShardHomework(id)) {
            return NewHomeworkBook.of(shardHomeworkBookDao.load(id));
        } else {
            return null;
        }
    }

    @Override
    public Map<String, NewHomeworkBook> loadNewHomeworkBooks(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }

        Set<String> subIds = new HashSet<>();
        Set<String> shardIds = new HashSet<>();
        ids.forEach(o -> {
            if (NewHomeworkUtils.isSubHomework(o)) {
                subIds.add(o);
            } else if (NewHomeworkUtils.isShardHomework(o)) {
                shardIds.add(o);
            }
        });

        Map<String, SubHomeworkBook> subMap = subHomeworkBookDao.loads(subIds);
        Map<String, ShardHomeworkBook> shardMap = shardHomeworkBookDao.loads(shardIds);
        Map<String, NewHomeworkBook> resultMap = new HashMap<>();
        for (Map.Entry<String, SubHomeworkBook> entry : subMap.entrySet()) {
            resultMap.put(entry.getKey(), NewHomeworkBook.of(entry.getValue()));
        }
        for (Map.Entry<String, ShardHomeworkBook> entry : shardMap.entrySet()) {
            resultMap.put(entry.getKey(), NewHomeworkBook.of(entry.getValue()));
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> loadHomeworkQuestions(HomeworkQuestionAnswerRequest request) {
        return doHomeworkProcessor.loadHomeworkQuestions(request);
    }

    @Override
    public Map<String, Object> indexData(String homeworkId, Long studentId) {
        HomeworkIndexDataContext homeworkIndexDataContext = new HomeworkIndexDataContext();
        homeworkIndexDataContext.setHomeworkId(homeworkId);
        homeworkIndexDataContext.setStudentId(studentId);
        homeworkIndexDataContext = homeworkIndexDataProcessor.process(homeworkIndexDataContext);
        return homeworkIndexDataContext.getResult();
    }

    @Override
    public Map<String, Object> loadQuestionAnswer(HomeworkQuestionAnswerRequest request) {
        return doHomeworkProcessor.questionAnswer(request);
    }

    @Override
    public Map<String, Object> loadWordTeachQuestionsAnswer(HomeworkQuestionAnswerRequest request) {
        return doHomeworkProcessor.loadWordTeachQuestionsAnswer(request);
    }

    @Override
    public Map<String, Object> loadHomeworkCorrectQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {
        return doHomeworkProcessor.loadHomeworkCorrectQuestions(homeworkId, objectiveConfigType, studentId);
    }

    @Override
    public Map<String, Object> loadCorrectQuestionAnswer(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {
        return doHomeworkProcessor.loadCorrectQuestionAnswer(homeworkId, objectiveConfigType, studentId);
    }

    //分学科翻页
    public Page<NewHomework.Location> loadGroupNewHomeworks(Collection<Long> groupIds, Subject subject, Pageable pageable) {

        Map<Long, List<NewHomework.Location>> groupResultMap = loadNewHomeworksByClazzGroupIds(groupIds);
        if (MapUtils.isEmpty(groupResultMap)) {
            return new PageImpl<>(Collections.<NewHomework.Location>emptyList(), pageable, 0);
        }

        List<NewHomework.Location> resultList = groupResultMap.values().stream()
                .flatMap(Collection::stream)
                .filter(o -> StringUtils.equalsIgnoreCase(o.getSubject().name(), subject.name()))
                .filter(o -> o.getType() != NewHomeworkType.OCR)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .collect(Collectors.toList());

        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        if (page < 0) {
            page = 0;
        }
        pageable = new PageRequest(page, size);
        int fromIndex = page * size;
        int toIndex = (page + 1) * size;
        if (toIndex >= resultList.size()) {
            toIndex = resultList.size();
        }
        if (fromIndex > toIndex) {
            fromIndex = toIndex;
        }

        return new PageImpl<>(new LinkedList<>(resultList.subList(fromIndex, toIndex)), pageable, resultList.size());
    }


    public Page<NewHomework.Location> loadGroupNewHomeworksWithTimeLimit(Collection<Long> groupIds, Subject subject, Date startDate, Date endDate, Pageable pageable) {

        Map<Long, List<NewHomework.Location>> groupResultMap = loadNewHomeworksByClazzGroupIds(groupIds, startDate, endDate);
        if (MapUtils.isEmpty(groupResultMap)) {
            return new PageImpl<>(Collections.<NewHomework.Location>emptyList(), pageable, 0);
        }

        List<NewHomework.Location> resultList = groupResultMap.values().stream()
                .flatMap(Collection::stream)
                .filter(o -> StringUtils.equalsIgnoreCase(o.getSubject().name(), subject.name()))
                .filter(o -> o.getType() != NewHomeworkType.OCR)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .collect(Collectors.toList());

        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        if (page < 0) {
            page = 0;
        }
        pageable = new PageRequest(page, size);
        int fromIndex = page * size;
        int toIndex = (page + 1) * size;
        if (toIndex >= resultList.size()) {
            toIndex = resultList.size();
        }
        if (fromIndex > toIndex) {
            fromIndex = toIndex;
        }

        return new PageImpl<>(new LinkedList<>(resultList.subList(fromIndex, toIndex)), pageable, resultList.size());
    }

    //
    public Page<NewHomework.Location> loadGroupNewHomeworks(Collection<Long> groupIds, Date startDate, Date endDate, Pageable pageable) {
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();

        Map<Long, List<NewHomework.Location>> groupResultMap = loadNewHomeworksByClazzGroupIds(groupIds);
        if (MapUtils.isEmpty(groupResultMap)) {
            return new PageImpl<>(Collections.<NewHomework.Location>emptyList(), pageable, 0);
        }

        List<NewHomework.Location> resultList = groupResultMap.values().stream()
                .flatMap(Collection::stream)
                .filter(o -> (o.getCreateTime() > startTime && o.getCreateTime() < endTime))
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .collect(Collectors.toList());

        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        if (page < 0) {
            page = 0;
        }
        pageable = new PageRequest(page, size);
        int fromIndex = page * size;
        int toIndex = (page + 1) * size;
        if (toIndex >= resultList.size()) {
            toIndex = resultList.size();
        }
        if (fromIndex > toIndex) {
            fromIndex = toIndex;
        }
        return new PageImpl<>(new LinkedList<>(resultList.subList(fromIndex, toIndex)), pageable, resultList.size());
    }

    @Override
    public Map<String, StudentHomeworkStat> loadStudentHomeworkStats(Collection<String> uniques) {
        return studentHomeworkStatPersistence.yetAnotherLoads(uniques);
    }

    @Override
    public List<StudentHomeworkStat> loadClazzStudentHomeworkStats(Long clazzId) {
        if (clazzId == null) return Collections.emptyList();
        Collection<String> ids = studentHomeworkStatPersistence.queryIdsByClazzId(clazzId);
        return new LinkedList<>(loadStudentHomeworkStats(ids).values());
    }

    @Override
    public List<StudentHomeworkStat> loadTeacherStudentHomeworkStats(Long teacherId) {
        if (teacherId == null) return Collections.emptyList();
        Collection<String> ids = studentHomeworkStatPersistence.queryIdsByTeacherId(teacherId);
        return new LinkedList<>(loadStudentHomeworkStats(ids).values());
    }

    @Override
    public PossibleCheatingHomework getByTeacherIdAndHomeworkId(Long teacherId, String homeworkId, HomeworkType type) {
        return possibleCheatingHomeworkDao.getByTeacherIdAndHomeworkId(teacherId, homeworkId, type);
    }

    @Override
    public PossibleCheatingTeacher loadByTeacherId(Long teacherId) {
        return possibleCheatingTeacherDao.loadByTeacherId(teacherId);
    }

    @Override
    public PossibleCheatingHomework loadPossibleCheatingHomeworkById(String id) {
        return possibleCheatingHomeworkDao.load(id);
    }

    @Override
    public List<PossibleCheatingHomework> findPossibleCheatingHomeworkListByDateRange(DateRange range) {
        return possibleCheatingHomeworkDao.getByDateRange(range);
    }

    @Override
    public PossibleCheatingHomework loadPossibleCheatingHomeworkByTeacherIdAndHomeworkId(Long teacherId, String homeworkId, HomeworkType type) {
        return possibleCheatingHomeworkDao.getByTeacherIdAndHomeworkId(teacherId, homeworkId, type);
    }

    @Override
    public Page<PossibleCheatingHomework> pageFindPossibleCheatingHomeworkByDateRange(DateRange range, Pageable pageable) {
        return possibleCheatingHomeworkDao.pageGetByDateRange(range, pageable);
    }

    @Override
    public List<PossibleCheatingHomework> findPossibleCheatingHomeworkListByTeacherIdAndHomeworkIds(Long teacherId, List<String> homeworkIds, HomeworkType type) {
        return possibleCheatingHomeworkDao.getByTeacherIdAndHomeworkIds(teacherId, homeworkIds, type);
    }

    @Override
    public List<PossibleCheatingTeacher> findPossibleCheatingTeacherListByDateRangeAndStatus(DateRange range, CheatingTeacherStatus status) {
        return possibleCheatingTeacherDao.loadByDateRangeAndStatus(range, status);
    }

    @Override
    public List<PossibleCheatingTeacher> loadAllCheatingTeacher() {
        return possibleCheatingTeacherDao.loadAllCheatingTeacher();
    }

    @Override
    public List<PossibleCheatingTeacher> loadTeacherIdFieldWhereUpdateGteAndLt(Date start, Date end) {
        return possibleCheatingTeacherDao.loadTeacherIdFieldWhereUpdateGteAndLt(start, end);
    }

    @Override
    public List<PossibleCheatingTeacher> loadAllBlackTeachers() {
        return possibleCheatingTeacherDao.loadAllBlackTeachers();
    }

    @Override
    public List<PossibleCheatingTeacher> loadBlackStatusTeachers() {
        return possibleCheatingTeacherDao.loadBlackStatusTeachers();
    }

    @Override
    public PossibleCheatingTeacher loadPossibleCheatingTeacherByTeacherId(Long teacherId) {
        return possibleCheatingTeacherDao.loadByTeacherId(teacherId);
    }


    /**
     * 根据老师ID查询老师当前所有学生完成作业的情况， 已经做了对于转校的兼容
     *
     * @param teacherId 老师id
     * @return List<StudentHomeworkStat.DataMapper>
     */
    @Override
    public List<StudentHomeworkStat.DataMapper> getStudentHomeworkStatByTeacherId(Long teacherId) {
        // 查询老师当前所有学生
        List<User> studentList = studentLoaderClient.loadTeacherStudents(teacherId);
        if (CollectionUtils.isEmpty(studentList)) {
            return Collections.emptyList();
        }
        List<Long> studentIdList = studentList.stream().map(User::getId).collect(Collectors.toList());
        // 查询所有班级学生做作业的情况
        List<StudentHomeworkStat> statList = loadTeacherStudentHomeworkStats(teacherId);
        statList = statList.stream().filter(t -> studentIdList.contains(t.getStudentId())).collect(Collectors.toList());
        // 这里可能会有学生转班的情况 需要做兼容 根据学生取合并的count
        Map<Long, List<StudentHomeworkStat>> statMap = statList.stream().collect(Collectors.groupingBy(StudentHomeworkStat::getStudentId));
        List<StudentHomeworkStat.DataMapper> dataList = new ArrayList<>();
        for (Map.Entry<Long, List<StudentHomeworkStat>> entry : statMap.entrySet()) {
            List<StudentHomeworkStat> list = entry.getValue();
            long count = 0;
            for (StudentHomeworkStat stat : list) {
                count = count + stat.getNormalHomeworkCount();
            }
            StudentHomeworkStat.DataMapper mapper = new StudentHomeworkStat.DataMapper();
            mapper.setStudentId(entry.getKey());
            mapper.setNormalHomeworkCount(count);
            dataList.add(mapper);
        }
        return dataList;
    }

    @Override
    public NewHomeworkPrize loadNewHomeworkPrize(NewHomework.Location location) {
        if (location == null) return null;

        String day = DayRange.newInstance(location.getCreateTime()).toString();
        NewHomeworkPrize.ID id = new NewHomeworkPrize.ID(day, location.getSubject(), location.getId());
        return newHomeworkPrizeDao.load(id.toString());
    }

    @Override
    public Map<String, List<Map<String, Object>>> getStudentWrongQuestionIds(Long studentId, Subject subject, Collection<String> homeworkIds) {
        Map<String, List<Map<String, Object>>> homeworkWrongIdMap = new HashMap<>();
        Set<NewHomework.Location> locationSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(homeworkIds)) {
            //带了作业ID进来。就只查一次作业的。
            Map<String, NewHomework> newHomeworkMap = loadNewHomeworks(homeworkIds);
            if (MapUtils.isEmpty(newHomeworkMap)) {
                return homeworkWrongIdMap;
            }
            for (NewHomework newHomework : newHomeworkMap.values()) {
                CollectionUtils.addNonNullElement(locationSet, newHomework.toLocation());
            }
        } else {
            //不带作业ID进来。查一个月内的。
            List<Group> groupMappers = raikouSystem.loadStudentGroups(studentId);
            if (CollectionUtils.isEmpty(groupMappers)) {
                return homeworkWrongIdMap;
            }
            Set<Long> groupIds = groupMappers.stream().map(Group::getId).collect(Collectors.toSet());
            Map<Long, List<NewHomework.Location>> homeworkMaps = loadNewHomeworksByClazzGroupIds(groupIds, subject);
            if (MapUtils.isEmpty(homeworkMaps)) {
                return homeworkWrongIdMap;
            }
            locationSet = homeworkMaps.values()
                    .stream()
                    .flatMap(List::stream)
                    .filter(p -> DateUtils.dayDiff(new Date(), new Date(p.getCreateTime())) < 30)
                    .collect(Collectors.toSet());
        }
        if (CollectionUtils.isEmpty(locationSet)) {
            return homeworkWrongIdMap;
        }
        Map<String, NewHomework.Location> homeworkLocationMap = locationSet.stream()
                .collect(Collectors.toMap(NewHomework.Location::getId, Function.identity()));


        Set<String> subProcessIds = new HashSet<>();
        //批量获取学生指定作业的完成情况
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultLoader.loadNewHomeworkResult(locationSet, studentId);
        //取出所有家长端app错题本支持的作业类型
        Set<ObjectiveConfigType> supportTypes = new HashSet<>();
        for (ObjectiveConfigType type : ObjectiveConfigType.values()) {
            if (type.isSupportErrorBook()) {
                supportTypes.add(type);
            }
        }
        for (NewHomeworkResult result : newHomeworkResults) {
            if (result == null) {
                continue;
            }
            for (ObjectiveConfigType type : supportTypes) {
                List<String> typeProcessIds = result.findHomeworkProcessIdsByObjectiveConfigType(type);
                if (CollectionUtils.isNotEmpty(typeProcessIds)) {
                    if (NewHomeworkUtils.isSubHomework(result.getHomeworkId()) || NewHomeworkUtils.isShardHomework(result.getHomeworkId())) {
                        subProcessIds.addAll(typeProcessIds);
                    }

                }
            }
        }

        List<BaseHomeworkProcessResult> processResults = findProcessResults(subProcessIds);
        Map<String, List<BaseHomeworkProcessResult>> processResultMap = processResults.stream().collect(Collectors.groupingBy(BaseHomeworkProcessResult::getHomeworkId));
        for (String newHomeworkId : processResultMap.keySet()) {
            NewHomework.Location location = homeworkLocationMap.get(newHomeworkId);
            List<BaseHomeworkProcessResult> homeworkProcessResults = processResultMap.get(newHomeworkId);
            String key = DateUtils.dateToString(new Date(location.getCreateTime()), "yyyy.MM.dd");
            Map<String, Object> map = new HashMap<>();
            map.put("qid", homeworkProcessResults.stream().map(BaseHomeworkProcessResult::getQuestionId).collect(Collectors.toSet()));
            map.put("wrongCount", homeworkProcessResults.stream().map(BaseHomeworkProcessResult::getQuestionId).count());
            map.put("homeworkId", newHomeworkId);
            map.put("homeworkType", HomeworkType.of(location.getSubject().name()));
            map.put("createTime", location.getCreateTime());
            if (homeworkWrongIdMap.containsKey(key)) {
                List<Map<String, Object>> mapList = homeworkWrongIdMap.get(key);
                mapList.add(map);
            } else {
                List<Map<String, Object>> mapList = new ArrayList<>();
                mapList.add(map);
                homeworkWrongIdMap.put(key, mapList);
            }
        }
        for (String key : homeworkWrongIdMap.keySet()) {
            if (homeworkWrongIdMap.get(key).size() > 1) {
                List<Map<String, Object>> mapList = homeworkWrongIdMap.get(key);
                mapList = mapList.stream().sorted((o1, o2) -> {
                    long t1 = SafeConverter.toLong(o1.get("createTime"));
                    long t2 = SafeConverter.toLong(o2.get("createTime"));
                    return Long.compare(t2, t1);
                }).collect(Collectors.toList());
                homeworkWrongIdMap.replace(key, mapList);
            }
        }
        return homeworkWrongIdMap;
    }

    private List<BaseHomeworkProcessResult> findProcessResults(Set<String> subProcessIds) {
        List<BaseHomeworkProcessResult> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subProcessIds)) {
            Map<String, SubHomeworkProcessResult> subMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(subProcessIds);
            if (MapUtils.isNotEmpty(subMap)) {
                List<BaseHomeworkProcessResult> subList = subMap.values()
                        .stream()
                        .filter(p -> p.getScore() != null)
                        .filter(p -> Boolean.FALSE == p.getGrasp())
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(subList)) {
                    resultList.addAll(subList);
                }
            }
        }
        return resultList;
    }

    @Override
    public List<WechatHomeworkMapper> loadHomeworkWithTime(Collection<Long> groupIds, Long studentId, Date startTime, Date endTime, Subject subject) {
        return loadHomeworkWithTime(groupIds, studentId, startTime, endTime, subject, groupLoaderClient);
    }

    @Override
    public List<NewHomeworkCard> loadNewHomeworkCard(Long teacherId) {
        Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        // 老师分组
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherIds, false);
        // group id -> subject map
        Map<Long, Subject> groupSubjectMap = new LinkedHashMap<>();
        Map<Long, List<Long>> clazzIdGroupIdsMap = new LinkedHashMap<>();
        teacherGroups.forEach((tId, groups) ->
                groups.stream()
                        .filter(g -> g.isTeacherGroupRefStatusValid(tId))
                        .forEach(group -> {
                            groupSubjectMap.put(group.getId(), group.getSubject());
                            clazzIdGroupIdsMap.computeIfAbsent(group.getClazzId(), k -> new ArrayList<>()).add(group.getId());
                        }));
        // 班级
        List<Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIdGroupIdsMap.keySet())
                .stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        // groupId -> clazzName map
        Map<Long, String> groupIdClazzNameMap = new LinkedHashMap<>();
        for (Clazz clazz : clazzs) {
            if (clazz != null && !clazz.isTerminalClazz()) {
                Long clazzId = clazz.getId();
                String clazzName = clazz.formalizeClazzName();
                List<Long> groupIds = clazzIdGroupIdsMap.getOrDefault(clazzId, Collections.emptyList());
                for (Long groupId : groupIds) {
                    groupIdClazzNameMap.put(groupId, clazzName);
                    if (teacherIds.size() > 1) {
                        // 包班制在班级名称后面加上学科
                        groupIdClazzNameMap.put(groupId, clazzName + "(" + groupSubjectMap.getOrDefault(groupId, Subject.ENGLISH).getValue() + ")");
                    }
                }
            }
        }
        Long mainTeacherId = teacherId;
        if (teacherIds.size() > 1) {
            // 包班制，获取当前老师id的主学科id
            mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
            if (mainTeacherId == null) {
                mainTeacherId = teacherId;
            }
        }
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
        Teacher mainTeacher = teacherMap.get(mainTeacherId);
        if (mainTeacher == null || mainTeacher.getSubject() == null) {
            return Collections.emptyList();
        }
        Subject mainSubject = mainTeacher.getSubject();

        List<NewHomeworkCard> cardList = new ArrayList<>();
        // 获取配置的任务
        List<HomeworkTask> taskList = homeworkCardTaskFilter.loadValidHomeworkTaskList();
        for (HomeworkTask homeworkTask : taskList) {
            List<Subject> subjects = homeworkTask.getTaskSubjects();
            if (subjects != null && subjects.stream().anyMatch(subject -> Objects.equals(subject, mainSubject))) {
                HomeworkTaskType taskType = homeworkTask.getHomeworkTaskType();
                switch (taskType) {
                    case DAILY_HOMEWORK:
                        cardList.add(generateDailyHomeworkCard(homeworkTask, mainTeacherId, taskType, mainSubject, groupIdClazzNameMap));
                        break;
                    case WEEKEND_HOMEWORK:
                        cardList.add(generateWeekendHomeworkCard(homeworkTask, mainTeacherId, taskType, mainSubject, groupIdClazzNameMap));
                        break;
                    case VACATION_HOMEWORK:
                        break;
                    case ACTIVITY_HOMEWORK:
                        cardList.add(generateActivityHomeworkCard(homeworkTask, mainTeacherId, taskType, mainSubject, groupIdClazzNameMap));
                        break;
                }
            }
        }
        return cardList;
    }

    private NewHomeworkCard generateDailyHomeworkCard(HomeworkTask homeworkTask,
                                                      Long mainTeacherId,
                                                      HomeworkTaskType taskType,
                                                      Subject mainSubject,
                                                      Map<Long, String> groupIdClazzNameMap) {
        // 日常作业，任务周期为周，使用当前周的开始日期为任务周期
        Integer taskId = homeworkTask.getTaskId();
        String taskPeriod = HomeworkTaskUtils.calculateDailyTaskPeriod();
        String recordId = HomeworkTaskRecord.generateId(mainTeacherId, taskId, taskType, taskPeriod);

        NewHomeworkCard newHomeworkCard = new NewHomeworkCard();
        // 根据主学科来判断任务要求到底是几天
        // 小英为4天，小数、小语为3天，包班制按主学科处理
        int taskNeedDays = mainSubject == Subject.ENGLISH ? 4 : 3;
        String taskName = "本周布置" + taskNeedDays + "天作业";
        String taskDescription = "本周布置" + taskNeedDays + "天（及以上）作业，即可领取5园丁豆奖励。";
        // 日常作业相关字段后端写死，不使用配置的数据
        newHomeworkCard.setTaskName(taskName);
        newHomeworkCard.setTaskType(HomeworkTaskType.DAILY_HOMEWORK);
        newHomeworkCard.setIntegralCount(5);
        newHomeworkCard.setTaskDescription(taskDescription);
        newHomeworkCard.setTaskId(recordId);
        newHomeworkCard.setTeacherId(mainTeacherId);
        newHomeworkCard.setSubject(mainSubject);

        HomeworkTaskRecord taskRecord = homeworkTaskRecordDao.load(recordId);
        Map<Integer, List<Long>> dailyDetails = Collections.emptyMap();
        if (taskRecord != null) {
            dailyDetails = taskRecord.findDailyDetails();
            // 库里面有记录 判断任务完成状态
            HomeworkTaskStatus taskStatus = taskRecord.getTaskStatus();
            newHomeworkCard.setTaskStatus(taskStatus);
            switch (taskStatus) {
                case UNFINISHED:
                    // 获取完成的天数
                    int finishedDayCount = dailyDetails.size();
                    newHomeworkCard.setProgress("进度:" + finishedDayCount + "/" + taskNeedDays);
                    break;
                case FINISHED:
                    newHomeworkCard.setProgress("快来领奖励吧");
                    break;
                case REWARDED:
                    newHomeworkCard.setProgress("奖励已领取");
                    break;
            }
        } else {
            // 库里面没有记录，任务未完成，进度0
            newHomeworkCard.setTaskStatus(HomeworkTaskStatus.UNFINISHED);
            newHomeworkCard.setProgress("进度:0/" + taskNeedDays);
        }
        // 处理详情
        List<Map<String, Object>> dayDetails = new ArrayList<>();
        // 获取当前是周几，周一为0，周日为6
        int currentDay = HomeworkTaskUtils.calculateDayOfWeek();
        for (int day = 0; day < 7; day++) {
            List<Map<String, Object>> clazzDetails = new ArrayList<>();
            List<Long> assignedGroupList = dailyDetails.getOrDefault(day, Collections.emptyList());
            for (Map.Entry<Long, String> entry : groupIdClazzNameMap.entrySet()) {
                Long groupId = entry.getKey();
                String clazzName = entry.getValue();
                boolean assigned = assignedGroupList.contains(groupId);
                String status = assigned ? "已布置" : (currentDay > day ? "未布置" : "待布置");
                clazzDetails.add(MapUtils.m("groupId", groupId, "clazzName", clazzName, "status", status));
            }
            boolean finished = CollectionUtils.isNotEmpty(assignedGroupList);
            dayDetails.add(MapUtils.m("finished", finished, "clazzDetails", clazzDetails));
        }
        newHomeworkCard.setTaskDetails(MapUtils.m("dayDetails", dayDetails, "currentDay", currentDay, "taskNeedDays", taskNeedDays));
        return newHomeworkCard;
    }

    private NewHomeworkCard generateWeekendHomeworkCard(HomeworkTask homeworkTask,
                                                        Long mainTeacherId,
                                                        HomeworkTaskType taskType,
                                                        Subject mainSubject,
                                                        Map<Long, String> groupIdClazzNameMap) {
        // 周末作业，任务周期为周，使用当前周的开始日期为任务周期
        Integer taskId = homeworkTask.getTaskId();
        String taskPeriod = HomeworkTaskUtils.calculateWeekendTaskPeriod();
        String recordId = HomeworkTaskRecord.generateId(mainTeacherId, taskId, taskType, taskPeriod);

        NewHomeworkCard newHomeworkCard = new NewHomeworkCard();
        newHomeworkCard.setTaskName("周五布置巩固作业");
        newHomeworkCard.setTaskDescription("周五布置作业且周末完成人数达到10人，即可领取5园丁豆奖励。");
        newHomeworkCard.setIntegralCount(5);
        newHomeworkCard.setTaskType(HomeworkTaskType.WEEKEND_HOMEWORK);
        newHomeworkCard.setTaskId(recordId);
        newHomeworkCard.setTeacherId(mainTeacherId);
        newHomeworkCard.setSubject(mainSubject);

        HomeworkTaskRecord taskRecord = homeworkTaskRecordDao.load(recordId);
        Map<Long, Boolean> weekendDetails = Collections.emptyMap();
        if (taskRecord != null) {
            weekendDetails = taskRecord.findWeekendDetails();
            // 库里面有记录，判断任务完成状态
            HomeworkTaskStatus taskStatus = taskRecord.getTaskStatus();
            newHomeworkCard.setTaskStatus(taskStatus);
            switch (taskStatus) {
                case UNFINISHED:
                    newHomeworkCard.setProgress("周末未到10人完成");
                    break;
                case FINISHED:
                    newHomeworkCard.setProgress("快来领奖励吧");
                    break;
                case REWARDED:
                    newHomeworkCard.setProgress("奖励已领取");
                    break;
            }
        } else {
            // 库里面没记录，没有布置作业
            newHomeworkCard.setTaskStatus(HomeworkTaskStatus.UNFINISHED);
            newHomeworkCard.setProgress("快来布置吧");
        }
        // 处理详情
        // 获取当前是周几，周一为0，周日为6
        int currentDay = HomeworkTaskUtils.calculateDayOfWeek();
        List<Map<String, Object>> clazzDetails = new ArrayList<>();
        for (Map.Entry<Long, String> entry : groupIdClazzNameMap.entrySet()) {
            Long groupId = entry.getKey();
            String clazzName = entry.getValue();
            boolean assigned = weekendDetails.containsKey(groupId);
            String status = assigned ? "已布置" : (currentDay == 4 ? "待布置" : "未布置");
            if (assigned && Objects.equals(true, weekendDetails.get(groupId))) {
                status = "已完成";
            }
            clazzDetails.add(MapUtils.m("groupId", groupId, "clazzName", clazzName, "status", status));
        }
        newHomeworkCard.setTaskDetails(MapUtils.m("clazzDetails", clazzDetails, "currentDay", currentDay));
        return newHomeworkCard;
    }

    private NewHomeworkCard generateActivityHomeworkCard(HomeworkTask homeworkTask,
                                                         Long mainTeacherId,
                                                         HomeworkTaskType taskType,
                                                         Subject mainSubject,
                                                         Map<Long, String> groupIdClazzNameMap) {
        Integer taskId = homeworkTask.getTaskId();
        String taskPeriod = HomeworkTaskUtils.calculateActivityTaskPeriod(homeworkTask.getStartTime(), homeworkTask.getEndTime());
        String recordId = HomeworkTaskRecord.generateId(mainTeacherId, taskId, taskType, taskPeriod);

        NewHomeworkCard newHomeworkCard = new NewHomeworkCard();
        newHomeworkCard.setTaskType(HomeworkTaskType.ACTIVITY_HOMEWORK);
        newHomeworkCard.setTaskName(homeworkTask.getTaskName());
        newHomeworkCard.setTaskDescription(homeworkTask.getTaskDescription());
        newHomeworkCard.setIntegralCount(homeworkTask.getIntegralCount());
        newHomeworkCard.setTaskStatus(HomeworkTaskStatus.UNFINISHED);
        newHomeworkCard.setTaskRules(homeworkTask.getTaskRules());
        newHomeworkCard.setTaskId(recordId);
        newHomeworkCard.setTeacherId(mainTeacherId);
        newHomeworkCard.setSubject(mainSubject);
        newHomeworkCard.setPcImgUrl(homeworkTask.getPcImgUrl());
        newHomeworkCard.setNativeImgUrl(homeworkTask.getNativeImgUrl());
        newHomeworkCard.setH5ImgUrl(homeworkTask.getH5ImgUrl());
        List<Map<String, Object>> clazzDetails = new ArrayList<>();
        HomeworkTaskRecord taskRecord = homeworkTaskRecordDao.load(recordId);
        Map<Long, Boolean> activityDetails = Collections.emptyMap();
        if (taskRecord != null) {
            activityDetails = taskRecord.findActivityDetails();
            // 库里面有记录，判断任务完成状态
            HomeworkTaskStatus taskStatus = taskRecord.getTaskStatus();
            newHomeworkCard.setTaskStatus(taskStatus);
            switch (taskStatus) {
                case UNFINISHED:
                    newHomeworkCard.setProgress("假期未到10人完成");
                    break;
                case FINISHED:
                    newHomeworkCard.setProgress("快来领奖励吧");
                    break;
                case REWARDED:
                    newHomeworkCard.setProgress("奖励已领取");
                    break;
            }
        } else {
            // 库里面没记录，没有布置作业
            newHomeworkCard.setTaskStatus(HomeworkTaskStatus.UNFINISHED);
            newHomeworkCard.setProgress("快来布置吧");
        }
        Date endDate = DateUtils.stringToDate("2017-05-31 00:00:00");
        Date now = new Date();
        for (Map.Entry<Long, String> entry : groupIdClazzNameMap.entrySet()) {
            Long groupId = entry.getKey();
            String clazzName = entry.getValue();
            boolean assigned = activityDetails.containsKey(groupId);
            String status = assigned ? "已布置" : (now.before(endDate) ? "待布置" : "未布置");
            if (assigned && Objects.equals(true, activityDetails.get(groupId))) {
                status = "已完成";
            }
            clazzDetails.add(MapUtils.m("groupId", groupId, "clazzName", clazzName, "status", status));
        }
        newHomeworkCard.setTaskDetails(MapUtils.m("clazzDetails", clazzDetails, "showToast", true, "toast", "练习截止时间在假期内（5月28日~5月30日）才能完成任务哦，是否继续布置作业"));
        return newHomeworkCard;
    }


    public List<WechatHomeworkMapper> loadHomeworkWithTime(Collection<Long> groupIds,
                                                           Long studentId,
                                                           Date startTime,
                                                           Date endTime,
                                                           Subject subject,
                                                           GroupLoader groupLoader) {
        if (studentId == null || subject == null) {
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(groupIds)) {
            List<Group> groupMappers = raikouSystem.loadStudentGroups(studentId);
            if (CollectionUtils.isEmpty(groupMappers)) {
                return new ArrayList<>();
            }
            groupIds = groupMappers.stream()
                    .filter(p -> subject == p.getSubject())
                    .map(Group::getId)
                    .collect(Collectors.toSet());
        }
        Map<Long, List<NewHomework.Location>> clazzGroupIdNewHomeworkMap = loadNewHomeworksByClazzGroupIds(groupIds, subject);
        List<NewHomework.Location> locationList = clazzGroupIdNewHomeworkMap.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(o -> Objects.equals(subject, o.getSubject()))
                .filter(e -> startTime == null || e.getCreateTime() > startTime.getTime())
                .filter(p -> endTime == null || p.getCreateTime() < endTime.getTime())
                .collect(Collectors.toList());
        List<WechatHomeworkMapper> mapperList = newHomeworkPartLoader.getAllHomeworkMapper(locationList, studentId);
        mapperList = mapperList.stream().sorted((o1, o2) -> Long.compare(o2.getCreateTime().getTime(), o1.getCreateTime().getTime())).collect(Collectors.toList());
        return mapperList;
    }

    public NewHomeworkLocationLoader loadGroupHomeworks(Collection<Long> groupIds, Subject subject) {
        return loadGroupHomeworks(groupIds, Collections.singleton(subject));
    }

    public NewHomeworkLocationLoader loadGroupHomeworks(Collection<Long> groupIds, Subject subject, Date begin, Date end) {
        return loadGroupHomeworks(groupIds, Collections.singleton(subject), begin, end);
    }

    public NewHomeworkLocationLoader loadGroupHomeworks(Collection<Long> groupIds, Collection<Subject> subjects, Date begin, Date end) {
        Set<NewHomework.Location> locations = loadNewHomeworksByClazzGroupIds(groupIds, subjects, begin, end).values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        return createNewHomeworkLocationLoader(locations);
    }

    public NewHomeworkLocationLoader loadGroupHomeworks(Collection<Long> groupIds, Collection<Subject> subjects) {
        Set<NewHomework.Location> locations = loadNewHomeworksByClazzGroupIds(groupIds, subjects).values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        return createNewHomeworkLocationLoader(locations);
    }

    public NewHomeworkLocationLoader loadIncludeIntelligentTeachingGroupHomeworks(Collection<Long> groupIds, Subject subject) {
        Set<NewHomework.Location> locationSet = loadNewHomeworksByClazzGroupIds(groupIds, subject).values().stream()
                .flatMap(List::stream)
                .filter(n -> n.isIncludeIntelligentTeaching() && n.isChecked())
                .collect(Collectors.toSet());
        return createNewHomeworkLocationLoader(locationSet);
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

    @Override
    public Map<String, DubbingSyntheticHistory> loadDubbingSyntheticHistories(Collection<String> ids) {
        return dubbingSyntheticHistoryDao.loads(ids);
    }

    @Override
    public Map<String, Long> getAssignHomeWorkCount(Date todayBegin, Date todayEnd, Date yesBegin, Date yesEnd, Date weekBegin, Date weekEnd) {
        Map<String, Long> result = Maps.newLinkedHashMap();
        Long todayCount = shardHomeworkDao.getHomeworkCount(todayBegin, todayEnd);
        Long yesCount = shardHomeworkDao.getHomeworkCount(yesBegin, yesEnd);
        Long weekCount = shardHomeworkDao.getHomeworkCount(weekBegin, weekEnd);
        result.put("今日", todayCount);
        result.put("昨日", yesCount);
        result.put("上周", weekCount);
        return result;
    }
}