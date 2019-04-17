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

import com.voxlearning.alps.api.monitor.ControllerMetric;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.like.RecordLikeInfo;
import com.voxlearning.utopia.service.action.api.support.AchievementType;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.mobile.student.helper.MobileStudentRankHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * @author xinxin
 * @since 4/8/2016
 * <p>
 * since v2.7.0
 */
@ControllerMetric
@Controller
@RequestMapping(value = "/studentMobile/rank")
public class MobileStudentRankController extends AbstractMobileController {

    @Inject private ActionServiceClient actionServiceClient;
    @Inject private MobileStudentRankHelper mobileStudentRankHelper;

    private final static Date latest = DateUtils.stringToDate("2017-02-13 00:00:00");

    @RequestMapping(value = "{page}.vpage", method = RequestMethod.GET)
    public String page(@PathVariable("page") String page, Model model) {
        if (studentUnLogin()) {
            return "studentmobilev3/logininvalid";
        }

        model.addAttribute("showRank", showRank());

        return "studentmobilev3/rank/" + page;
    }

    /**
     * 是否展示排行榜
     */
    private boolean showRank() {
        // 排行榜显示选项，默认是显示
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(currentUserId());
        boolean showRankFlag = clazz == null || clazz.needShowRank();
        //注册时间晚于 2017-02-13 的同学不展示排行榜
        if (showRankFlag) {
            Date registerTime = currentUser().getCreateTime();
            return null != registerTime && latest.after(registerTime);
        } else {
            return false;
        }

    }

    /**
     * 查询学生排行榜概要
     * FIXME 理论上是已经下线了
     */
    @RequestMapping(value = "/summary.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage summary() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(currentUserId());
            if (null == clazz) {
                return MapMessage.errorMessage("您还没有加入班级");
            }

            MapMessage message = MapMessage.successMessage();
            message.add("class_name", clazz.formalizeClazzName());

            //查出在土豪榜中的排名
            Integer silverRanking = mobileStudentRankHelper.getSilverRanking(currentUserId(), clazz);
            if (null != silverRanking) {
                message.add("silver_ranking", silverRanking);
            }

            //查在学霸榜中的排名
            Integer smRanking = mobileStudentRankHelper.getSmRanking(currentUserId(), clazz);
            if (null != smRanking) {
                message.add("sm_ranking", smRanking);
            }

            //查在成长榜中的排名
            Integer growthRanking = mobileStudentRankHelper.getGrowthRanking(currentUserId(), clazz);
            if (null != growthRanking && 0 != growthRanking) {
                message.add("growth_ranking", growthRanking);
            }

            //查家长奖励情况
            Map<String, Integer> rewardCountInParentApp = newHomeworkPartLoaderClient.getRewardCountInParentApp(currentUserId());
            message.add("reward", MapUtils.isNotEmpty(rewardCountInParentApp));

            return message;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 排行榜点赞
     */
    @RequestMapping(value = "/like.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage like() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        String type = getRequestString("type");
        Long likedUserId = getRequestLong("likedUserId");

        try {
            if (Objects.equals(likedUserId, currentUserId())) {
                return MapMessage.errorMessage("您不能给自己点赞");
            }

            UserLikeType t = UserLikeType.of(type);
            if (null == t) {
                return MapMessage.errorMessage("未知的点赞行为");
            }

            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(currentUserId());
            if (null == clazz) {
                return MapMessage.errorMessage("您还未加入班级");
            }

            Map<String, Object> extInfo = new HashMap<>();
            String recordId = "";
            switch (t) {
                case ACHIEVEMENT_RANK:   // 成就达人
                    AchievementType achievementType = AchievementType.of(getRequestParameter("achievementType", ""));
                    if (null == achievementType) {
                        return MapMessage.errorMessage("未知的成就类型");
                    }

                    Integer achievementLevel = SafeConverter.toInt(getRequestInt("level"), 0);
                    if (0 == achievementLevel) {
                        return MapMessage.errorMessage("未知的成就等级");
                    }

                    extInfo.put("achievementType", achievementType.name());
                    extInfo.put("achievementLevel", achievementLevel);

                    // 成就达人
                    t = UserLikeType.ACHIEVEMENT_RANK;
                    recordId = RecordLikeInfo.generateAchievementId(likedUserId, achievementType.name(), achievementLevel);
                    break;
                case ATTENDANCE_RANK:         // 签到排行榜
                    t = UserLikeType.ATTENDANCE_RANK;
                    // 签到是以月单位的，ID为 YYYYMM_CLASSID_UID
                    recordId = RecordLikeInfo.generateAttendanceId(MonthRange.current().getStartDate(), clazz.getId(), likedUserId);
                    break;

                default:
                    return MapMessage.successMessage();
            }

            actionServiceClient.likeRank(clazz.getId(), currentUserId(), likedUserId, t, recordId, extInfo);
            return MapMessage.successMessage();

        } catch (Exception ex) {
            logger.error("like operation:{} fail,{}", type, ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

}
