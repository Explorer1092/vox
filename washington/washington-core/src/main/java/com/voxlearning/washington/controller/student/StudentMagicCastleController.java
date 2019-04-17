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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.StudentMagicLevelName;
import com.voxlearning.utopia.entity.activity.StudentMagicCastleRecord;
import com.voxlearning.utopia.entity.activity.StudentMagicLevel;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2015/12/2.
 * Student magic castle--
 */
@Controller
@RequestMapping("/student/magic/")
public class StudentMagicCastleController extends AbstractController {

    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;

    /**
     * 魔法城堡首页
     */
    @RequestMapping(value = "castle.vpage", method = RequestMethod.GET)
    public String page(Model model) {
        StudentDetail detail = currentStudentDetail();
        // 获取魔法师等级
        StudentMagicLevel level = studentMagicCastleServiceClient.loadStudentMagicLevel(detail.getId());
        // 初始等级为1
        if (level == null) {
            level = StudentMagicLevel.newLevel();
        }
        model.addAttribute("level", level);
        StudentMagicLevelName levelName = StudentMagicLevelName.of(level.getLevel());
        model.addAttribute("levelName", levelName == null ? "" : levelName.getLevelName());
        // 获取本周魔法药水 这里有个逻辑 绑定了家长APP的 要多加一次
        Integer waterCount = studentMagicCastleServiceClient.loadStudentMagicWater(detail.getId());
        model.addAttribute("waterCount", waterCount);
        // 是否绑定了家长APP
        boolean isBindApp = studentMagicCastleServiceClient.hasBindParentApp(detail.getId());
        model.addAttribute("isBindApp", isBindApp);
        // 获取我的唤醒进度
        Map<String, Object> myActiveInfo = studentMagicCastleServiceClient.loadStudentActiveDetailInfo(detail.getId());
        model.addAttribute("myActiveInfo", myActiveInfo);
        // 获取今日是否可以唤醒
        model.addAttribute("showActive", true);
        return "studentv3/activity/magic/castle";
    }

    /**
     * 发出唤醒 每次只能唤醒一个人
     */
    @RequestMapping(value = "active.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage active() {
        Long magicianId = currentUserId();
        Long activeId = getRequestLong("activeId");
        Integer activeLevel = getRequestInt("activeLevel");
        String source = getRequestString("source");
        if (StringUtils.isBlank(source)) {
            source = "pc";
        }
        if (activeId == 0L) {
            return MapMessage.errorMessage("请选择同学");
        }
        if (activeLevel == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return atomicLockManager.wrapAtomic(studentMagicCastleServiceClient)
                    .keyPrefix("MAGIC_CASTLE_ACTIVE")
                    .keys(activeId)
                    .proxy()
                    .activeMagician(magicianId, activeId, activeLevel, StudentMagicCastleRecord.Source.valueOf(source));
        } catch (DuplicatedOperationException e) {
            logger.warn("student magic castle active magician duplicated, activeId id is {}", activeId);
            return MapMessage.errorMessage("你点击太快了，请重试");
        }
    }

    /**
     * 获取可唤醒列表
     */
    @RequestMapping(value = "loadactivelist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadActiveList() {
        StudentDetail detail = currentStudentDetail();
        if (detail == null) {
            return MapMessage.errorMessage();
        }
        boolean canActive = true;
        // 获取是否可以唤醒
        Long dayCount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .unflushable_getUserBehaviorCount(
                        UserBehaviorType.STUDENT_MAGIC_SEND_ACTIVE_COUNT_DAY,
                        detail.getId(),
                        0)
                .getUninterruptibly();
        if (dayCount >= 10) {
            canActive = false;
        }
        // 获取沉睡的魔法师列表
        List<ActivateInfoMapper> activeList = studentMagicCastleServiceClient.loadClazzSleepMagicianList(detail.getId(), detail.getClazzId());
        Map<Integer, List<ActivateInfoMapper>> dataMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(activeList)) {
            dataMap = activeList.stream().collect(Collectors.groupingBy(ActivateInfoMapper::getActiveLevel));
        }
        return MapMessage.successMessage().add("dataMap", dataMap).add("canActive", canActive);
    }

    /**
     * 获取达成该称号的魔法师
     */
    @RequestMapping(value = "loadsupermagician.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadSuperMagician() {
        StudentDetail detail = currentStudentDetail();
        if (detail == null) {
            return MapMessage.errorMessage("参数错误");
        }
        Integer level = getRequestInt("level");
        if (level == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        // 获取超过该等级的魔法师列表
        List<ActivateInfoMapper> superList = studentMagicCastleServiceClient.loadSuperMagician(detail, level);
        return MapMessage.successMessage().add("superList", superList);
    }

    /**
     * 获取本班级最高魔法师等级
     */
    @RequestMapping(value = "loadmaxmagician.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadMaxMagician() {
        StudentDetail detail = currentStudentDetail();
        if (detail == null) {
            return MapMessage.errorMessage("参数错误");
        }
        // 获取最高等级魔法师
        Map<String, Object> maxMagician = studentMagicCastleServiceClient.loadMaxMagician(detail);
        return MapMessage.successMessage().add("maxMagician", maxMagician);
    }

    /**
     * 获取本班级魔法师排行
     */
    @RequestMapping(value = "loadrank.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadRank() {
        StudentDetail detail = currentStudentDetail();
        if (detail == null) {
            return MapMessage.errorMessage("参数错误");
        }
        String type = getRequestParameter("type", "");
        if (StringUtils.isBlank(type)) {
            return MapMessage.errorMessage("参数错误");
        }
        if (StringUtils.equals("week", type)) {
            // 获取周榜
            List<Map<String, Object>> currentWeekRank = studentMagicCastleServiceClient.loadCurrentWeekMagicValueRank(detail);
            return MapMessage.successMessage().add("rankList", currentWeekRank);
        } else {
            // 获取总榜
            List<Map<String, Object>> totalRank = studentMagicCastleServiceClient.loadTotalMagicValueRank(detail);
            return MapMessage.successMessage().add("rankList", totalRank);
        }
    }

}
