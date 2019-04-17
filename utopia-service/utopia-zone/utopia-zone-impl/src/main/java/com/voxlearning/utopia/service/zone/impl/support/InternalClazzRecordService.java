package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.athena.bean.MarkThumbsUp;
import com.voxlearning.athena.bean.Marks;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkStudyMaster;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.consumer.NewAccomplishmentLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkPartLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkResultLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.entity.RewardCategory;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.zone.api.constant.ClazzRecordCardStatusEnum;
import com.voxlearning.utopia.service.zone.api.constant.ClazzRecordTypeEnum;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.ClazzRecordCardMapper;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.RecordLikeMapper;
import com.voxlearning.utopia.service.zone.impl.athena.ClazzSpaceClient;
import com.voxlearning.utopia.service.zone.impl.service.ClazzRecordHelper;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by Yuechen.Wang on 2017/4/28.
 */
@Named
public class InternalClazzRecordService {

    private static final Logger logger = LoggerFactory.getLogger(InternalClazzRecordService.class);

    @Inject private NewAccomplishmentLoaderClient newAccomplishmentLoaderClient;
    @Inject private NewHomeworkPartLoaderClient newHomeworkPartLoaderClient;
    @Inject private NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private ClazzRecordHelper clazzRecordHelper;
    @Inject private RewardLoaderClient rewardLoaderClient;
    @Inject private ClazzSpaceClient clazzSpaceClient;

    /**
     * 卡片信息处理（作业相关）
     */
    public ClazzRecordCardMapper internalLoadStudyMasterCard(Long userId, NewHomework.Location location) {

        ClazzRecordTypeEnum recordType = ClazzRecordTypeEnum.getFromSubject(location.getSubject());
        if (recordType == null) {
            return null;
        }

        ClazzRecordCardMapper cardMapper = new ClazzRecordCardMapper();

        cardMapper.setHomeworkId(location.getId());
        cardMapper.setRecordTypeEnum(recordType);
        cardMapper.setSubject(location.getSubject());
        cardMapper.setStartDate(DateUtils.dateToString(new Date(location.getStartTime()), "MM月dd日"));
        cardMapper.setGroupId(location.getClazzGroupId());

        // 未解锁
//        if (location.getStartTime() > System.currentTimeMillis()) {
//            cardMapper.setStatusEnum(ClazzRecordCardStatusEnum.UNLOCK);
//            return cardMapper;
//        }

        // 挑战中
        if (!location.isChecked()) {
            List<Long> classMates = studentLoaderClient.loadGroupStudentIds(location.getClazzGroupId());
            renderHwIngMapper(cardMapper, location, classMates.size(), userId);
            return cardMapper;
        }

        NewHomeworkStudyMaster studyMaster = newHomeworkPartLoaderClient
                .getNewHomeworkStudyMasterMap(Collections.singleton(location.getId())).get(location.getId());
        if (studyMaster == null || CollectionUtils.isEmpty(studyMaster.getMasterStudentList())) {
            cardMapper.setNotExist(true);
        } else {
            cardMapper.setNotExist(false);
            NewHomeworkStudyMaster.MasterStudent masterStudent = studyMaster.getMasterStudentList().get(0);
            Map<Long, NewHomeworkResult> homeworkResultMap =
                    newHomeworkResultLoaderClient.loadNewHomeworkResult(location, Collections.singletonList(masterStudent.getUserId()), false);
            clazzRecordHelper.fillUserInfo(cardMapper, masterStudent.getUserId());
            cardMapper.setHasGot(Collections.singletonList(masterStudent.getUserId()));
            NewHomeworkResult newHomeworkResult = homeworkResultMap.get(masterStudent.getUserId());
            if (newHomeworkResult != null) {
                cardMapper.setTime(newHomeworkResult.processDuration());
                cardMapper.setScore(newHomeworkResult.processScore());
            } else {
                cardMapper.setTime(0L);
                cardMapper.setScore(0);
            }

            updateLikeCount(cardMapper, 0L, Collections.emptyList());
        }

        cardMapper.setStatusEnum(ClazzRecordCardStatusEnum.DONE);
        cardMapper.setGroupId(location.getClazzGroupId());
        return cardMapper;
    }

    public ClazzRecordCardMapper internalLoadFocusCard(Long userId, NewHomework.Location homework) {
        //专注之星
        ClazzRecordCardMapper mapper = new ClazzRecordCardMapper();
        mapper.setRecordTypeEnum(ClazzRecordTypeEnum.FOCUS_STAR);
        mapper.setHomeworkId(homework.getId());
        mapper.setSubject(homework.getSubject());
        mapper.setStartDate(DateUtils.dateToString(new Date(homework.getStartTime()), "MM月dd日"));
        mapper.setGroupId(homework.getClazzGroupId());
        // 未解锁
//        if (homework.getStartTime() > System.currentTimeMillis()) {
//            mapper.setStatusEnum(ClazzRecordCardStatusEnum.UNLOCK);
//            return mapper;
//        }
        List<Long> classMates = studentLoaderClient.loadGroupStudentIds(homework.getClazzGroupId());
        if (homework.isChecked()) {
            Map<Long, NewHomeworkResult> homeworkResultMap =
                    newHomeworkResultLoaderClient.loadNewHomeworkResult(homework, classMates, false);

            List<NewHomeworkResult> homeworkResults = clazzRecordHelper.filterFocusRecords(homeworkResultMap);

            List<NewHomeworkResult> results = homeworkResults.stream()
                    .sorted((o1, o2) -> (Long.compare(getBreakTime(o1), getBreakTime(o2))))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(results)) {
                mapper.setNotExist(true);
                mapper.setStatusEnum(ClazzRecordCardStatusEnum.DONE);
                return mapper;
            }
            List<NewHomeworkResult> minBreakList = new ArrayList<>();
            minBreakList.add(results.get(0));

            long minTime = getBreakTime(results.get(0));
            for (int i = 1; i < results.size(); i++) {
                long breakTime = getBreakTime(results.get(i));
                if (breakTime > minTime) {
                    break;
                } else {
                    minBreakList.add(results.get(i));
                }
            }
            mapper.setHomeworkId(homework.getId());
            mapper.setNotExist(false);
            mapper.setCount(minBreakList.size());
            mapper.setTime(minTime);
            mapper.setScore(results.get(0).processScore());
            mapper.setHasGot(minBreakList.stream().map(NewHomeworkResult::getUserId).collect(Collectors.toList()));
            mapper.setGroupId(homework.getClazzGroupId());
            if (minBreakList.size() == 1) {
                clazzRecordHelper.fillUserInfo(mapper, minBreakList.get(0).getUserId());
            }
            updateLikeCount(mapper, 0L, Collections.emptyList());
            mapper.setStatusEnum(ClazzRecordCardStatusEnum.DONE);

        } else {
            renderHwIngMapper(mapper, homework, classMates.size(), userId);
        }

        return mapper;
    }

    public ClazzRecordCardMapper internalLoadFashionCard(Long clazzId, List<Long> classmates) {

        ClazzRecordCardMapper cardMapper = new ClazzRecordCardMapper(ClazzRecordTypeEnum.FASHION_STAR);
        cardMapper.setNotExist(true);


        if (CollectionUtils.isEmpty(classmates)) return cardMapper;

        DateRange dateRange = SchoolYear.newInstance().currentTermDateRange();
        Map<Long, Integer> mOrders = rewardLoaderClient.loadUserCollectOrdersInClazz(
                clazzId, RewardCategory.SubCategory.HEAD_WEAR.name(), dateRange.getStartDate()
        );

        if (MapUtils.isEmpty(mOrders)) return cardMapper;

        Map<Long, Integer> targetOrders = new HashMap<>();
        classmates.forEach(c -> {
            if (mOrders.get(c) != null) {
                targetOrders.put(c, mOrders.get(c));
            }
        });

        if (MapUtils.isEmpty(targetOrders)) return cardMapper;

        List<Integer> sortedOrderCount = targetOrders.values().stream()
                .filter(v -> v != null && v > 0)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        int maxOrderCount = sortedOrderCount.get(0);
        List<Long> top1 = new LinkedList<>();
        for (Map.Entry<Long, Integer> entry : targetOrders.entrySet()) {
            if (entry.getValue() != null && entry.getValue() >= maxOrderCount) {
                top1.add(entry.getKey());
            }

        }

        if (maxOrderCount > 0) {
            cardMapper.setNotExist(false);
            cardMapper.setStatusEnum(ClazzRecordCardStatusEnum.DONE);
            cardMapper.setCount(maxOrderCount);
            cardMapper.setHasGot(top1);
            updateLikeCount(cardMapper, clazzId, classmates);
            if (top1.size() == 1) {
                clazzRecordHelper.fillUserInfo(cardMapper, top1.get(0));
            }
        }

        return cardMapper;
    }

    public List<ClazzRecordCardMapper> internalLoadTop3FashionMapper(Long clazzId, List<Long> classmates) {
        if (clazzId == null || clazzId <= 0L || CollectionUtils.isEmpty(classmates)) {
            return Collections.emptyList();
        }

        // 按照班级维度查询
        DateRange dateRange = SchoolYear.newInstance().currentTermDateRange();
        Map<Long, Integer> mOrders = rewardLoaderClient.loadUserCollectOrdersInClazz(
                clazzId, RewardCategory.SubCategory.HEAD_WEAR.name(), dateRange.getStartDate()
        );
        if (MapUtils.isEmpty(mOrders)) return Collections.emptyList();

        // 过滤掉不是本班同学的情况
        mOrders.entrySet().removeIf(entity -> !classmates.contains(entity.getKey()));
        if (MapUtils.isEmpty(mOrders)) return Collections.emptyList();

        // 然后将 Map根据Key/Value排好序
        TreeMap<Integer, List<Long>> orderedMap = new TreeMap<>();
        for (Map.Entry<Long, Integer> result : mOrders.entrySet()) {
            if (result.getValue() > 0) {
                if (!orderedMap.containsKey(result.getValue())) {
                    orderedMap.put(result.getValue(), new ArrayList<>());
                }
                orderedMap.get(result.getValue()).add(result.getKey());
            }
        }

        if (MapUtils.isEmpty(orderedMap)) return Collections.emptyList();

        // 直接取出前三个使用
        return orderedMap.descendingMap().entrySet().stream().limit(3)
                .map(e -> {
                    ClazzRecordCardMapper mapper = new ClazzRecordCardMapper();
                    mapper.setRecordTypeEnum(ClazzRecordTypeEnum.FASHION_STAR);
                    mapper.setStatusEnum(ClazzRecordCardStatusEnum.ING);
                    mapper.setNotExist(true);
                    mapper.setCount(e.getKey());
                    List<Long> users = e.getValue();
                    mapper.setHasGot(users);
                    if (users.size() == 1) {
                        clazzRecordHelper.fillAloneMapper(users.get(0), mapper);
                    }
                    return mapper;
                })
                .collect(Collectors.toList());
    }

    public ClazzRecordCardMapper internalLoadWeekTopFashionMapper(Long clazzId, List<Long> classmates) {
        if (clazzId == null || clazzId <= 0L || CollectionUtils.isEmpty(classmates)) {
            return null;
        }

        // 按照班级维度查询
        Map<Long, Integer> mOrders = rewardLoaderClient.loadUserCollectOrdersInClazz(
                clazzId, RewardCategory.SubCategory.HEAD_WEAR.name(), WeekRange.current().getStartDate()
        );
        if (MapUtils.isEmpty(mOrders)) return null;

        // 过滤掉不是本班同学的情况
        mOrders.entrySet().removeIf(entity -> !classmates.contains(entity.getKey()));
        if (MapUtils.isEmpty(mOrders)) return null;

        // 然后将 Map根据Key/Value排好序
        TreeMap<Integer, List<Long>> orderedMap = new TreeMap<>();
        for (Map.Entry<Long, Integer> result : mOrders.entrySet()) {
            if (result.getValue() > 0) {
                if (!orderedMap.containsKey(result.getValue())) {
                    orderedMap.put(result.getValue(), new ArrayList<>());
                }
                orderedMap.get(result.getValue()).add(result.getKey());
            }
        }
        if (MapUtils.isEmpty(orderedMap)) return null;

        // 找出第一个, 封装
        Map.Entry<Integer, List<Long>> entry = orderedMap.descendingMap().firstEntry();
        ClazzRecordCardMapper mapper = new ClazzRecordCardMapper();
        mapper.setRecordTypeEnum(ClazzRecordTypeEnum.FASHION_STAR);
        mapper.setNotExist(false);
        mapper.setStatusEnum(ClazzRecordCardStatusEnum.ING);
        mapper.setCount(entry.getKey());
        List<Long> users = entry.getValue();
        mapper.setHasGot(users);
        if (users.size() == 1) {
            clazzRecordHelper.fillAloneMapper(users.get(0), mapper);
        }
        return mapper;
    }

    public ClazzRecordCardMapper internalLoadFriendShipCard(Long clazzId, List<Long> classmates) {
        List<Marks> marksList = new ArrayList<>();
        try {
            MarkThumbsUp markThumbsUp = clazzSpaceClient.getClazzSpace().queryLike(clazzId);
            if (markThumbsUp != null && CollectionUtils.isNotEmpty(markThumbsUp.getMarksList())) {
                marksList = markThumbsUp.getMarksList();
            }
        } catch (Exception ex) {
            logger.error("An error occurs when invoke athena interface, interface=queryLike, clazzId={}", clazzId, ex);
            return null;
        }

        ClazzRecordCardMapper cardMapper = new ClazzRecordCardMapper(ClazzRecordTypeEnum.FRIENDSHIP_STAR);
        cardMapper.setNotExist(true);
        marksList = marksList.stream()
                .filter(o -> o.getCount() != null)
                .filter(o -> classmates.contains(o.getUserId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(marksList)) {
            return cardMapper;
        }

        Map<Integer, List<Marks>> marks = marksList.stream().collect(groupingBy(Marks::getCount));
        Integer maxCount = marks.keySet().stream().sorted((o1, o2) -> Integer.compare(o2, o1)).findFirst().orElse(null);

        cardMapper = clazzRecordHelper.renderFriendshipCard(marks, maxCount);
        return cardMapper;
    }

    public ClazzRecordCardMapper internalLoadFullMarksCard(Collection<Long> groupIds, Long clazzId, List<Long> classmates) {
        groupIds = CollectionUtils.toLinkedHashSet(groupIds);
        Long[] groupIdArray = new Long[groupIds.size()];
        List<MarkThumbsUp> markThumbsUps;
        try {
            markThumbsUps = clazzSpaceClient.getClazzSpace().queryFullMarks(groupIds.toArray(groupIdArray));
        } catch (Exception ex) {
            logger.error("An error occurs when invoke athena interface, interface=queryFullMarks, clazzId={}", clazzId, ex);
            return null;
        }

        ClazzRecordCardMapper cardMapper = new ClazzRecordCardMapper(ClazzRecordTypeEnum.FULLMARKS_STAR);
        cardMapper.setNotExist(true);
        if (CollectionUtils.isEmpty(markThumbsUps)) {
            return cardMapper;
        }

        TreeMap<Integer, List<Long>> sortedMap = clazzRecordHelper.getFullMarkSortMap(markThumbsUps);
        if (MapUtils.isEmpty(sortedMap)) {
            return cardMapper;
        }
        List<Long> target = sortedMap.get(sortedMap.firstKey());
        int fullMarksCount = target.size();
        if (fullMarksCount > 0) {
            cardMapper.setNotExist(false);
            cardMapper.setStatusEnum(ClazzRecordCardStatusEnum.DONE);
            cardMapper.setCount(sortedMap.firstKey());
            Map<Long, List<Long>> map = studentLoaderClient.loadGroupStudentIds(groupIds);
            if (MapUtils.isNotEmpty(map)) {
                updateLikeCount(cardMapper, clazzId, classmates);
            }

            cardMapper.setHasGot(target);
            if (target.size() == 1) {
                clazzRecordHelper.fillUserInfo(cardMapper, target.get(0));
            }
        }

        return cardMapper;
    }

    /**
     * 渲染作业类 挑战中的mapper
     */
    private void renderHwIngMapper(ClazzRecordCardMapper cardMapper, NewHomework.Location location, int total, Long userId) {
        //读取当前作业信息
        String accomplishmentId = NewAccomplishment.ID.build(location.getCreateTime(),
                location.getSubject(), location.getId()).toString();
        NewAccomplishment newAccomplishment =
                newAccomplishmentLoaderClient.__loadNewAccomplishment(accomplishmentId);
        //设置当前完成作业人数
        if (newAccomplishment != null) {
            if (MapUtils.isEmpty(newAccomplishment.getDetails())) {
                cardMapper.setCount(0);
                cardMapper.setFinished(Collections.emptySet());
            } else {
                cardMapper.setCount(newAccomplishment.getDetails().size());
                cardMapper.setFinished(newAccomplishment.getDetails().keySet());
                cardMapper.setFinishedSelf(newAccomplishment.getDetails().keySet().contains(String.valueOf(userId)));
            }
        }
        cardMapper.setTotal(total);
        cardMapper.setStatusEnum(ClazzRecordCardStatusEnum.ING);
    }

    private long getBreakTime(NewHomeworkResult result) {
        long breakTime = Integer.MAX_VALUE;
        if (result.getFinishAt() != null && result.getUserStartAt() != null) {
            breakTime = (result.getFinishAt().getTime() - result.getUserStartAt().getTime()) / 1000 - result.processDuration();
        }
        return breakTime;
    }

    public void updateLikeCount(ClazzRecordCardMapper mapper, Long clazzId, List<Long> classmates) {
        if (mapper == null) {
            return;
        }

        if (mapper.getRecordTypeEnum() == null) {
            mapper.setLikeCount(0);
            return;
        }

        RecordLikeMapper param = new RecordLikeMapper();
        param.setHomeworkId(mapper.getHomeworkId());
        param.setClazzId(clazzId);
        param.setRecordTypeEnumName(mapper.getRecordTypeEnum().name());

        //转换成map 减少同学匹配运算复杂度
        List<RecordLikeMapper> exitsMappers;
        if (mapper.getRecordTypeEnum().isHomeworkRecord()) {
            exitsMappers = clazzRecordHelper.getClazzRecordHwLikeCacheManager().loadClickLikedList(param);
        } else {
            exitsMappers = clazzRecordHelper.getClazzRecordNonHwLikeCacheManager().loadTodayClickLikedList(param);
        }

        if (CollectionUtils.isEmpty(exitsMappers)) {
            mapper.setLikeCount(0);
            return;
        }
        mapper.setLikeCount((int) exitsMappers.stream().filter(r -> classmates.contains(r.getUserId())).count());
    }

}
