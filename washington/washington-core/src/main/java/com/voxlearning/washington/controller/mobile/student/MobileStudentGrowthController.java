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
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.action.api.document.*;
import com.voxlearning.utopia.service.action.api.support.UserGrowthReward;
import com.voxlearning.utopia.service.action.client.ActionLoaderClient;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.mapper.StudentGrowthLevelRewardMapper;
import com.voxlearning.washington.mapper.StudentGrowthMapper;
import com.voxlearning.washington.mapper.StudentGrowthRecordMapper;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 4/8/2016
 */
@Controller
@RequestMapping(value = "/studentMobile/growth")
public class MobileStudentGrowthController extends AbstractMobileController {

    @Inject private ActionLoaderClient actionLoaderClient;
    @Inject private ActionServiceClient actionServiceClient;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;

    @RequestMapping(value = "{page}.vpage", method = RequestMethod.GET)
    public String page(@PathVariable("page") String page) {
        if (studentUnLogin()) {
            return "studentmobilev3/logininvalid";
        }
        return "studentmobilev3/growth/" + page;
    }

    /**
     * 查询用户30天内的成长值获取记录
     */
    @RequestMapping(value = "/record.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage record() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            List<UserGrowthLog> userGrowthLogs = actionLoaderClient.getRemoteReference().getUserGrowthLogs(currentUserId());

            List<StudentGrowthRecordMapper> records = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM月dd日");
            for (UserGrowthLog log : userGrowthLogs) {
                StudentGrowthRecordMapper mapper = new StudentGrowthRecordMapper();
                mapper.setType(log.getType().getTitle());
                mapper.setDate(formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(log.getActionTime().getTime()), ZoneId.systemDefault())));
                mapper.setDelta(log.getDelta());

                records.add(mapper);
            }

            return MapMessage.successMessage().add("records", records);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 查询等级详情
     */
    @RequestMapping(value = "/levelinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage levelInfo() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            MapMessage message = MapMessage.successMessage();

            UserGrowth userGrowth = actionLoaderClient.getRemoteReference().loadUserGrowth(currentUserId());
            if (null != userGrowth) {
                message.add("level", userGrowth.toLevel());
                message.add("title", userGrowth.toTitle());
                message.add("current", userGrowth.getGrowthValue());
                message.add("next", userGrowth.getGrowthValue() + UserGrowthLevel.valueNeededForLevelUp(userGrowth.getGrowthValue()));
            }
            return message;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 查询全班的成长值排行榜
     */
    @RequestMapping(value = "/clazz/rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage clazzRank() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(currentUserId());
            if (null == clazz) {
                return MapMessage.errorMessage("您还没有加入班级");
            }

            List<User> users = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazz.getId(), currentUserId());
            Set<Long> userIds = new HashSet<>();
            if (CollectionUtils.isNotEmpty(users)) {
                userIds = users.stream().map(User::getId).collect(Collectors.toSet());
            }

            Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);
            List<StudentGrowthMapper> mappers = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(userIds)) {
                Map<Long, UserGrowth> userGrowthMap = actionLoaderClient.getRemoteReference().loadUserGrowths(userIds);
                List<UserGrowth> sortedList = userGrowthMap.values().stream().sorted((u1, u2) -> u2.getGrowthValue().compareTo(u1.getGrowthValue())).collect(Collectors.toList());
                for (int i = 0; i < sortedList.size(); i++) {
                    StudentGrowthMapper mapper = new StudentGrowthMapper();
                    mapper.setUserId(sortedList.get(i).getId());
                    if (userMap.containsKey(sortedList.get(i).getId())) {
                        mapper.setUserName(userMap.get(sortedList.get(i).getId()).fetchRealname());
                    }
                    mapper.setRank(i + 1);
                    mapper.setLevel(sortedList.get(i).toLevel());
                    mapper.setTitle(sortedList.get(i).toTitle());
                    mappers.add(mapper);
                }
            }

            return MapMessage.successMessage().add("rank", mappers);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 查询用户当前升级礼包
     * 如果有未领取的,按等级由大到小显示
     * 如果没有未领的,显示下一次升级可领取的礼包
     */
    @RequestMapping(value = "/reward.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage reward() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            int level = 0;
            UserGrowth userGrowth = actionLoaderClient.getRemoteReference().loadUserGrowth(currentUserId());
            if (null != userGrowth) {
                level = userGrowth.toLevel();
            }

            //当前等级可领的奖励,包含已领、未领(都是已经获得的奖励)
            List<Integer> canRewardLevels = UserGrowthReward.getLevelsCanReceive(level);

            //用户已经领取的奖励
            List<UserGrowthRewardLog> logs = actionLoaderClient.getRemoteReference().getUserGrowthLevelRewards(currentUserId());

            List<StudentGrowthLevelRewardMapper> mappers = new ArrayList<>();
            if (CollectionUtils.isEmpty(canRewardLevels) || (CollectionUtils.isNotEmpty(logs) && logs.size() >= canRewardLevels.size())) {
                //没有可领的奖励,显示下一个可领的礼包
                level += 5;
                List<Integer> ls = UserGrowthReward.getLevelsCanReceive(level);
                for (Integer l : ls) {
                    if (!canRewardLevels.contains(l)) {
                        fillRewardMapper(mappers, l, 0); //0表示不可领取,还没达到此等级
                        break;
                    }
                }
            } else {
                if (CollectionUtils.isEmpty(logs)) {
                    for (Integer l : canRewardLevels) {
                        fillRewardMapper(mappers, l, 2); //2表示有奖励等待领取
                    }
                } else {
                    Set<Integer> receivedLevels = logs.stream().map(UserGrowthRewardLog::getGrowthLevel).collect(Collectors.toSet());
                    for (Integer l : canRewardLevels) {
                        if (!receivedLevels.contains(l)) {
                            fillRewardMapper(mappers, l, 2);
                        }
                    }
                }
            }

            //按等级由大到小排序
            mappers = mappers.stream().sorted((m1, m2) -> m2.getLevel().compareTo(m1.getLevel())).collect(Collectors.toList());

            return MapMessage.successMessage().add("rewards", mappers);
        } catch (Exception ex) {
            logger.error("Failed process student growth reward, student={}", currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 查询所有等级奖励(包含用户已领取、未领取、不可领取的)
     */
    @RequestMapping(value = "/rewards.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rewards() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            int level = 1;
            UserGrowth userGrowth = actionLoaderClient.getRemoteReference().loadUserGrowth(currentUserId());
            if (null != userGrowth) {
                level = userGrowth.toLevel();
            }
            //当前等级可以领取奖励的等级
            List<Integer> canReceivedLevels = UserGrowthReward.getLevelsCanReceive(level);

            //所有可以领取奖励的等级
            List<Integer> allLevels = UserGrowthReward.getLevelsCanReceive(UserGrowthReward.MAX_LEVEL);

            //当前用户已经领取的奖励
            Map<Integer, UserGrowthRewardLog> rewardLogMap = new HashMap<>();
            List<UserGrowthRewardLog> rewardLogs = actionLoaderClient.getRemoteReference().getUserGrowthLevelRewards(currentUserId());
            if (CollectionUtils.isNotEmpty(rewardLogs)) {
                for (UserGrowthRewardLog log : rewardLogs) {
                    rewardLogMap.put(log.getGrowthLevel(), log);
                }
            }

            List<StudentGrowthLevelRewardMapper> mappers = new ArrayList<>();
            for (Integer l : allLevels) {
                if (canReceivedLevels.contains(l)) {
                    if (rewardLogMap.keySet().contains(l)) {
                        fillRewardMapper(mappers, l, 1); //1表示奖励已经领取
                    } else {
                        fillRewardMapper(mappers, l, 2);  //2表示奖励等待领取
                    }
                } else {
                    fillRewardMapper(mappers, l, 0);    //0表示奖励还未领取
                }
            }

            //排序,按等级由小到大
            mappers = mappers.stream().sorted((m1, m2) -> m1.getLevel().compareTo(m2.getLevel())).collect(Collectors.toList());

            return MapMessage.successMessage().add("rewards", mappers);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 领取等级礼包
     */
    @RequestMapping(value = "/reward/receive.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage receiveGrowthReward(@RequestParam Integer level) {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        if (null == level || 0 == level) {
            return MapMessage.errorMessage("参数错误");
        }

        try {

            MapMessage resultMsg = MapMessage.successMessage();

            UserGrowth userGrowth = actionLoaderClient.getRemoteReference().loadUserGrowth(currentUserId());
            if (null == userGrowth) {
                return MapMessage.errorMessage("没有奖励可以领取");
            }

            List<Integer> canReceiveLevels = UserGrowthReward.getLevelsCanReceive(userGrowth.toLevel());
            if (CollectionUtils.isEmpty(canReceiveLevels)) {
                return MapMessage.errorMessage("当前等级没有奖励可领取");
            }

            if (!canReceiveLevels.contains(level)) {
                return MapMessage.errorMessage("您选择的等级没有奖励可领");
            }

            actionServiceClient.receiveGrowthLevelReward(currentUserId(), level);

            String headWearCode = UserGrowthReward.getHeadWearCode(level);
            if (!StringUtils.isEmpty(headWearCode)) {
                Privilege headwear = privilegeBufferServiceClient.getPrivilegeBuffer().loadByCode(headWearCode);
                if (headwear != null) {
                    resultMsg.add("headwear", headwear);
                }
            }

            return resultMsg;
        } catch (Exception ex) {
            logger.error("Receive growth reward failed,uid:{},level:{},msg:{}", level, ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private void fillRewardMapper(List<StudentGrowthLevelRewardMapper> mappers, Integer level, Integer state) {
        StudentGrowthLevelRewardMapper mapper = new StudentGrowthLevelRewardMapper();
        mapper.setIntegralCount(UserGrowthReward.getIntegral(level));

        Privilege headWear = privilegeBufferServiceClient.getPrivilegeBuffer().loadByCode(UserGrowthReward.getHeadWearCode(level));
        if (headWear != null) {
            mapper.setHeadWearId(headWear.getId());
        }
        mapper.setState(state);
        mapper.setLevel(level);
        mappers.add(mapper);
    }
}
