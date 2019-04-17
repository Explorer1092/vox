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

package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Created by Summer Yang on 2016/8/10.
 * 学生魔法城堡 移动端
 */
@Controller
@RequestMapping("/studentMobile/magic/")
@NoArgsConstructor
@Slf4j
public class MobileStudentMagicCastleController extends AbstractMobileController {

    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;

    /**
     * 唤醒首页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index() {
        return "redirect:/view/mobile/student/magic/index"; //功能页面已经迁移到Node project
    }

    /**
     * 唤醒首页 ajax
     */
    @RequestMapping(value = "ysera.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ysera() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        try {
            StudentDetail detail = currentStudentDetail();
            // 获取本周魔法药水 这里有个逻辑 绑定了家长APP的 要多加一次
            Integer waterCount = studentMagicCastleServiceClient.loadStudentMagicWater(detail.getId());
            // 获取我的唤醒进度pushedit
            Map<String, Object> myActiveInfo = studentMagicCastleServiceClient.loadStudentActiveDetailInfo(detail.getId());
            // 获取本周唤醒成功次数排行
            List<Map<String, Object>> weekRankList = studentMagicCastleServiceClient.loadCurrentWeekActiveRank(detail);
            return MapMessage.successMessage()
                    .add("waterCount", waterCount)
                    .add("myActiveInfo", myActiveInfo)
                    .add("weekRankList", weekRankList);
        } catch (Exception ex) {
            logger.error("Failed fetch student magical castle index page, student={}", currentUserId(), ex);
            return MapMessage.errorMessage("数据加载失败");
        }
    }

    /**
     * 去唤醒页面 ajax
     *
     * @return
     */
    @RequestMapping(value = "awake.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage awake() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        try {
            StudentDetail detail = currentStudentDetail();
            // 获取是否可以唤醒
            Long dayCount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .unflushable_getUserBehaviorCount(
                            UserBehaviorType.STUDENT_MAGIC_SEND_ACTIVE_COUNT_DAY,
                            detail.getId(),
                            0)
                    .getUninterruptibly();
            boolean canActive = dayCount < 10;
            // 获取沉睡的魔法师列表
            List<ActivateInfoMapper> activeList = studentMagicCastleServiceClient.loadClazzSleepMagicianList(detail.getId(), detail.getClazzId());
            Map<Integer, List<ActivateInfoMapper>> activeMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(activeList)) {
                activeMap = activeList.stream().collect(Collectors.groupingBy(ActivateInfoMapper::getActiveLevel));
            }
            Map<String, List<ActivateInfoMapper>> stringMap = new HashMap<>();
            for (Map.Entry<Integer, List<ActivateInfoMapper>> entry : activeMap.entrySet()) {
                stringMap.put(entry.getKey().toString(), entry.getValue());
            }
            // 获取本周魔法药水 这里有个逻辑 绑定了家长APP的 要多加一次
            Integer waterCount = studentMagicCastleServiceClient.loadStudentMagicWater(detail.getId());
            return MapMessage.successMessage()
                    .add("activeMap", stringMap)
                    .add("canActive", canActive)
                    .add("waterCount", waterCount);
        } catch (Exception ex) {
            logger.error("Failed active student magical data, student={}", currentUserId(), ex);
            return MapMessage.errorMessage("数据加载失败");
        }
    }

}
