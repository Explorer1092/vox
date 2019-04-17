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

package com.voxlearning.washington.controller.afenti;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiActivityType;
import com.voxlearning.utopia.service.afenti.api.constant.PurchaseType;
import com.voxlearning.utopia.service.afenti.client.AsyncAfentiCacheServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.client.CreditServiceClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardService;
import com.voxlearning.utopia.service.parentreward.api.mapper.ParentRewardReceiveResult;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;

/**
 * @author Ruib
 * @since 2016/8/15
 */
@Controller
@RequestMapping("/afenti/api/activity")
public class StudentAfentiActivityController extends StudentAfentiBaseController {

    @Inject
    private AsyncAfentiCacheServiceClient asyncAfentiCacheServiceClient;
    @ImportService(interfaceClass = ParentRewardService.class)
    private ParentRewardService parentRewardService;
    @Inject
    private CreditServiceClient creditServiceClient;

    // 获取活动 activities格式"[{'img':'','activityType':'','link':''}]"
    @RequestMapping(value = "fetchactivity.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchActivity() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;
        List<Map<String, Object>> activities = new ArrayList<>();
//        if (new Date().before(DateUtils.stringToDate("2017-05-31 23:59:59"))) {
//            Map<String, Object> m = new HashMap<>();
//            m.put("img", "https://oss-image.17zuoye.com/afenti/activity/test/2017/05/17/20170517191459832896.jpg");
//            m.put("activityType", AfentiActivityType.integralAddition.name());
//            m.put("link", "app://Pay");
//            activities.add(m);
//        }
        return MapMessage.successMessage().add("activities", activities);
    }

    // 获取活动数据
    @RequestMapping(value = "fetchactivitydata.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchActivityData() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        AfentiActivityType type = AfentiActivityType.safeParse(getRequestString("activityType"));

        MapMessage message = MapMessage.successMessage();
        message.putAll(afentiActivityServiceClient.fetchActivityData(student, type, subject));
        return message;
    }

    // 会员奖励领取列表
    @RequestMapping(value = "loginreward/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loginrewardList() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        if (subject == null || student == null) {
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
        List<Map<String, Object>> receiveReward = new ArrayList<>();
        Set<Integer> receiveDate = asyncAfentiCacheServiceClient.getAsyncAfentiCacheService()
                .AfentiUserLoginRewardCacheManager_loadRecords(student, subject)
                .take();
        for (int i = 1; i <= 7; i++) {
            Map<String, Object> mid = new HashMap<>();
            mid.put("isReceived", receiveDate.contains(i) ? true : false);
            mid.put("dayNum", i);
            mid.put("beans", 5);
            receiveReward.add(mid);
        }

        long nowDayNum = DateUtils.dayDiff(new Date(), WeekRange.current().getStartDate()) + 1;

        return MapMessage.successMessage().add("receiveReward", receiveReward).add("nowDayNum", nowDayNum);

    }

    // 会员奖励领取
    @RequestMapping(value = "loginreward/receive.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage receiveReward() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        return afentiActivityServiceClient.receiveLoginReward(student, subject);
    }

    //期末测评活动页面
    @RequestMapping(value = "termquizindex.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String termQuizIndex() {
        return "/studentmobilev3/activity/afenti/termquizindex";
    }

    //期末测评活动页面
    @RequestMapping(value = "mentionpoints.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String mentionPoints() {
        return "/studentmobilev3/activity/afenti/mentionpoints";
    }

    //期末测评报告页
    @RequestMapping(value = "termquizreport.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String termQuizReport() {
        return "/studentmobilev3/activity/afenti/termquizreport";
    }

    // 后门儿

    @RequestMapping(value = "/addUserPurchaseInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage testAddUserPurchaseInfo() {
        Long userId = getRequestLong("userId");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        afentiServiceClient.addUserPurchaseInfo(studentDetail, PurchaseType.NEW_PAID, new Date());
        List<Map<String, Object>> purchaseInfos = afentiLoaderClient.loadPurchaseInfos(studentDetail);
        return MapMessage.successMessage().add("purchaseInfos", purchaseInfos);
    }

    @RequestMapping(value = "/addUserRewardInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage testAddUserRewardInfo() {
        Long userId = getRequestLong("userId");
        Integer integral = getRequestInt("integral");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        return MapMessage.successMessage().add("retFlag", afentiServiceClient.addUserRewardInfo(studentDetail, integral));
    }

    // 家长奖励实验领取奖励
    @RequestMapping(value = "getparentreward.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getParentReward() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;
        // 获取参数
        String rewardTypeCodes = getRequestString("rewardTypeCodes");
        if (StringUtils.isBlank(rewardTypeCodes)) {
            return MapMessage.errorMessage("参数错误");
        }
        String[] keyArray = StringUtils.split(rewardTypeCodes, ",");
        Set<String> keys = new HashSet<>();
        Collections.addAll(keys, keyArray);

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        try {
            ParentRewardReceiveResult result = atomicLockManager.wrapAtomic(parentRewardService)
                    .expirationInSeconds(30)
                    .keyPrefix("GET_AFENTI_PARENT_REWARD")
                    .keys(student.getId())
                    .proxy()
                    .receiveParentReward(student.getId(), keys);
            if (result.getIntegral() > 0) {
                IntegralHistory integralHistory = new IntegralHistory();
                integralHistory.setIntegral(result.getIntegral());
                integralHistory.setComment("家长奖励获得学豆");
                integralHistory.setIntegralType(IntegralType.STUDENT_HOMEWORK_FROM_PARENT_REWARD.getType());
                integralHistory.setUserId(student.getId());
                try {
                    MapMessage mapMessage = AtomicLockManager.instance()
                            .wrapAtomic(userIntegralService)
                            .keyPrefix("receiveParentReward")
                            .keys(student.getId())
                            .proxy()
                            .changeIntegral(integralHistory);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage("领取奖励失败");
                    }
                } catch (DuplicatedOperationException e) {
                    return MapMessage.errorMessage("重复领取");
                }
            }
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("你点击太快了，请重试");
        }
        return MapMessage.successMessage("领取成功");
    }

}
