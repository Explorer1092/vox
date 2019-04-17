package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.athena.bean.Homework;
import com.voxlearning.athena.bean.MarkThumbsUp;
import com.voxlearning.athena.bean.Marks;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkStudyMaster;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkPartLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkResultLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserAggregationLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.zone.api.ClazzRecordService;
import com.voxlearning.utopia.service.zone.api.constant.ClazzRecordCardStatusEnum;
import com.voxlearning.utopia.service.zone.api.constant.ClazzRecordTypeEnum;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.ClazzRecordCardMapper;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.ClazzRecordHwMapper;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.RecordLikeMapper;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.RecordSoundShareMapper;
import com.voxlearning.utopia.service.zone.impl.athena.ClazzSpaceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/2/27
 * Time: 11:50
 * 班级空间记录实现
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = ClazzRecordService.class, version = @ServiceVersion(version = "20170502")),
        @ExposeService(interfaceClass = ClazzRecordService.class, version = @ServiceVersion(version = "20170315"))
})
public class ClazzRecordServiceImpl extends SpringContainerSupport implements ClazzRecordService {

    // inject loader client in alphabetic order
    @Inject private ClazzSpaceClient clazzSpaceClient;
    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject private NewHomeworkPartLoaderClient newHomeworkPartLoaderClient;
    @Inject private NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private ClazzRecordHelper clazzRecordHelper;

    @Override
    public Map<NewHomework.Location, ClazzRecordHwMapper> loadUserHomeworkRecords(Long userId, Collection<NewHomework.Location> homework) {
        if (userId == null || userId <= 0 || CollectionUtils.isEmpty(homework)) {
            return Collections.emptyMap();
        }

        Map<NewHomework.Location, ClazzRecordHwMapper> retMap = new HashMap<>();

        List<NewHomeworkResult> resultList = newHomeworkResultLoaderClient.loadNewHomeworkResult(homework, userId, false);

        resultList = resultList.stream().filter(BaseHomeworkResult::isFinished).collect(Collectors.toList());

        List<String> homeworkIds = homework.stream().map(NewHomework.Location::getId).collect(Collectors.toList());
        List<Homework> bigDataList = new LinkedList<>();
        String[] homeworkArray = new String[homeworkIds.size()];
        try {
            bigDataList = clazzSpaceClient.getClazzSpace().queryHomeworkInfo(homeworkIds.toArray(homeworkArray));
        } catch (Exception e) {
            logger.error("big data interface queryHomeworkInfo is error for :", e);
        }
        Map<String, Homework> bigDataResult = bigDataList.stream().collect(Collectors.toMap(Homework::getHomeworkId, p -> p));

        Map<String, NewHomeworkResult> resultMap = resultList.stream().collect(Collectors.toMap(NewHomeworkResult::getHomeworkId, t -> t));

        for (NewHomework.Location location : homework) {
            NewHomeworkResult result = resultMap.get(location.getId());
            ClazzRecordHwMapper mapper = new ClazzRecordHwMapper();
            mapper.setFinish(false);
            mapper.setSubject(location.getSubject());
            mapper.setHomeworkId(location.getId());
            mapper.setCreateTime(new Date(location.getCreateTime()));

            //大数据计算
            Homework bigDataInfo = bigDataResult.get(location.getId());
            if (bigDataInfo != null) {
                if (CollectionUtils.isNotEmpty(bigDataInfo.getMaxScoreStudentList())) {
                    mapper.setHighest(bigDataInfo.getMaxScoreStudentList().contains(SafeConverter.toString(userId)));
                }
                if (CollectionUtils.isNotEmpty(bigDataInfo.getMinDuaStudentList())) {
                    mapper.setFastest(bigDataInfo.getMaxScoreStudentList().contains(SafeConverter.toString(userId)));
                }
            }

            if (result != null) {
                mapper.setFinish(result.isFinished());
                mapper.setBreakTime(clazzRecordHelper.calBreakTime(result));
                mapper.setRepair(result.getRepair());
                mapper.setTime(result.processDuration());
                mapper.setScore(result.processScore());
            }

            retMap.put(location, mapper);
        }

        return retMap;
    }


    @Override
    public List<NewHomework.Location> loadHomeworkList(Long groupId) {
        if (groupId == null || groupId <= 0) {
            return Collections.emptyList();
        }

        List<NewHomework.Location> homeworkLocations = newHomeworkLoaderClient
                .loadNewHomeworksByClazzGroupIds(Collections.singleton(groupId)).get(groupId);

        if (CollectionUtils.isEmpty(homeworkLocations)) {
            return Collections.emptyList();
        }

        DateRange termRange = SchoolYear.newInstance().currentTermDateRange();

        return homeworkLocations.stream()
                .filter(p -> !p.isDisabled())
                .filter(p -> p.getCreateTime() >= termRange.getStartTime())
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .collect(Collectors.toList());
    }


    private Map<Integer, List<NewHomeworkResult>> queryTop3Focus(NewHomework.Location newlyHomework) {
        if (newlyHomework == null) {
            return Collections.emptyMap();
        }
        List<Long> studentIds = studentLoaderClient.loadGroupStudentIds(newlyHomework.getClazzGroupId());
        Map<Long, NewHomeworkResult> homeworkResultMap =
                newHomeworkResultLoaderClient.loadNewHomeworkResult(newlyHomework, studentIds, false);

        if (MapUtils.isEmpty(homeworkResultMap)) {
            return Collections.emptyMap();
        }

        List<NewHomeworkResult> homeworkResults = clazzRecordHelper.filterFocusRecords(homeworkResultMap);

        Map<Integer, List<NewHomeworkResult>> result = new HashMap<>();

        // 根据做题中断时间分组
        Map<Long, List<NewHomeworkResult>> choiceMap =
                homeworkResults.stream().collect(Collectors.groupingBy(this::calBreakTime));


        // 获取专注top3
        List<Long> top3Key = choiceMap.keySet().stream().sorted().limit(3).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(top3Key)) {
            for (int i = 0; i < top3Key.size(); i++) {
                result.put(i, choiceMap.get(top3Key.get(i)));
            }
        }
        return result;

    }

    @Override
    public Map<String, NewHomeworkResult> queryHomeworkRecords(Collection<NewHomework.Location> locations, Long userId) {
        if (CollectionUtils.isEmpty(locations) || userId == null) {
            return Collections.emptyMap();
        }
        Long groupId = new ArrayList<>(locations).get(0).getClazzGroupId();
        Map<String, NewHomeworkResult> studentHomeworkMap = queryStudentHomework(userId, groupId);

        if (MapUtils.isEmpty(studentHomeworkMap)) {
            return Collections.emptyMap();
        }

        List<NewHomeworkResult> result = new LinkedList<>();

        for (NewHomework.Location location : locations) {
            NewHomeworkResult newHomeworkResult = studentHomeworkMap.get(location.getId());
            if (newHomeworkResult == null) continue;
            result.add(newHomeworkResult);
        }

        return result.stream().collect(Collectors.toMap(NewHomeworkResult::getHomeworkId, (p) -> p));
    }

    @Override
    public void recordSoundShare(RecordSoundShareMapper mapper) {
        if (mapper == null) {
            return;
        }
        clazzRecordHelper.getClazzRecordSoundShareManager().share(mapper);
    }

    @Override
    public List<RecordSoundShareMapper> queryRecordSoundShare(Long clazzId, Long userId) {
        if (clazzId == null || userId == null) {
            return Collections.emptyList();
        }

        List<RecordSoundShareMapper> recordSoundShareMappers = clazzRecordHelper.getClazzRecordSoundShareManager().loadSharedList(clazzId);
        if (CollectionUtils.isEmpty(recordSoundShareMappers)) {
            return Collections.emptyList();
        }

        List<User> classmates = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazzId, userId);
        if (CollectionUtils.isEmpty(classmates)) {
            return Collections.emptyList();
        }
        //转换成map 减少同学匹配运算复杂度
        Map<Long, List<RecordSoundShareMapper>> recordSoundShareMapperMap =
                recordSoundShareMappers.stream().collect(Collectors.groupingBy(RecordSoundShareMapper::getUserId));

        List<RecordSoundShareMapper> target = new LinkedList<>();
        //匹配同一个分组的同学
        classmates.forEach(classmate -> {
            if (recordSoundShareMapperMap.containsKey(classmate.getId())) {
                target.addAll(recordSoundShareMapperMap.get(classmate.getId()));
            }
        });

        return target.stream()
                .sorted((t1, t2) -> t2.getCreateTime().compareTo(t1.getCreateTime()))
                .collect(Collectors.toList());
    }

    @Override
    public List<RecordSoundShareMapper> $directlyLoadFromCache(Long clazzId, Long userId) {
        if (clazzId == null) {
            return Collections.emptyList();
        }
        List<RecordSoundShareMapper> recordList = clazzRecordHelper.getClazzRecordSoundShareManager().loadSharedList(clazzId);
        if (CollectionUtils.isEmpty(recordList)) {
            return Collections.emptyList();
        }
        return recordList.stream()
                .filter(record -> userId == null || userId.equals(record.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public void $clearNaughtyRecord(Long clazzId, Long userId, String uri) {
        if (clazzId == null || userId == null) {
            return;
        }
        clazzRecordHelper.getClazzRecordSoundShareManager().deleteRecord(clazzId, userId, uri);
    }


    @Override
    public void like(RecordLikeMapper mapper) {
        if (mapper == null) {
            return;
        }
        ClazzRecordTypeEnum typeEnum = ClazzRecordTypeEnum.safeParse(mapper.getRecordTypeEnumName());
        if (typeEnum == null) {
            return;
        }
        if (recordLiked(mapper)) {
            return;
        }

        if (typeEnum.isHomeworkRecord()) {
            clazzRecordHelper.getClazzRecordHwLikeCacheManager().clickLiked(mapper);
        } else {
            clazzRecordHelper.getClazzRecordNonHwLikeCacheManager().clickLiked(mapper);
        }
    }

    @Override
    public boolean recordLiked(RecordLikeMapper mapper) {
        if (mapper == null) {
            return false;
        }

        ClazzRecordTypeEnum typeEnum = ClazzRecordTypeEnum.safeParse(mapper.getRecordTypeEnumName());
        if (typeEnum == null) {
            return false;
        }

        List<RecordLikeMapper> exitsMappers;
        if (typeEnum.isHomeworkRecord()) {
            exitsMappers = clazzRecordHelper.getClazzRecordHwLikeCacheManager().loadClickLikedList(mapper);
        } else {
            exitsMappers = clazzRecordHelper.getClazzRecordNonHwLikeCacheManager().loadTodayClickLikedList(mapper);
        }

        return CollectionUtils.isNotEmpty(exitsMappers)
                && exitsMappers.stream().filter(m -> Objects.equals(mapper.getUserId(), m.getUserId())).count() > 0;
    }

    @Override
    public List<RecordLikeMapper> queryRecordLike(RecordLikeMapper mapper) {
        if (mapper == null) {
            return Collections.emptyList();
        }

        ClazzRecordTypeEnum typeEnum = ClazzRecordTypeEnum.safeParse(mapper.getRecordTypeEnumName());
        if (typeEnum == null) {
            return Collections.emptyList();
        }

        List<User> classmates = userAggregationLoaderClient.loadLinkedStudentsByClazzId(mapper.getClazzId(), mapper.getUserId());
        if (CollectionUtils.isEmpty(classmates)) {
            return Collections.emptyList();
        }
        //转换成map 减少同学匹配运算复杂度
        List<RecordLikeMapper> exitsMappers;
        if (typeEnum.isHomeworkRecord()) {
            exitsMappers = clazzRecordHelper.getClazzRecordHwLikeCacheManager().loadClickLikedList(mapper);
        } else {
            exitsMappers = clazzRecordHelper.getClazzRecordNonHwLikeCacheManager().loadTodayClickLikedList(mapper);
        }

        if (CollectionUtils.isEmpty(exitsMappers)) {
            return Collections.emptyList();
        }

        Map<Long, RecordLikeMapper> recordSoundShareMapperMap = exitsMappers
                .stream()
                .collect(Collectors.toMap(RecordLikeMapper::getUserId, o -> o));

        List<RecordLikeMapper> target = new LinkedList<>();
        //匹配同一个分组的同学
        classmates.forEach(classmate -> {
            if (recordSoundShareMapperMap.containsKey(classmate.getId())) {
                target.add(recordSoundShareMapperMap.get(classmate.getId()));
            }
        });

        return target.stream()
                .sorted((t1, t2) -> t2.getCreateTime().compareTo(t1.getCreateTime()))
                .collect(Collectors.toList());

    }

    /**
     * 获取当前学期的作业列表
     */
    @Override
    public List<NewHomework.Location> loadHomeworkList(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        groupIds = CollectionUtils.toLinkedHashSet(groupIds);
        return newHomeworkLoaderClient.loadNewHomeworksByClazzGroupIds(groupIds)
                .values().stream()
                .flatMap(Collection::stream)
                .filter(l -> (!l.isDisabled() && l.getCreateTime() >= SchoolYear.newInstance().currentTermDateRange().getStartTime()))
                .sorted((t1, t2) -> Long.compare(t2.getCreateTime(), t1.getCreateTime()))
                .collect(Collectors.toList());

//        List<NewHomework.Location> homeworkLocations = new LinkedList<>();

//        Map<Long, List<NewHomework.Location>> homeworkMap =
//                newHomeworkLoaderClient.loadNewHomeworksByClazzGroupIds(groupIds);
//        if (MapUtils.isEmpty(homeworkMap)) {
//            return Collections.emptyList();
//        }
//
//        for (List<NewHomework.Location> arr : homeworkMap.values()) {
//            homeworkLocations.addAll(arr);
//        }

//        homeworkLocations = homeworkLocations.stream()
//                .filter(l -> (!l.isDisabled() && l.getCreateTime() >= SchoolYear.newInstance().currentTermDateRange().getStartTime()))
//                .sorted((t1, t2) -> Long.compare(t2.getCreateTime(), t1.getCreateTime()))
//                .collect(Collectors.toList());
//
//        return homeworkLocations;

    }

    @Override
    public List<ClazzRecordCardMapper> queryTop3FocusMapper(String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) {
            return Collections.emptyList();
        }

        NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
        if (newHomework == null) {
            return Collections.emptyList();
        }

        Map<Integer, List<NewHomeworkResult>> homeworks = queryTop3Focus(newHomework.toLocation());
        if (MapUtils.isEmpty(homeworks)) {
            return Collections.emptyList();
        }
        List<ClazzRecordCardMapper> result = new LinkedList<>();
        //封装mapper
        for (int i = 0; i < 3; i++) {
            List<NewHomeworkResult> newHomeworkResults = homeworks.get(i);
            //因为排名是有序的 当获取当前排名没有人获得的时候， 下面的名次肯定也没人获取了
            if (CollectionUtils.isEmpty(newHomeworkResults)) {
                break;
            }
            ClazzRecordCardMapper cardMapper = new ClazzRecordCardMapper();
            cardMapper.setRecordTypeEnum(ClazzRecordTypeEnum.FOCUS_STAR);
            cardMapper.setStatusEnum(ClazzRecordCardStatusEnum.DONE);
            clazzRecordHelper.renderDoneFocusCardMapper(newHomeworkResults, cardMapper);
            result.add(cardMapper);
        }

        return result;
    }

    @Override
    public List<ClazzRecordCardMapper> queryTop3StudyMasterMapper(String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) return Collections.emptyList();
        NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
        if (newHomework == null) return Collections.emptyList();

        List<ClazzRecordCardMapper> result = new LinkedList<>();
        NewHomeworkStudyMaster studyMaster = newHomeworkPartLoaderClient
                .getNewHomeworkStudyMasterMap(Collections.singleton(homeworkId)).get(homeworkId);
        if (studyMaster == null || CollectionUtils.isEmpty(studyMaster.getMasterStudentList())) {
            return Collections.emptyList();
        }

        List<NewHomeworkStudyMaster.MasterStudent> students = studyMaster.getMasterStudentList();
        List<Long> studentIds = students.stream().map(NewHomeworkStudyMaster.MasterStudent::getUserId).collect(Collectors.toList());
        Map<Long, NewHomeworkResult> homeworkResultMap = newHomeworkResultLoaderClient.loadNewHomeworkResult(
                newHomework.toLocation(), studentIds, false
        );

        int limit = Integer.min(students.size(), 3);
        for (int i = 0; i < limit; i++) {
            NewHomeworkStudyMaster.MasterStudent masterStudent = students.get(i);
            User user = userLoaderClient.loadUser(masterStudent.getUserId());
            ClazzRecordCardMapper cardMapper = new ClazzRecordCardMapper();
            cardMapper.setStatusEnum(ClazzRecordCardStatusEnum.DONE);
            cardMapper.setHomeworkId(homeworkId);
            cardMapper.setRecordTypeEnum(ClazzRecordTypeEnum.getFromSubject(newHomework.getSubject()));
            cardMapper.setNotExist(false);
            cardMapper.setImage(user.fetchImageUrl());
            cardMapper.setHeadWear(clazzRecordHelper.getHeadWear(user.getId()));
            cardMapper.setHasGot(Collections.singletonList(user.getId()));
            cardMapper.setGroupId(newHomework.getClazzGroupId());
            cardMapper.setStudentName(user.fetchRealname());
            //补充分数 和 做题时间
            NewHomeworkResult newHomeworkResult = homeworkResultMap.get(masterStudent.getUserId());
            if(newHomeworkResult != null) {
                cardMapper.setScore(newHomeworkResult.processScore());
                cardMapper.setTime(newHomeworkResult.processDuration());
            }else{
                cardMapper.setScore(0);
                cardMapper.setTime(0L);
            }
            result.add(cardMapper);
        }

        return result;
    }

    @Override
    public Double getAvgScore(String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) return 0D;
        NewHomework targetHomework = newHomeworkLoaderClient.load(homeworkId);
        if (targetHomework == null) return 0D;

        List<Long> studentIds = studentLoaderClient.loadGroupStudentIds(targetHomework.getClazzGroupId());
        if (CollectionUtils.isEmpty(studentIds)) return 0D;

        Map<Long, NewHomeworkResult> homeworkResultMap =
                newHomeworkResultLoaderClient.loadNewHomeworkResult(targetHomework.toLocation(), studentIds, false);

        if (MapUtils.isEmpty(homeworkResultMap)) return 0D;
        // 平均分数
        return homeworkResultMap.values().stream()
                .filter(BaseHomeworkResult::isFinished)
                .filter(r -> r.processScore() != null)
                .mapToInt(BaseHomeworkResult::processScore)
                .average()
                .orElse(0D);
    }

    @Override
    public List<ClazzRecordCardMapper> queryFullMarksTop3(List<Long> groupIds, Long clazzId) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        Long[] groupIdArray = new Long[groupIds.size()];
        List<MarkThumbsUp> markThumbsUps = null;
        try {
            markThumbsUps = clazzSpaceClient.getClazzSpace().queryFullMarks(groupIds.toArray(groupIdArray));
        } catch (Exception e) {
            logger.error(" big data interface is error for :", e);
        }

        TreeMap<Integer, List<Long>> sortedMap = clazzRecordHelper.getFullMarkSortMap(markThumbsUps);

        if (MapUtils.isEmpty(sortedMap)) {
            return Collections.emptyList();
        }
        List<ClazzRecordCardMapper> result = new LinkedList<>();
        int count = 0;

        for (Map.Entry<Integer, List<Long>> entry : sortedMap.entrySet()) {
            if (count > 2) {
                break;
            }
            List<Long> studentIds = entry.getValue();
            if (CollectionUtils.isEmpty(studentIds)) {
                return Collections.emptyList();
            }
            ClazzRecordCardMapper cardMapper = clazzRecordHelper.renderFullMarksCard(studentIds, entry.getKey());
            result.add(cardMapper);
            count++;
        }
        return result;
    }

    @Override
    public List<ClazzRecordCardMapper> queryFriendShipTop3(Long clazzId, List<Long> classmates) {
        List<Marks> marksList = new LinkedList<>();
        try {
            MarkThumbsUp markThumbsUp = clazzSpaceClient.getClazzSpace().queryLike(clazzId);
            marksList = markThumbsUp.getMarksList();
        } catch (Exception e) {
            logger.error(" big data interface queryLike(clazzId:{}) is error for :", clazzId, e);
        }

        if (CollectionUtils.isEmpty(marksList)) {
            return Collections.emptyList();
        }

        marksList = marksList.stream()
                .filter(o -> o.getCount() != null)
                .filter(o -> classmates.contains(o.getUserId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(marksList)) {
            return Collections.emptyList();
        }

        Map<Integer, List<Marks>> marks = marksList.stream().collect(groupingBy(Marks::getCount));

        List<Integer> markCounts = marks.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        List<ClazzRecordCardMapper> result = new LinkedList<>();
        int limit = markCounts.size() > 3 ? 3 : markCounts.size();
        for (int i = 0; i < limit; i++) {
            result.add(clazzRecordHelper.renderFriendshipCard(marks, markCounts.get(i)));
        }

        return result;
    }

    @Override
    public ClazzRecordCardMapper queryWeekTopFullMarks(List<Long> groupIds, Long clazzId) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return null;
        }
        Long[] groupIdArray = new Long[groupIds.size()];
        List<MarkThumbsUp> markThumbsUps = null;
        try {
            markThumbsUps = clazzSpaceClient.getClazzSpace().queryFullMarksWeek(groupIds.toArray(groupIdArray));
        } catch (Exception e) {
            logger.error(" big data interface is error for :", e);
        }

        TreeMap<Integer, List<Long>> sortedMap = clazzRecordHelper.getFullMarkSortMap(markThumbsUps);

        if (MapUtils.isEmpty(sortedMap)) {
            return null;
        }
        List<Long> weekUserIds = sortedMap.firstEntry().getValue();
        if (CollectionUtils.isEmpty(weekUserIds)) {
            return null;
        }

        return clazzRecordHelper.renderFullMarksCard(weekUserIds, sortedMap.firstEntry().getKey());
    }

    @Override
    public ClazzRecordCardMapper queryWeekTopFriendShip(Long clazzId, List<Long> classmates) {
        List<Marks> marksList = new LinkedList<>();
        try {
            MarkThumbsUp markThumbsUp = clazzSpaceClient.getClazzSpace().queryLikeWeek(clazzId);
            marksList = markThumbsUp.getMarksList();
        } catch (Exception e) {
            logger.error(" big data interface queryLike(clazzId:{}) is error for :", clazzId, e);
        }

        if (CollectionUtils.isEmpty(marksList)) {
            return null;
        }

        Map<Integer, List<Marks>> marks = marksList.stream().filter(o -> o.getCount() != null).collect(groupingBy(Marks::getCount));
        marksList = marksList.stream()
                .filter(o -> o.getCount() != null)
                .filter(o -> classmates.contains(o.getUserId()))
                .collect(Collectors.toList());

        List<Integer> markCounts = marksList.stream()
                .sorted(((o1, o2) -> o2.getCount().compareTo(o1.getCount())))
                .map(Marks::getCount).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(markCounts)) {
            return null;
        }

        return clazzRecordHelper.renderFriendshipCard(marks, markCounts.get(0));
    }

    /**
     * 计算作业中断时间
     */
    private Long calBreakTime(NewHomeworkResult newHomeworkResult) {
        Long allTime = (newHomeworkResult.getFinishAt().getTime() - newHomeworkResult.getUserStartAt().getTime()) / 1000;
        return allTime - newHomeworkResult.processDuration();
    }


    /**
     * 获取用户本学期所有完成的作业
     *
     * @param userId  用户id
     * @param groupId 分组id
     */
    private Map<String, NewHomeworkResult> queryStudentHomework(Long userId, Long groupId) {
        if (userId == null || groupId == null) {
            return Collections.emptyMap();
        }

        List<NewHomework.Location> loadHomeworkList = loadHomeworkList(groupId);
        if (CollectionUtils.isEmpty(loadHomeworkList)) {
            return Collections.emptyMap();
        }

        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultLoaderClient.loadNewHomeworkResult(loadHomeworkList, userId, false);

        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            return Collections.emptyMap();
        }

        return newHomeworkResults.stream()
                .collect(Collectors.toMap(NewHomeworkResult::getHomeworkId, Function.identity(),
                        (u, v) -> {
                            logger.error("Duplicate key found when query student homework, homeworkId={}", u.getHomeworkId());
                            return u;
                        }, LinkedHashMap::new));
    }

}
