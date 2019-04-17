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

package com.voxlearning.washington.controller.open.wechat.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.client.CampaignLoaderClient;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by XiaoPeng.Yang on 15-1-13.
 * 老师微信抽奖
 */
@Controller
@RequestMapping(value = "/open/wechat/teacher/lottery")
@Slf4j
public class WechatTeacherLotteryController extends AbstractOpenController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private CampaignServiceClient campaignServiceClient;
    @Inject private CampaignLoaderClient campaignLoaderClient;

    @RequestMapping(value = "getlotteryparam.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getLotteryTimes(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long userId = SafeConverter.toLong(openAuthContext.getParams().get("uid"), Long.MIN_VALUE);
        if (userId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        try {
            //前置条件 认证老师
            //判断资格
            Teacher teacher = teacherLoaderClient.loadTeacher(userId);
            openAuthContext.add("authenticationFlag", true);
            openAuthContext.add("homeworkFlag", true);

            if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
                openAuthContext.add("authenticationFlag", false);
            }

            //用户今天是否有免费机会
            int count = campaignServiceClient.getCampaignService().getTeacherLotteryFreeChance(CampaignType.TEACHER_LOTTERY, userId);
            openAuthContext.add("freeChance", count);//当前免费次数

            //大奖信息
            List<Map<String, Object>> campaignLotteryResultsBig = campaignLoaderClient.loadRecentCampaignLotteryResultBig(CampaignType.TEACHER_LOTTERY.getId());
            openAuthContext.add("lotteryResultsBig", campaignLotteryResultsBig);
            //滚动信息
            List<Map<String, Object>> campaignLotteryResults = campaignLoaderClient.loadRecentCampaignLotteryResult(CampaignType.TEACHER_LOTTERY.getId());
            openAuthContext.add("lotteryResults", campaignLotteryResults);
            openAuthContext.setCode("200");
        } catch (Exception ex) {
            log.error("get teacher lottery param failed.", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("查询抽奖次数失败");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "dolottery.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext doLottery(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long userId = SafeConverter.toLong(openAuthContext.getParams().get("uid"), Long.MIN_VALUE);
        if (userId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        try {
            User user = raikouSystem.loadUser(userId);
            if (user == null || UserType.TEACHER != user.fetchUserType()) {
                openAuthContext.setCode("400");
                openAuthContext.setError("no user found " + userId);
                return openAuthContext;
            }
            if (CampaignType.TEACHER_LOTTERY.getExpiredTime().before(new Date())) {
                openAuthContext.setCode("400");
                openAuthContext.setError("活动已过期");
                return openAuthContext;
            }
            MapMessage mapMessage = atomicLockManager.wrapAtomic(campaignServiceClient.getCampaignService())
                    .expirationInSeconds(30)
                    .keyPrefix("CAMPAIGN_DRAW_LOTTERY")
                    .keys(user.getId())
                    .proxy()
                    .drawLottery(CampaignType.TEACHER_LOTTERY, user, LotteryClientType.WECHAT);
            if (!mapMessage.isSuccess()) {
                openAuthContext.setCode("400");
                openAuthContext.setError(mapMessage.getInfo());
                return openAuthContext;
            }
            boolean win = (boolean) mapMessage.get("win");
            if (win) {
                openAuthContext.add("lottery", mapMessage.get("lottery"));
                openAuthContext.add("message", "恭喜您，中奖了");
            } else {
                openAuthContext.add("message", "很遗憾，未中奖");
            }
            openAuthContext.setCode("200");
            openAuthContext.add("win", mapMessage.get("win"));
            return openAuthContext;
        } catch (DuplicatedOperationException ignore) {
            openAuthContext.setCode("400");
            openAuthContext.setError("您点击太快了，请重试");
            return openAuthContext;
        } catch (Exception ex) {
            log.error("do lottery failed.", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("抽奖失败");
        }
        return openAuthContext;
    }
}
