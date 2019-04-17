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

package com.voxlearning.utopia.service.business.impl.service.student;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.api.constant.MagicValueType;
import com.voxlearning.utopia.api.constant.MagicWaterType;
import com.voxlearning.utopia.api.constant.StudentMagicLevelName;
import com.voxlearning.utopia.business.api.StudentMagicCastleService;
import com.voxlearning.utopia.entity.activity.StudentMagicCastleRecord;
import com.voxlearning.utopia.entity.activity.StudentMagicLevel;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.footprint.client.UserActivityServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.user.api.constants.UserActivityType;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserActivity;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.temp.ActivityDateManager;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Summer Yang
 * @since 2015/12/2
 */
@Named
@Service(interfaceClass = StudentMagicCastleService.class)
@ExposeService(interfaceClass = StudentMagicCastleService.class)
public class StudentMagicCastleServiceImpl extends BusinessServiceSpringBean implements StudentMagicCastleService {

    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
    @Inject private UserActivityServiceClient userActivityServiceClient;
    @Inject private ActionServiceClient actionServiceClient;

    @Override
    public StudentMagicLevel loadStudentMagicLevel(Long studentId) {
        return studentMagicLevelPersistence.findByMagicianId(studentId);
    }

    @Override
    public boolean hasBindParentApp(Long studentId) {
        List<StudentParentRef> refList = studentLoaderClient.loadStudentParentRefs(studentId);
        if (CollectionUtils.isEmpty(refList)) {
            return false;
        }
        List<Long> parentIds = refList.stream().map(StudentParentRef::getParentId).collect(Collectors.toList());
        Map<Long, VendorAppsUserRef> userMap = vendorLoaderClient.loadVendorAppUserRefs("17Parent", parentIds);
        if (MapUtils.isNotEmpty(userMap)) {
            return true;
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> loadCurrentWeekMagicValueRank(StudentDetail detail) {
        // 获取我的同学
        List<User> users = userAggregationLoaderClient.loadLinkedStudentsByClazzId(detail.getClazzId(), detail.getId());
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        List<Long> studentIds = users.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, Long> historyMap = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_getUserBehaviorCounts(UserBehaviorType.STUDENT_MAGIC_VALUE_WEEK, studentIds)
                .getUninterruptibly();
        if (MapUtils.isEmpty(historyMap)) {
            return Collections.emptyList();
        }
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        List<Map<String, Object>> dataMap = new ArrayList<>();
        Map<Long, StudentMagicLevel> levelMap = studentMagicLevelPersistence.loadByMagicianIds(studentIds);
        if (MapUtils.isEmpty(levelMap)) {
            return Collections.emptyList();
        }
        for (Long studentId : studentIds) {
            Map<String, Object> map = new HashMap<>();
            map.put("studentId", studentId);
            long magicValue = historyMap.get(studentId) == null ? 0 : historyMap.get(studentId);
            if (magicValue <= 0) {
                continue;
            }
            map.put("magicValue", magicValue);
            map.put("levelName", StudentMagicLevelName.of(levelMap.get(studentId) == null ? 1 : levelMap.get(studentId).getLevel()).getLevelName());
            map.put("studentName", userMap.get(studentId).fetchRealname());
            dataMap.add(map);
        }
        // 排序
        dataMap = dataMap.stream().sorted((o1, o2) -> {
            int v1 = SafeConverter.toInt(o1.get("magicValue"));
            int v2 = SafeConverter.toInt(o2.get("magicValue"));
            return Integer.compare(v2, v1);
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dataMap)) {
            return Collections.emptyList();
        }
        int currentRank = 1;
        int currentCount = ConversionUtils.toInt(dataMap.get(0).get("magicValue"));
        for (int i = 0; i < dataMap.size(); i++) {
            Map<String, Object> element = dataMap.get(i);
            int count = ConversionUtils.toInt(element.get("magicValue"));
            if (count >= currentCount) {
                element.put("rank", currentRank);
            } else {
                currentRank = i + 1;
                element.put("rank", currentRank);
                currentCount = count;
            }
        }
        return dataMap;
    }


    // 获取本周唤醒成功次数排行
    @Override
    public List<Map<String, Object>> loadCurrentWeekActiveRank(StudentDetail detail) {
        // 获取我的同学
        List<User> users = userAggregationLoaderClient.loadLinkedStudentsByClazzId(detail.getClazzId(), detail.getId());
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        List<Long> studentIds = users.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, Long> historyMap = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_getUserBehaviorCounts(UserBehaviorType.STUDENT_MAGIC_ACTIVE_COUNT_WEEK, studentIds)
                .getUninterruptibly();
        if (MapUtils.isEmpty(historyMap)) {
            return Collections.emptyList();
        }
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        List<Map<String, Object>> dataMap = new ArrayList<>();
        for (Long studentId : studentIds) {
            Map<String, Object> map = new HashMap<>();
            map.put("studentId", studentId);
            long activeCount = historyMap.get(studentId) == null ? 0 : historyMap.get(studentId);
            if (activeCount <= 0) {
                continue;
            }
            map.put("activeCount", activeCount);
            map.put("studentName", userMap.get(studentId).fetchRealname());
            dataMap.add(map);
        }
        // 排序
        dataMap = dataMap.stream().sorted((o1, o2) -> {
            int v1 = SafeConverter.toInt(o1.get("activeCount"));
            int v2 = SafeConverter.toInt(o2.get("activeCount"));
            return Integer.compare(v2, v1);
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dataMap)) {
            return Collections.emptyList();
        }
        int currentRank = 1;
        int currentCount = ConversionUtils.toInt(dataMap.get(0).get("activeCount"));
        for (int i = 0; i < dataMap.size(); i++) {
            Map<String, Object> element = dataMap.get(i);
            int count = ConversionUtils.toInt(element.get("activeCount"));
            if (count >= currentCount) {
                element.put("rank", currentRank);
            } else {
                currentRank = i + 1;
                element.put("rank", currentRank);
                currentCount = count;
            }
        }
        return dataMap;
    }

    @Override
    public List<ActivateInfoMapper> loadClazzSleepMagicianList(Long studentId, Long clazzId) {
        if (studentId == null || clazzId == null) {
            return Collections.emptyList();
        }
        //获取可抢夺的学生名单   7天没有完成作业并且没有在抢夺中状态
        final Date one_weeks_date = DateUtils.calculateDateDay(new Date(), -7);
        List<User> clazzMates = userAggregationLoaderClient.loadLinkedClassmatesByClazzId(clazzId, studentId);
        if (CollectionUtils.isEmpty(clazzMates)) {
            return Collections.emptyList();
        }

        Set<Long> allStudentIds = clazzMates.stream().map(User::getId).collect(Collectors.toSet());
        Map<Long, List<UserActivity>> activityMap = userActivityServiceClient.getUserActivityService()
                .findUserActivities(allStudentIds)
                .getUninterruptibly();
        clazzMates = clazzMates.stream()
                .filter(s -> s.getCreateTime().before(one_weeks_date))
                .filter(s -> activityMap.get(s.getId()) != null && activityMap.get(s.getId()).stream()
                        .anyMatch(a -> a.getActivityType() == UserActivityType.LAST_HOMEWORK_TIME && a.getActivityTime().before(one_weeks_date)))
                .collect(Collectors.toList());

        List<ActivateInfoMapper> allMapper = new ArrayList<>();
        clazzMates.stream().forEach(s -> {
            ActivateInfoMapper am = new ActivateInfoMapper();
            am.setUserId(s.getId());
            am.setUserName(s.fetchRealname());
            am.setUserAvatar(s.fetchImageUrl());

            List<UserActivity> activities = activityMap.get(s.getId());
            if (CollectionUtils.isNotEmpty(activities)) {
                UserActivity activity = activities.stream().filter(a -> a.getActivityType() == UserActivityType.LAST_HOMEWORK_TIME).findFirst().orElse(null);
                if (activity != null) {
                    long diff = DateUtils.dayDiff(new Date(), activity.getActivityTime());
                    // 一层梦境
                    if (diff <= 30) {
                        am.setActiveLevel(1);
                    }
                    // 二层梦境
                    if (diff > 30) {
                        am.setActiveLevel(2);
                    }
                }
            }
            allMapper.add(am);
        });


        if (CollectionUtils.isEmpty(allMapper)) {
            return Collections.emptyList();
        }
        //获取在抢夺中的学生
        List<StudentMagicCastleRecord> sleepList = new LinkedList<>(studentMagicCastleRecordPersistence.findUnDisabledByClazzId(clazzId));
        if (CollectionUtils.isEmpty(sleepList)) {
            return allMapper;
        }
        //超过24小时的 重新扔到池子里
        List<StudentMagicCastleRecord> disableList = sleepList.stream()
                .filter(r -> new Date().after(DateUtils.calculateDateDay(r.getCreateDatetime(), 1)))
                .collect(Collectors.toList());
        disableList.forEach(r -> {
            studentMagicCastleRecordPersistence.disabled(r.getId());
        });

        //去除掉所有失效的抢夺
        sleepList.removeAll(disableList);
        if (CollectionUtils.isEmpty(sleepList)) {
            return allMapper;
        }

        Set<Long> activeUserIds = sleepList.stream()
                .map(StudentMagicCastleRecord::getActiveId)
                .collect(Collectors.toSet());

        List<ActivateInfoMapper> realMapper = new ArrayList<>();
        for (ActivateInfoMapper mapper : allMapper) {
            if (activeUserIds.contains(mapper.getUserId())) {
                continue;
            }
            realMapper.add(mapper);
        }
        return realMapper;
    }

    @Override
    public Map<String, Object> loadStudentActiveDetailInfo(Long studentId) {
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
        if (clazz == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> dataMap = new HashMap<>();
        // 本周总共发出的唤醒次数
        dataMap.put("totalCount", asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_getUserBehaviorCount(
                        UserBehaviorType.STUDENT_MAGIC_SEND_ACTIVE_COUNT_WEEK,
                        studentId,
                        0).getUninterruptibly());
        // 本周总共成功的唤醒次数
        dataMap.put("successCount", asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_getUserBehaviorCount(
                        UserBehaviorType.STUDENT_MAGIC_ACTIVE_COUNT_WEEK,
                        studentId,
                        0).getUninterruptibly());
        List<StudentMagicCastleRecord> activingRecordList = studentMagicCastleRecordPersistence.loadActivingRecordByMagicianId(studentId);
        //超过24小时的 从列表去除
        List<StudentMagicCastleRecord> disableList = activingRecordList.stream()
                .filter(r -> new Date().after(DateUtils.calculateDateDay(r.getCreateDatetime(), 1)))
                .collect(Collectors.toList());
        disableList.forEach(r -> {
            studentMagicCastleRecordPersistence.disabled(r.getId());
        });
        activingRecordList.removeAll(disableList);
        if (CollectionUtils.isEmpty(activingRecordList)) {
            dataMap.put("detailList", Collections.emptyList());
            return dataMap;
        }
        List<StudentMagicCastleRecord> recordList = activingRecordList.stream()
                .filter(r -> Objects.equals(r.getClazzId(), clazz.getId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(recordList)) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> detailList = new ArrayList<>();
        Map<Long, User> userMap = userLoaderClient.loadUsers(recordList.stream().map(StudentMagicCastleRecord::getActiveId).collect(Collectors.toList()));
        for (StudentMagicCastleRecord record : recordList) {
            if (!record.getDisabled()) {
                Map<String, Object> detailInfo = new HashMap<>();
                User user = userMap.get(record.getActiveId());
                if (user == null) {
                    continue;
                }
                detailInfo.put("userId", record.getActiveId());
                detailInfo.put("userName", user.fetchRealname());
                detailInfo.put("imgUrl", user.fetchImageUrl());
                detailInfo.put("success", record.getSuccess());
                if (!record.getSuccess()) {
                    Date endDate = DateUtils.calculateDateDay(record.getCreateDatetime(), 1);
                    detailInfo.put("endDate", DateUtils.dateToString(endDate, "yyyy/MM/dd HH:mm:ss"));
                }
                detailList.add(detailInfo);
            }
        }
        dataMap.put("detailList", detailList);
        return dataMap;
    }

    @Override
    public MapMessage activeMagician(Long magicianId, Long activeId, Integer activeLevel, StudentMagicCastleRecord.Source source) {
        if (magicianId == null || activeId == null || activeLevel == null) {
            return MapMessage.errorMessage("参数错误");
        }
        if (Objects.equals(magicianId, activeId)) {
            return MapMessage.errorMessage("对不起，你不能唤醒自己");
        }
        Long activeCount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .unflushable_getUserBehaviorCount(
                        UserBehaviorType.STUDENT_MAGIC_SEND_ACTIVE_COUNT_DAY,
                        magicianId,
                        0)
                .getUninterruptibly();
        if (activeCount >= 10) {
            return MapMessage.errorMessage("每天最多发出10次唤醒哦");
        }
        //判断该队员是不是可抢
        StudentMagicCastleRecord record = studentMagicCastleRecordPersistence.findByActiveIdIncludeDisabled(activeId)
                .stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .findFirst()
                .orElse(null);
        if (record != null) {
            return MapMessage.errorMessage("对不起，这个同学已经被别人唤醒了");
        }
        Clazz magicianClazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(magicianId);
        Clazz activeClazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(activeId);
        if (magicianClazz == null || activeClazz == null) {
            return MapMessage.errorMessage("对不起，班级不存在");
        }
        if (!Objects.equals(magicianClazz.getId(), activeClazz.getId())) {
            return MapMessage.errorMessage("对不起，这个同学不在你的班级");
        }
        //判断队员是否在本班级池子里
        List<ActivateInfoMapper> mappers = loadClazzSleepMagicianList(magicianId, magicianClazz.getId());
        if (CollectionUtils.isEmpty(mappers)) {
            return MapMessage.errorMessage("对不起，你的班级没有同学可以唤醒");
        }
        List<ActivateInfoMapper> filterList = mappers.stream()
                .filter(r -> Objects.equals(r.getUserId(), activeId))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterList)) {
            return MapMessage.errorMessage("对不起，这个同学已经被唤醒了");
        }
        // 获取本周魔法药水
        Integer waterCount = loadStudentMagicWater(magicianId);
        // 没有药水了
        if (waterCount <= 0) {
            StudentDetail captainDetail = studentLoaderClient.loadStudentDetail(magicianId);
            UserIntegral integral = captainDetail.getUserIntegral();
            if (integral == null || integral.getUsable() < 5) {
                return MapMessage.errorMessage("对不起，你没有足够的学豆");
            }
            // 扣除学豆
            IntegralHistory integralHistory = new IntegralHistory(magicianId, IntegralType.魔法城堡_产品平台, -5);
            integralHistory.setComment("魔法城堡唤醒魔法师消耗学豆");
            if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                logger.error("magic castle change integral faild, magician is ", magicianId);
                return MapMessage.errorMessage("唤醒失败");
            }
        } else {
            // 消耗药水
            asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .persistence_decrUserBehaviorCount(
                            UserBehaviorType.STUDENT_MAGIC_WATER,
                            magicianId,
                            1L,
                            WeekRange.current().getEndDate())
                    .awaitUninterruptibly();
        }
        StudentMagicCastleRecord castleRecord = new StudentMagicCastleRecord();
        castleRecord.setMagicianId(magicianId);
        castleRecord.setActiveId(activeId);
        castleRecord.setClazzId(magicianClazz.getId());
        castleRecord.setActiveLevel(activeLevel);
        castleRecord.setSource(source);
        studentMagicCastleRecordPersistence.insert(castleRecord);
        // 增加今天唤醒次数
        asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .unflushable_incUserBehaviorCount(
                        UserBehaviorType.STUDENT_MAGIC_SEND_ACTIVE_COUNT_DAY,
                        magicianId,
                        1L,
                        DateUtils.getCurrentToDayEndSecond())
                .awaitUninterruptibly();
        // 增加本周唤醒次数
        asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_incUserBehaviorCount(
                        UserBehaviorType.STUDENT_MAGIC_SEND_ACTIVE_COUNT_WEEK,
                        magicianId,
                        1L,
                        WeekRange.current().getEndDate())
                .awaitUninterruptibly();
        return MapMessage.successMessage("唤醒成功");
    }

    @Override
    public Integer loadStudentMagicWater(Long studentId) {
        Long waterCount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_getUserBehaviorCount(UserBehaviorType.STUDENT_MAGIC_WATER, studentId, -1)
                .getUninterruptibly();
        if (waterCount < 0) {
            // 表示缓存里没有 要给一个初始值 是否绑定了家长APP
            boolean isUse = hasBindParentApp(studentId);
            long defaultCount = 0;
            if (isUse) {
                defaultCount = MagicWaterType.BING_PARENT_APP.getValue();
            }
            // 初始值
            asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .persistence_incUserBehaviorCount(
                            UserBehaviorType.STUDENT_MAGIC_WATER,
                            studentId,
                            defaultCount,
                            WeekRange.current().getEndDate())
                    .awaitUninterruptibly();
            return SafeConverter.toInt(defaultCount);
        } else {
            return waterCount.intValue();
        }
    }

    //成功唤醒魔法师
    @Override
    public void activeMagicianSuccess(Long activeId) {
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(activeId);
        if (clazz == null) {
            return;
        }
        List<StudentMagicCastleRecord> allRecords = Collections.emptyList();
        if (activeId != null) {
            allRecords = studentMagicCastleRecordPersistence.findByActiveIdIncludeDisabled(activeId);
        }
        StudentMagicCastleRecord record = allRecords.stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .findFirst()
                .orElse(null);
        //没有记录， 或者已经完成奖励了，就返回
        if (record == null || record.getSuccess()) {
            // 没有 获取过期的没有完成的激活记录 72小时之内的最新数据 记录
            List<StudentMagicCastleRecord> records = allRecords.stream()
                    .filter(e -> SafeConverter.toBoolean(e.getDisabled()))
                    .collect(Collectors.toList());
            records = records.stream().filter(r -> r.getCreateDatetime().after(DateUtils.calculateDateDay(new Date(), -3)))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(records)) {
                records.sort((o1, o2) -> o1.getCreateDatetime().compareTo(o2.getCreateDatetime()));
                StudentMagicCastleRecord disabledRecord = MiscUtils.firstElement(records);
                if (disabledRecord != null) {
                    // 更新为过期激活
                    studentMagicCastleRecordPersistence.updateSuccessLevel(disabledRecord.getId(), 2);
                }
            }
            return;
        }
        //存在唤醒关系 判断是否满足24小时内
        if (new Date().after(DateUtils.calculateDateDay(record.getCreateDatetime(), 1))) {
            // 已经过期了， 只是因为没有人刷新活动页面， 所以这条记录没有删除 在此删除
            studentMagicCastleRecordPersistence.disabled(record.getId());
            // 判断是否满足72小时， 如果满足 打标机
            if (DateUtils.calculateDateDay(record.getCreateDatetime(), 3).after(new Date())) {
                // 更新为过期激活
                studentMagicCastleRecordPersistence.updateSuccessLevel(record.getId(), 2);
            }
            return;
        }

        //属于正常唤醒关系  改变状态， 奖励魔力值
        studentMagicCastleRecordPersistence.success(record.getId());
        //成长体系
        actionServiceClient.wakeupClassmate(record.getMagicianId());
        // 奖励魔力值
        MagicValueType valueType = MagicValueType.ACTIVE_STUDENT_SUCCESS;
        MagicWaterType waterType = MagicWaterType.ACTIVE_SUCCESS_LEVEL_1;
        if (record.getActiveLevel() == 2) {
            valueType = MagicValueType.ACTIVE_STUDENT_SUCCESS_LEVEL_2;
            waterType = MagicWaterType.ACTIVE_SUCCESS_LEVEL_2;
        }
        addMagicValue(record.getMagicianId(), valueType);
        // 添加唤醒机会 （药水）
        addMagicWater(record.getMagicianId(), waterType);
        // 活动期内 奖励学豆
        if (ActivityDateManager.isInActivity(ActivityDateManager.ActivityType.魔法城堡唤醒活动)) {
            Long activeCount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .persistence_getUserBehaviorCount(
                            UserBehaviorType.STUDENT_MAGIC_ACTIVITY_FIRST_SUCCESS,
                            record.getMagicianId(),
                            0)
                    .getUninterruptibly();
            int coin = 0;
            if (activeCount <= 0) {
                // 奖励30学豆
                coin = 30;
            } else {
                if (record.getActiveLevel() == 1) {
                    coin = 6;
                }
                if (record.getActiveLevel() == 2) {
                    coin = 10;
                }
            }
            if (coin > 0) {
                // 添加学豆
                IntegralHistory integralHistory = new IntegralHistory(record.getMagicianId(), IntegralType.魔法城堡_产品平台, coin);
                integralHistory.setComment("魔法城堡唤醒魔法师奖励");
                if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                    logger.error("student magic castle add integral failed, student {}, integral {}", record.getMagicianId(), coin);
                }
            }
            // 记录活动期内第一次唤醒成功次数
            asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .persistence_incUserBehaviorCount(
                            UserBehaviorType.STUDENT_MAGIC_ACTIVITY_FIRST_SUCCESS,
                            record.getMagicianId(),
                            1L,
                            DateUtils.stringToDate(ActivityDateManager.ActivityType.魔法城堡唤醒活动.getEndDate()))
                    .awaitUninterruptibly();
        }
        // 增加本周唤醒成功次数
        asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_incUserBehaviorCount(
                        UserBehaviorType.STUDENT_MAGIC_ACTIVE_COUNT_WEEK,
                        record.getMagicianId(),
                        1L,
                        WeekRange.current().getEndDate())
                .awaitUninterruptibly();
    }

    // 奖励魔力值
    @Override
    public void addMagicValue(Long magicianId, MagicValueType magicValueType) {
        if (magicianId == null || magicValueType == null) {
            return;
        }
        StudentMagicLevel magicLevel = studentMagicLevelPersistence.findByMagicianId(magicianId);
        if (magicLevel == null) {
            // 新添加一条记录
            StudentMagicLevel level = new StudentMagicLevel();
            level.setLevelValue(magicValueType.getValue());
            level.setLevel(1);
            level.setMagicianId(magicianId);
            level.setDisabled(false);
            studentMagicLevelPersistence.insert(level);
        } else {
            int level = getLevel(magicLevel.getLevel(), magicLevel.getLevelValue(), magicValueType.getValue());
            int exp = magicLevel.getLevelValue() + magicValueType.getValue();
            // 更新
            studentMagicLevelPersistence.updateLevel(magicianId, level);
            studentMagicLevelPersistence.updateLevelValue(magicianId, exp);
        }
        //记录历史
//        StudentMagicValueHistory history = new StudentMagicValueHistory();
//        history.setMagicianId(magicianId);
//        history.setValueType(magicValueType);
//        history.setLevelValue(magicValueType.getValue());
//        studentMagicValueHistoryPersistence.persist(history);
        // 更新本周获取魔力值
        asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_incUserBehaviorCount(UserBehaviorType.STUDENT_MAGIC_VALUE_WEEK,
                        magicianId, SafeConverter.toLong(magicValueType.getValue()), WeekRange.current().getEndDate())
                .awaitUninterruptibly();
    }

    public static int getLevel(Integer level, Integer levelValue, int addExp) {
        int temp1 = level * 6;//当前级别升级需要的总活跃值
        int temp2 = level * (level - 1) * 3;//当前级别最小活跃值
        int temp3 = levelValue - temp2;//当前经验值超出当前级别最小值的部分
        int expToNextLevel = temp1 - temp3;//当前级别（当前活跃值）升级到下一级需要的活跃值
        int tempLevel = level;
        while (expToNextLevel <= addExp) {
            tempLevel = ++level;
            addExp -= expToNextLevel;
            expToNextLevel = tempLevel * 6;
        }
        return tempLevel;
    }

    @Override
    public void addMagicWater(Long studentId, MagicWaterType magicWaterType) {
        Long waterCount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_getUserBehaviorCount(UserBehaviorType.STUDENT_MAGIC_WATER, studentId, -1)
                .getUninterruptibly();
        if (waterCount < 0) {
            // 表示缓存里没有 要给一个初始值 是否绑定了家长APP
            boolean isUse = hasBindParentApp(studentId);
            if (isUse) {
                // 初始值
                asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                        .persistence_incUserBehaviorCount(
                                UserBehaviorType.STUDENT_MAGIC_WATER,
                                studentId,
                                SafeConverter.toLong(MagicWaterType.BING_PARENT_APP.getValue()),
                                WeekRange.current().getEndDate())
                        .awaitUninterruptibly();
            }
        }
        asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_incUserBehaviorCount(
                        UserBehaviorType.STUDENT_MAGIC_WATER,
                        studentId,
                        SafeConverter.toLong(magicWaterType.getValue()),
                        WeekRange.current().getEndDate())
                .awaitUninterruptibly();
    }

    // 获取超过该等级的魔法师
    @Override
    public List<ActivateInfoMapper> loadSuperMagician(StudentDetail detail, Integer level) {
        // 获取我的同学
        List<User> users = userAggregationLoaderClient.loadLinkedStudentsByClazzId(detail.getClazzId(), detail.getId());
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        List<Long> studentIds = users.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, StudentMagicLevel> levelMap = studentMagicLevelPersistence.loadByMagicianIds(studentIds);
        if (MapUtils.isEmpty(levelMap)) {
            return Collections.emptyList();
        }
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        List<StudentMagicLevel> superLevels = levelMap.values().stream().filter(l -> l.getLevel() >= level).collect(Collectors.toList());
        List<ActivateInfoMapper> allMapper = new ArrayList<>();
        superLevels.forEach(s -> {
            User user = userMap.get(s.getMagicianId());
            if (user != null) {
                ActivateInfoMapper am = new ActivateInfoMapper();
                am.setUserId(user.getId());
                am.setUserName(user.fetchRealname());
                am.setUserAvatar(user.fetchImageUrl());
                allMapper.add(am);
            }
        });
        return allMapper;
    }

    // 获取班级最高等级魔法师
    @Override
    public Map<String, Object> loadMaxMagician(StudentDetail detail) {
        // 获取我的同学
        List<User> users = userAggregationLoaderClient.loadLinkedStudentsByClazzId(detail.getClazzId(), detail.getId());
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyMap();
        }
        List<Long> studentIds = users.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, StudentMagicLevel> levelMap = studentMagicLevelPersistence.loadByMagicianIds(studentIds);
        if (MapUtils.isEmpty(levelMap)) {
            return Collections.emptyMap();
        }

        StudentMagicLevel maxLevel = levelMap.values().stream()
                .sorted((o1, o2) -> Integer.compare(o2.getLevel(), o1.getLevel())).findFirst().get();
        if (maxLevel == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("magicianId", maxLevel.getMagicianId());
        dataMap.put("level", maxLevel.getLevel());
        dataMap.put("levelValue", maxLevel.getLevelValue());
        return dataMap;
    }

    @Override
    public List<Map<String, Object>> loadTotalMagicValueRank(StudentDetail detail) {
        // 获取我的同学
        List<User> users = userAggregationLoaderClient.loadLinkedStudentsByClazzId(detail.getClazzId(), detail.getId());
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        List<Long> studentIds = users.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, StudentMagicLevel> levelMap = studentMagicLevelPersistence.loadByMagicianIds(studentIds);
        if (MapUtils.isEmpty(levelMap)) {
            return Collections.emptyList();
        }
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        List<Map<String, Object>> dataMap = new ArrayList<>();
        for (StudentMagicLevel level : levelMap.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("studentId", level.getMagicianId());
            map.put("magicValue", level.getLevelValue());
            map.put("levelName", StudentMagicLevelName.of(level.getLevel()).getLevelName());
            map.put("studentName", userMap.get(level.getMagicianId()).fetchRealname());
            dataMap.add(map);
        }
        // 排序
        dataMap = dataMap.stream().sorted((o1, o2) -> {
            int v1 = SafeConverter.toInt(o1.get("magicValue"));
            int v2 = SafeConverter.toInt(o2.get("magicValue"));
            return Integer.compare(v2, v1);
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dataMap)) {
            return Collections.emptyList();
        }
        int currentRank = 1;
        int currentCount = ConversionUtils.toInt(dataMap.get(0).get("magicValue"));
        for (int i = 0; i < dataMap.size(); i++) {
            Map<String, Object> element = dataMap.get(i);
            int count = ConversionUtils.toInt(element.get("magicValue"));
            if (count >= currentCount) {
                element.put("rank", currentRank);
            } else {
                currentRank = i + 1;
                element.put("rank", currentRank);
                currentCount = count;
            }
        }
        return dataMap;
    }
}
