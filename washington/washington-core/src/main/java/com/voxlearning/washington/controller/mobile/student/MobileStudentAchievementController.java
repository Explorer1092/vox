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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.entity.like.RecordLikeInfo;
import com.voxlearning.utopia.service.action.api.document.*;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.api.support.AchievementType;
import com.voxlearning.utopia.service.action.api.support.PrivilegeType;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import com.voxlearning.utopia.service.action.client.ActionLoaderClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeLoaderClient;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.client.UserLikeServiceClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.mapper.StudentAchievementMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping(value = "/studentMobile/achievement")
public class MobileStudentAchievementController extends AbstractMobileController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private ActionLoaderClient actionLoaderClient;
    @Inject private PrivilegeLoaderClient privilegeLoaderClient;
    @Inject private UserLikeServiceClient userLikeServiceClient;

    @RequestMapping(value = "{page}.vpage", method = RequestMethod.GET)
    public String page(@PathVariable("page") String page) {
        if (studentUnLogin()) {
            return "studentmobilev3/logininvalid";
        }
        return "studentmobilev3/achievement/" + page;
    }

    /**
     * 查询当前用户的成就情况(包括已获得的、未获得的)
     */
    @RequestMapping(value = "/received.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage received() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            MapMessage result = MapMessage.successMessage();

            List<UserAchievementRecord> uarList = actionLoaderClient.getRemoteReference().loadUserAchievementRecords(currentUserId());
            if (CollectionUtils.isNotEmpty(uarList)) {
                List<StudentAchievementMapper> mappers = new ArrayList<>();
                for (UserAchievementRecord uar : uarList) {
                    Achievement achievement = AchievementBuilder.build(uar);

                    if (achievement != null && achievement.getType() != null && !achievement.getType().getValid()) {
                        continue;
                    }

                    if (null != achievement && null != achievement.getType()) {
                        StudentAchievementMapper mapper = new StudentAchievementMapper();
                        mapper.setLevel(achievement.getRank());
                        mapper.setType(achievement.getType().name());
                        mapper.setTitle(achievement.getTitle());
                        mapper.setCurrent(uar.getScore());
                        mapper.setNext(AchievementBuilder.next(ActionEventType.valueOf(uar.parse().getAet()), achievement.getRank()));
                        mapper.setLast(AchievementBuilder.next(ActionEventType.valueOf(uar.parse().getAet()), achievement.getRank() - 1));
                        mappers.add(mapper);
                    }
                }

                fillLockedAchievementWithoutLevel(mappers);

                if (CollectionUtils.isNotEmpty(mappers)) {
                    result.add("achievements", mappers);
                }
            }

            // 获取勋章
            List<UserPrivilege> privilegeList = privilegeLoaderClient.getPrivilegeLoader()
                    .findUserPrivileges(currentUserId())
                    .getUninterruptibly();
            if (CollectionUtils.isNotEmpty(privilegeList)) {
                privilegeList = privilegeList.stream().filter(p -> p.getType() != null && Objects.equals(p.getType(), PrivilegeType.Medal.name()))
                        .collect(Collectors.toList());
                result.add("medals", privilegeList);
            }
            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 查询班级成就墙
     */
    @RequestMapping(value = "/wall.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage achievementWall() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(currentUserId());
            if (null == clazz) {
                return MapMessage.errorMessage("您还没有加入班级");
            }

            boolean loadAllAchievements = SafeConverter.toBoolean(getRequestParameter("all", "false"), false);

            List<StudentAchievementMapper> mappers = new ArrayList<>();

            List<ClazzAchievementLog> logs = actionLoaderClient.getRemoteReference().getClazzAchievementWall(clazz.getId());
            if (CollectionUtils.isNotEmpty(logs)) {
                Map<Long, User> userMap = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazz.getId(), currentUserId())
                        .stream().collect(Collectors.toMap(User::getId, Function.identity()));

                for (ClazzAchievementLog log : logs) {
                    User user = userMap.get(log.getUserId());
                    if (null == user) {//用户可能转班了
                        user = raikouSystem.loadUser(log.getUserId());
                    }

                    AchievementType type = AchievementType.of(log.getAchievementType());
                    if (null == type || !type.getValid()) continue;

                    StudentAchievementMapper mapper = new StudentAchievementMapper();
                    mapper.setUserId(log.getUserId());
                    mapper.setLevel(log.getAchievementLevel());
                    mapper.setUserName(null == user ? "" : user.fetchRealname());
                    mapper.setType(log.getAchievementType());
                    mapper.setTitle(type.getTitle());
                    mapper.setNext(AchievementBuilder.next(type, log.getAchievementLevel()));
                    mapper.setLast(AchievementBuilder.next(type, log.getAchievementLevel() - 1));

                    mappers.add(mapper);
                }
            }

            if (loadAllAchievements) {
                fillLockedAchievement(mappers);
            }

            Map<String, List<StudentAchievementMapper>> achievementMap = new HashMap<>();
            for (StudentAchievementMapper mapper : mappers) {
                if (!achievementMap.containsKey(mapper.getTitle())) {
                    achievementMap.put(mapper.getTitle(), new ArrayList<>());
                }

                achievementMap.get(mapper.getTitle()).add(mapper);
            }

            return MapMessage.successMessage().add("achievements", achievementMap);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 查询班级某一成就排行榜
     */
    @RequestMapping(value = "/clazz/rank.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clazzRank() {
        String type = getRequestString("type");
        Integer level = getRequestInt("level");
        if (StringUtils.isBlank(type) || 0 == level) {
            return MapMessage.errorMessage("参数错误");
        }

        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(currentUserId());
            if (null == clazz) {
                return MapMessage.errorMessage("您还没有加入班级");
            }

            AchievementType t = AchievementType.of(type);
            if (null == t) {
                return MapMessage.errorMessage("未知的成就类型");
            }

            List<User> users = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazz.getId(), currentUserId());
            Set<Long> userIds = users.stream().map(User::getId).collect(Collectors.toSet());
            Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));

            List<UserAchievementLog> userAchievementLogs = actionLoaderClient.getRemoteReference().getUserAchievements(userIds, t, level);
            if (CollectionUtils.isEmpty(userAchievementLogs)) {
                return MapMessage.successMessage();
            }
            List<StudentAchievementMapper> mappers = new ArrayList<>();

//            //先查点赞数量缓存，当用户点赞动作时会清掉这个缓存
//            Map<Long, Integer> likedCountMap = new HashMap<>();
//            Set<Long> likedUsersByCurrentUser = new HashSet<>();
//
//            String likedCountCacheKey = "ACHIEVEMENT_RANK_LIKED_COUNT_" + currentUserId();
//            String likedUserIdsCacheKey = "ACHIEVEMENT_RANK_LIKED_USER_IDS_" + currentUserId();
//            CacheObject<Map<Long, Integer>> likedCountCache = washingtonCacheSystem.CBS.flushable.get(likedCountCacheKey);
//            CacheObject<Set<Long>> likedUserIdsCache = washingtonCacheSystem.CBS.flushable.get(likedUserIdsCacheKey);
//            if (null == likedCountCache || null == likedCountCache.getValue() || null == likedUserIdsCache || null == likedUserIdsCache.getValue()) {
//                fillLikeInfo(clazz.getId(), UserLikeType.ACHIEVEMENT_RANK, likedCountMap, likedUsersByCurrentUser);
//
//                washingtonCacheSystem.CBS.flushable.set(likedCountCacheKey, 30 * 60, likedCountMap);
//                washingtonCacheSystem.CBS.flushable.set(likedUserIdsCacheKey, 30 * 60, likedUsersByCurrentUser);
//            } else {
//                likedCountMap = likedCountCache.getValue();
//                likedUsersByCurrentUser = likedUserIdsCache.getValue();
//            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (UserAchievementLog a : userAchievementLogs) {
                StudentAchievementMapper mapper = new StudentAchievementMapper();
                mapper.setLevel(a.getLevel());
                mapper.setType(t.name());
                mapper.setTitle(t.getTitle());
                mapper.setUserId(a.getUserId());
                LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(a.getCreateTime().getTime()), ZoneId.systemDefault());
                mapper.setReceiveDate(formatter.format(ldt));

                if (userMap.containsKey(a.getUserId())) {
                    mapper.setUserName(userMap.get(a.getUserId()).fetchRealname());
                    mapper.setUserImg(userMap.get(a.getUserId()).fetchImageUrl());
                }

                String recordId = RecordLikeInfo.generateAchievementId(a.getUserId(), t.name(), a.getLevel());
                RecordLikeInfo likeInfo = userLikeServiceClient.loadRecordLikeInfo(UserLikeType.ACHIEVEMENT_RANK, recordId);
                if (likeInfo != null) {
                    mapper.setLikeCount(likeInfo.getLikedCount());
                    mapper.setLiked(likeInfo.getLikeTime().containsKey(a.getUserId()));
                } else {
                    mapper.setLiked(false);
                }

                mappers.add(mapper);
            }

            return MapMessage.successMessage().add("achievements", mappers).add("condition", AchievementBuilder.next(t, level - 1));
        } catch (Exception ex) {
            logger.error("Get clazz achievement rank for type {} failed,{}", type, ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }


    private void fillLockedAchievement(List<StudentAchievementMapper> mappers) {
        List<StudentAchievementMapper> lockedAchievements = new ArrayList<>();

        for (AchievementType type : AchievementType.values()) {
            if (!type.getValid()) {
                continue;
            }

            int levelCount = AchievementBuilder.levelCount(type);

            for (int i = 1; i <= levelCount; i++) {
                boolean exist = false;
                for (StudentAchievementMapper mapper : mappers) {
                    if (type.name().equals(mapper.getType()) && mapper.getLevel() == i) {
                        exist = true;
                        break;
                    }
                }

                if (exist) {
                    continue;
                }

                StudentAchievementMapper mapper = new StudentAchievementMapper();
                mapper.setType(type.name());
                mapper.setTitle(type.getTitle());
                mapper.setLevel(i);
                mapper.setLast(AchievementBuilder.next(type, i - 1));   //要升到当前等级需要的经验
                mapper.setNext(AchievementBuilder.next(type, i));    //获取下一级需要经验
                lockedAchievements.add(mapper);
            }
        }

        mappers.addAll(lockedAchievements);
    }

    private void fillLockedAchievementWithoutLevel(List<StudentAchievementMapper> mappers) {
        List<StudentAchievementMapper> lockedAchievements = new ArrayList<>();

        for (AchievementType type : AchievementType.values()) {
            if (!type.getValid()) {
                continue;
            }

            boolean exist = false;
            for (StudentAchievementMapper mapper : mappers) {
                if (type.name().equals(mapper.getType())) {
                    exist = true;
                    break;
                }
            }

            if (exist) {
                continue;
            }

            StudentAchievementMapper mapper = new StudentAchievementMapper();
            mapper.setType(type.name());
            mapper.setTitle(type.getTitle());
            mapper.setLevel(0);
            mapper.setNext(AchievementBuilder.next(type, 0));   //获得Lv.1需要的经验
            lockedAchievements.add(mapper);
        }

        mappers.addAll(lockedAchievements);
    }

}
