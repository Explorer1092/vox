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

package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.entity.like.RecordLikeInfo;
import com.voxlearning.utopia.service.action.api.document.UserAttendanceCount;
import com.voxlearning.utopia.service.action.api.support.ClazzAttendanceInfo;
import com.voxlearning.utopia.service.action.api.support.SchoolAttendanceRank;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import com.voxlearning.utopia.service.action.client.ActionLoaderClient;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.client.UserLikeServiceClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.mapper.StudentAttendanceRankMapper;
import com.voxlearning.washington.mapper.StudentClazzAttendanceRankMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 4/8/2016
 */
@Controller
@RequestMapping(value = "/studentMobile/sign")
public class MobileStudentSignController extends AbstractMobileController {

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private ActionLoaderClient actionLoaderClient;
    @Inject private ActionServiceClient actionServiceClient;
    @Inject private UserLikeServiceClient userLikeServiceClient;

    @Inject private RaikouSDK raikouSDK;

    /**
     * 查询学生签到头条
     */
    @RequestMapping(value = "/headline.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage headline() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(currentUserId());
            if (null == clazz) return MapMessage.errorMessage("您还没有加入班级");
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(clazz.getSchoolId())
                    .getUninterruptibly();
            if (null == school) return MapMessage.errorMessage("未查询到学校信息");

            ClazzAttendanceInfo clazzAttendanceInfo = actionLoaderClient.getRemoteReference().getClazzAttendanceInfo(school.getId(), clazz.getId());
            if (null == clazzAttendanceInfo) return MapMessage.successMessage();
            Map<String, UserAttendanceCount> userAttendanceCountMap = actionLoaderClient.getRemoteReference().getUserAttendanceCountCurrentMonth(Collections.singletonList(currentUserId()));
            UserAttendanceCount userAttendanceCount = null;
            if (MapUtils.isNotEmpty(userAttendanceCountMap)) {
                userAttendanceCount = CollectionUtils.get(userAttendanceCountMap, 0).getValue();
            }

            return MapMessage.successMessage()
                    .add("signIn", clazzAttendanceInfo)
                    .add("signed", null != userAttendanceCount && LocalDateTime.ofInstant(userAttendanceCount.getUpdateTime().toInstant(), ZoneId.systemDefault()).getDayOfYear() == LocalDateTime.now().getDayOfYear());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 学生签到
     */
    @RequestMapping(value = "/submit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage signSubmit() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(currentUserId());
            if (null == clazz) return MapMessage.errorMessage("您还没有加入班级");
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(clazz.getSchoolId())
                    .getUninterruptibly();
            if (null == school) return MapMessage.errorMessage("未查询到学校信息");

            List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findStudentIdsByClazzId(clazz.getId());
            int clazzStudentCount = studentIds.size();

            actionServiceClient.studentAttendance(currentUserId(), clazz.getId(), school.getId(), clazzStudentCount);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 查询班级签到排行
     */
    @RequestMapping(value = "/rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rank() {
        if (studentUnLogin()) return MapMessage.errorMessage("请重新登录");

        try {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(currentUserId());
            if (null == clazz) return MapMessage.errorMessage("您还没有加入班级");

            List<Long> userIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findStudentIdsByClazzId(clazz.getId());
            Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);

            Map<String, UserAttendanceCount> userAttendanceCountMap = actionLoaderClient.getRemoteReference().getUserAttendanceCountCurrentMonth(userMap.keySet());
            if (MapUtils.isEmpty(userAttendanceCountMap)) return MapMessage.successMessage();

            List<UserAttendanceCount> userAttendanceCounts = userAttendanceCountMap.values()
                    .stream()
                    .sorted((o1, o2) -> {
                        if (Objects.equals(o1.getCount(), o2.getCount())) {
                            return o1.getUpdateTime().compareTo(o2.getUpdateTime());
                        } else {
                            return o2.getCount().compareTo(o1.getCount());
                        }
                    })
                    .collect(Collectors.toList());

//            //查出班里每个人当月被赞的次数
//            Map<Long, Integer> likedCountMap = new HashMap<>();
//            //查出当天内当前用户赞别人的记录
//            Set<Long> likedUsersByCurrentUser = new HashSet<>();
//
//            //这里先读取点赞数量缓存，当用户点赞动作时会清掉这个缓存
//            String likedCountCacheKey = "ATTENDANCE_RANK_LIKED_COUNT_" + currentUserId();
//            String likedUserCacheKey = "ATTENDANCE_RANK_LIKE_USER_IDS_" + currentUserId();
//            CacheObject<Map<Long, Integer>> likedCountCache = washingtonCacheSystem.CBS.flushable.get(likedCountCacheKey);
//            CacheObject<Set<Long>> likedUserIdsCache = washingtonCacheSystem.CBS.flushable.get(likedUserCacheKey);
//            if (null == likedCountCache || null == likedCountCache.getValue() || null == likedUserIdsCache || null == likedUserIdsCache.getValue()) {
//                fillLikeInfo(clazz.getId(), UserLikeType.ATTENDANCE_RANK, likedCountMap, likedUsersByCurrentUser);
//
//                washingtonCacheSystem.CBS.flushable.set(likedCountCacheKey, 30 * 60, likedCountMap);
//                washingtonCacheSystem.CBS.flushable.set(likedUserCacheKey, 30 * 60, likedUsersByCurrentUser);
//            } else {
//                likedCountMap = likedCountCache.getValue();
//                likedUsersByCurrentUser = likedUserIdsCache.getValue();
//            }

            List<StudentAttendanceRankMapper> mappers = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM月dd日　HH:mm");
            Date todayStart = DayRange.current().getStartDate();
            for (int i = 0; i < userAttendanceCounts.size(); i++) {
                UserAttendanceCount userAttendanceCount = userAttendanceCounts.get(i);
                if (!userMap.containsKey(userAttendanceCount.getUserId())) continue;

                StudentAttendanceRankMapper mapper = new StudentAttendanceRankMapper();
                mapper.setUserId(userAttendanceCount.getUserId());
                mapper.setUserName(userMap.get(userAttendanceCount.getUserId()).fetchRealname());
                mapper.setUserImg(userMap.get(userAttendanceCount.getUserId()).fetchImageUrl());
                mapper.setCount(userAttendanceCount.getCount());
                mapper.setLastSignDate(formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(userAttendanceCount.getUpdateTime().getTime()), ZoneId.systemDefault())));
                mapper.setRank(i + 1);

                // fill like info
                String recordId = RecordLikeInfo.generateAttendanceId(new Date(), clazz.getId(), userAttendanceCount.getUserId());
                RecordLikeInfo likeInfo = userLikeServiceClient.loadRecordLikeInfo(UserLikeType.ATTENDANCE_RANK, recordId);
                if (likeInfo != null) {
                    mapper.setLikeCount(likeInfo.getLikedCount());
                    Date likeTime = likeInfo.getLikeTime().get(currentUserId());
                    mapper.setLiked(likeTime != null && likeTime.after(todayStart));
                } else {
                    mapper.setLiked(false);
                }

//                if (likedCountMap.containsKey(userAttendanceCount.getUserId())) {
//                    mapper.setLikeCount(likedCountMap.get(userAttendanceCount.getUserId()));
//                }
//                mapper.setLiked(likedUsersByCurrentUser.contains(userAttendanceCount.getUserId()));

                mappers.add(mapper);
            }

            StudentAttendanceRankMapper myRank = null;
            for (int i = 0; i < mappers.size(); i++) {
                mappers.get(i).setRank(i + 1);

                if (Objects.equals(mappers.get(i).getUserId(), currentUserId())) {
                    myRank = mappers.get(i);
                }
            }

            return MapMessage.successMessage().add("ranks", mappers).add("myrank", myRank);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 查询区签到排行
     */
    @RequestMapping(value = "/region/rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage regionRank() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(currentUserId());
            if (null == clazz) return MapMessage.errorMessage("您还未加入班级");
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(clazz.getSchoolId())
                    .getUninterruptibly();
            if (null == school) return MapMessage.errorMessage("未查询到学校信息");

            SchoolAttendanceRank clazzAttendanceRank = actionLoaderClient.getRemoteReference().getClazzAttendanceRank(school.getId());
            if (null == clazzAttendanceRank || CollectionUtils.isEmpty(clazzAttendanceRank.getRanks())) {
                return MapMessage.successMessage();
            }

            Set<Long> clazzIds = clazzAttendanceRank.getRanks().stream().map(ClazzAttendanceInfo::getClazzId).collect(Collectors.toSet());
            Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzs(clazzIds)
                    .stream()
                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));
            clazzMap.put(clazz.getId(), clazz);

            MapMessage message = MapMessage.successMessage();
            List<StudentClazzAttendanceRankMapper> mappers = new ArrayList<>();
            for (int i = 0; i < clazzAttendanceRank.getRanks().size(); i++) {
                ClazzAttendanceInfo info = clazzAttendanceRank.getRanks().get(i);
                if (!clazzMap.containsKey(info.getClazzId())) {
                    continue;
                }

                StudentClazzAttendanceRankMapper mapper = new StudentClazzAttendanceRankMapper();
                mapper.setClazzName(clazzMap.get(info.getClazzId()).formalizeClazzName());
                mapper.setSchoolName(school.getCname());
                mapper.setRate(info.getRate());
                mapper.setRank(i + 1);

                mappers.add(mapper);
            }
            message.add("ranks", mappers);

            ClazzAttendanceInfo clazzAttendanceInfo = actionLoaderClient.getRemoteReference().getClazzAttendanceInfo(school.getId(), clazz.getId());
            if (null != clazzAttendanceInfo) {
                StudentClazzAttendanceRankMapper myRank = new StudentClazzAttendanceRankMapper();
                myRank.setClazzName(clazz.formalizeClazzName());
                myRank.setSchoolName(school.getCname());
                myRank.setRate(clazzAttendanceInfo.getRate());

                message.add("myrank", myRank);
            }
            return message;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

}
