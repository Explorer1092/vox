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

package com.voxlearning.washington.controller.open.wechat.ambassador;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by xiaopeng.yang on 2015/6/3.
 */
@Controller
@RequestMapping(value = "/open/wechat/ambassador/lottery")
@Slf4j
public class WechatAmbassadorLotteryController extends AbstractOpenController {

    //抽奖二期  假期作业
    @RequestMapping(value = "getlotteryparam.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getLotteryParam(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
//        long userId = SafeConverter.toLong(openAuthContext.getParams().get("uid"));
//        if (userId == 0) {
//            openAuthContext.setCode("400");
//            openAuthContext.setError("invalid data");
//            return openAuthContext;
//        }
//        try {
//            //判断资格
//            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(userId);
//            openAuthContext.add("authenticationFlag", true);
//            openAuthContext.add("homeworkFlag", false);
//            openAuthContext.add("isAmbassador", true);
//            if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
//                openAuthContext.add("authenticationFlag", false);
//            }
//            if (!teacher.isSchoolAmbassador()) {
//                openAuthContext.add("isAmbassador", false);
//            }
//            //大使所在学校所有认证老师布置假期作业在80%以上
//            List<Map<String, Object>> schoolList = homeworkCacheClient.getSchoolAuthTeacherVhCacheManager()
//                    .loadSchoolAuthTeacherVhInfo(teacher.getTeacherSchoolId(), sid -> businessHomeworkServiceClient.loadSchoolAuthTeacherVhInfo(sid));
//            List<Map<String, Object>> arrangeList = schoolList.stream().filter(m -> ConversionUtils.toBool(m.get("isArrange"))).collect(Collectors.toList());
//            int dividend = schoolList.size() == 0 ? 1 : schoolList.size();
//            int successCount = new BigDecimal(dividend).multiply(new BigDecimal(0.8)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
//            openAuthContext.add("homeworkFlag", arrangeList.size() >= successCount);
//            Integer freeChance = miscServiceClient.getTeacherLotteryFreeChance(CampaignType.AMBASSADOR_WECHAT_LOTTERY, userId);
//            openAuthContext.add("freeChance", freeChance);
//            //大奖信息
//            List<Map<String, Object>> campaignLotteryResultsBig = miscLoaderClient.loadRecentCampaignLotteryResultBig(CampaignType.AMBASSADOR_WECHAT_LOTTERY.getId());
//            openAuthContext.add("lotteryResultsBig", campaignLotteryResultsBig);
//        } catch (Exception ex) {
//            log.error("get teacher lottery param failed.", ex);
//            openAuthContext.setCode("400");
//            openAuthContext.setError("查询抽奖次数失败");
//        }
        openAuthContext.setCode("400");
        openAuthContext.setError("活动已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "dolottery.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext doLottery(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("活动已下线");
        return openAuthContext;
//        long userId = SafeConverter.toLong(openAuthContext.getParams().get("uid"));
//        if (userId == 0) {
//            openAuthContext.setCode("400");
//            openAuthContext.setError("invalid data");
//            return openAuthContext;
//        }
//        //0点到8点之间是不允许抽奖的， 因为这个时间段微信不会发红包。
//        List<String> night = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7");
//        if (night.contains(DateUtils.dateToString(new Date(), "H"))) {
//            openAuthContext.setCode("400");
//            openAuthContext.setError("每天0点-8点不开放抽奖");
//            return openAuthContext;
//        }
//        try {
//            User user = userLoaderClient.loadUser(userId);
//            if (user == null || UserType.TEACHER != user.fetchUserType()) {
//                openAuthContext.setCode("400");
//                openAuthContext.setError("no user found " + userId);
//                return openAuthContext;
//            }
//            if (CampaignType.AMBASSADOR_WECHAT_LOTTERY.getExpiredTime().before(new Date())) {
//                openAuthContext.setCode("400");
//                openAuthContext.setError("活动已过期");
//                return openAuthContext;
//            }
//            MapMessage mapMessage = atomicLockManager.wrapAtomic(miscServiceClient)
//                    .expirationInSeconds(30)
//                    .keyPrefix("CAMPAIGN_WECHAT_DRAW_LOTTERY")
//                    .keys(user.getId())
//                    .proxy()
//                    .drawLottery(CampaignType.AMBASSADOR_WECHAT_LOTTERY, user, LotteryClientType.WECHAT);
//            if (!mapMessage.isSuccess()) {
//                openAuthContext.setCode("400");
//                openAuthContext.setError(mapMessage.getInfo());
//                return openAuthContext;
//            }
//            boolean win = (boolean) mapMessage.get("win");
//            if (win) {
//                openAuthContext.add("lottery", mapMessage.get("lottery"));
//                openAuthContext.add("redPackFlag", mapMessage.get("redPackFlag"));
//                openAuthContext.add("message", "恭喜您，中奖了");
//            } else {
//                openAuthContext.add("message", "很遗憾，未中奖");
//            }
//            openAuthContext.setCode("200");
//            openAuthContext.add("win", mapMessage.get("win"));
//            return openAuthContext;
//        } catch (DuplicatedOperationException ignore) {
//            openAuthContext.setCode("400");
//            openAuthContext.setError("您点击太快了，请重试");
//            return openAuthContext;
//        } catch (Exception ex) {
//            log.error("do lottery failed.", ex);
//            openAuthContext.setCode("400");
//            openAuthContext.setError("抽奖失败");
//        }
//        return openAuthContext;
    }
}
