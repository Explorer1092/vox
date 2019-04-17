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

package com.voxlearning.washington.controller.campaign;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryFragmentHistory;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotterySendHistory;
import com.voxlearning.utopia.service.campaign.client.CampaignLoaderClient;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorServiceClient;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Alex on 14-10-21.
 */
@Controller
@RequestMapping("/campaign")
@Slf4j
@NoArgsConstructor
public class CampaignController extends AbstractController {

    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
    @Inject private AsyncVendorServiceClient asyncVendorServiceClient;

    @Inject private CampaignLoaderClient campaignLoaderClient;
    @Inject private CampaignServiceClient campaignServiceClient;

    /**
     * 老师端抽奖
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "teacherlottery.vpage", method = RequestMethod.GET)
    public String teacherLottery(Model model) {
        // 下线
        return "redirect:/";
    }

    /**
     * 学生端抽奖
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "studentlottery.vpage", method = RequestMethod.GET)
    public String studentLottery(Model model) {
        User user = currentUser();
        if (user == null || user.getUserType() != UserType.STUDENT.getType()) {
            return "redirect:/";
        }
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return "redirect:/";
        }
        if (studentDetail.getClazzId() == null) {
            return "redirect:/";
        }
        //滚动信息
        List<Map<String, Object>> campaignLotteryResults = campaignLoaderClient.loadRecentCampaignLotteryResultForStudent(CampaignType.STUDENT_LOTTERY_56.getId(), studentDetail.getClazzId());
        model.addAttribute("campaignLotteryResults", campaignLotteryResults);
        model.addAttribute("campaignId", CampaignType.STUDENT_LOTTERY_56.getId());
        //用户今天是否有免费机会
        int myCount = campaignServiceClient.getCampaignService().getTeacherLotteryFreeChance(CampaignType.STUDENT_LOTTERY_56, user.getId());
        model.addAttribute("myCount", myCount);
        // 可分享给同学的次数
        long sendCount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .unflushable_getUserBehaviorCount(UserBehaviorType.STUDENT_FREE_SEND_LOTTERY_CHANCE, user.getId())
                .getUninterruptibly();
        model.addAttribute("sendCount", sendCount);
        // 谁赠送给我抽奖机会
        List<CampaignLotterySendHistory> sendHistories = campaignLoaderClient.getCampaignLoader().findCampaignLotterySendHistories(CampaignType.STUDENT_LOTTERY_56.getId(), user.getId());
        model.addAttribute("sendList", sendHistories);
        // 统计碎片数量
        Date startDate = DateUtils.calculateDateDay(new Date(), -10);
        List<CampaignLotteryFragmentHistory> histories = campaignLoaderClient.getCampaignLoader().findCampaignLotteryFragmentHistories(CampaignType.STUDENT_LOTTERY_56.getId(), user.getId());
        histories = histories.stream().filter(h -> h.getAwardId() == 5).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(histories)) {
            CampaignLotteryFragmentHistory recentHis = MiscUtils.firstElement(histories);
            if (recentHis.getCreateDatetime().after(startDate)) {
                startDate = recentHis.getCreateDatetime();
            }
        }
        // 历史做兼容 查询41 42 43的历史碎片
        List<CampaignLotteryHistory> lotteryHistories = new ArrayList<>();
        if (new Date().before(DateUtils.stringToDate("2016-09-26 23:59:59"))) {
            List<CampaignLotteryHistory> lotteryHistories43 = campaignLoaderClient.getCampaignLoader().findCampaignLotteryHistories(43, user.getId());

            List<CampaignLotteryHistory> lotteryHistories41 = campaignLoaderClient.getCampaignLoader().findCampaignLotteryHistories(41, user.getId());

            List<CampaignLotteryHistory> lotteryHistories42 = campaignLoaderClient.getCampaignLoader().findCampaignLotteryHistories(42, user.getId());
            if (CollectionUtils.isNotEmpty(lotteryHistories41)) {
                lotteryHistories.addAll(lotteryHistories41);
            }
            if (CollectionUtils.isNotEmpty(lotteryHistories42)) {
                lotteryHistories.addAll(lotteryHistories42);
            }
            if (CollectionUtils.isNotEmpty(lotteryHistories43)) {
                lotteryHistories.addAll(lotteryHistories43);
            }
        } else {
            lotteryHistories = campaignLoaderClient.getCampaignLoader().findCampaignLotteryHistories(CampaignType.STUDENT_LOTTERY_56.getId(), user.getId());
        }

        final Date finalStartDate = startDate;
        long count = lotteryHistories.stream().filter(h -> h.getCreateDatetime().after(finalStartDate))
                .filter(h -> h.getAwardId() == 5).count();
        model.addAttribute("fragmentCount", count);
        return "/studentv3/activity/studentlottery";
    }

    /**
     * 中学老师端抽奖
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "middleteacherlottery.vpage", method = RequestMethod.GET)
    public String middleTeacherLottery(Model model) {
        // 下线
        return "redirect:/";
    }

    /**
     * 中学抽奖数据获取  app nodejs
     */
    @RequestMapping(value = "loadmiddleteacherlotterydata.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadMiddleTeacherLotteryData() {
        return MapMessage.errorMessage("活动已下线");
    }

    /**
     * 目前执行抽奖的统一入口
     *
     * @param campaignId
     * @return
     */
    @RequestMapping(value = "{campaignId}/lottery.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage doUserLottery(@PathVariable("campaignId") Integer campaignId) {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("用户未登录!");
        }
        CampaignType campaignType = CampaignType.of(campaignId);
        if (campaignType == null || campaignType.getExpiredTime().before(new Date())) {
            return MapMessage.errorMessage("无效的活动ID或者活动已过期!");
        }
        String clientType = getRequestString("clientType");
        LotteryClientType type = LotteryClientType.APP;
        if (StringUtils.isNotBlank(clientType)) {
            type = LotteryClientType.valueOf(clientType);
        }
        MapMessage mapMessage;
        try {
            mapMessage = atomicLockManager.wrapAtomic(campaignServiceClient.getCampaignService())
                    .expirationInSeconds(30)
                    .keyPrefix("CAMPAIGN_DRAW_LOTTERY")
                    .keys(user.getId())
                    .proxy()
                    .drawLottery(campaignType, currentUser(), type);
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
        return mapMessage;
    }

    // 获取分享学生名单 学生抽奖免费送抽奖次数给同学
    @RequestMapping(value = "getclassmates.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getClassMates() {
        User user = currentUser();
        if (user == null || user.fetchUserType() != UserType.STUDENT) {
            return MapMessage.errorMessage("用户未登录!");
        }
        List<User> users = userAggregationLoaderClient.loadLinkedClassmatesForSystemClazz(user.getId());
        List<Map<String, Object>> data = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("studentId", u.getId());
            objectMap.put("studentName", u.fetchRealname());
            data.add(objectMap);
        }
        return MapMessage.successMessage().add("data", data);
    }

    // 学生抽奖免费送抽奖次数给同学
    @RequestMapping(value = "sendlotterychance.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage sendLotteryChance() {
        User user = currentUser();
        if (user == null || user.fetchUserType() != UserType.STUDENT) {
            return MapMessage.errorMessage("用户未登录!");
        }
        Long studentId = getRequestLong("studentId");
        Integer campaignId = getRequestInt("campaignId");
        if (studentId == 0L) {
            return MapMessage.errorMessage("请选择同学");
        }
        MapMessage mapMessage;
        try {
            CampaignType type = CampaignType.of(campaignId);
            mapMessage = atomicLockManager.wrapAtomic(campaignServiceClient.getCampaignService())
                    .expirationInSeconds(30)
                    .keyPrefix("S_S_LOTTERY_CHANCE")
                    .keys(user.getId())
                    .proxy()
                    .studentSendLotteryChance(type, user, studentId);
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
        return mapMessage;
    }


    @RequestMapping(value = "gmc.vpage", method = RequestMethod.GET)
    public String gmcLanding(Model model) {
        if (currentUser() == null) {
            return "redirect:/login.vpage?returnURL=/campaign/gmc.vpage";
        }

        User user = currentUser();
        if (user.isTeacher()) {
            return "redirect:/teacher/challenge/index.vpage";
        } else {
            return "redirect:/student/challenge/index.vpage";
        }
    }

    @RequestMapping(value = "gmclogin.vpage", method = RequestMethod.GET)
    public String gmcLogin(Model model) {
        if (currentUser() == null) {
            return "redirect:/login.vpage?returnURL=/campaign/gmclogin.vpage";
        }

        final String appKey = "GlobalMath";
        Long userId = currentUserId();

        VendorApps app = vendorLoaderClient.getExtension().loadVendorApp(appKey);
        if (app == null || app.isSuspend()) {
            return "redirect:/";
        }

        MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                .registerVendorAppUserRef(appKey, userId)
                .getUninterruptibly();
        if (!message.isSuccess() || null == message.get("ref")) return "redirect:/";
        VendorAppsUserRef vendorAppsUserRef = (VendorAppsUserRef) message.get("ref");

        String sessionKey = vendorAppsUserRef.getSessionKey();

        return "redirect:" + app.getAppUrl() + "?session_key=" + sessionKey;
    }

    // 圣诞老人限时抢购
    @RequestMapping(value = "travelsantaclaus.vpage", method = RequestMethod.GET)
    public String usaSantaClaus(Model model) {
        if (currentUser() == null) {
            return "redirect:/";
        }

        Long userId = currentUserId();
        CampaignType campaignType = CampaignType.TRAVEL_AMERICA_SANTACLAUS;

        // 获取用户已经有的抽奖信息
        List<CampaignLotteryHistory> userLotteries = campaignLoaderClient.getCampaignLoader().findCampaignLotteryHistories(campaignType.getId(), userId);
        if (userLotteries == null || userLotteries.size() == 0) {
            model.addAttribute("lotteryTimes", 1);
        } else {
            model.addAttribute("lotteryTimes", 0);
        }

        return "/studentv3/activity/travelsantaclaus";
    }

    @RequestMapping(value = "invitestudents.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getInvitedStudents() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录再参加此次活动!");
        }

        StudentDetail detail = currentStudentDetail();
        if (detail.getClazz() == null) {
            return MapMessage.errorMessage("你还没有实际的班级，不能参加此次活动!");
        }

        // FIXME: 这个地方原有逻辑如此
        // FIXME: 为什么不是取enabled的，且distinct invitee之后的值？
        // FIXME: xiaohai.zhang
        // FIXME: 比如改成这样：
        // FIXME: int inviteStudents = (int) deprecatedInvitationLoaderClient.loadByInviter(currentUserId())
        // FIXME:         .originalLocationsAsList()
        // FIXME:         .stream()
        // FIXME:         .filter(t -> !t.isDisabled())
        // FIXME:         .filter(t -> t.getType() == InvitationType.STUDENT_INVITE_STUDENT_LINK)
        // FIXME:         .filter(t -> t.getInviteeId() != 0)
        // FIXME:         .map(InviteHistory.Location::getInviteeId)
        // FIXME:         .distinct()
        // FIXME:         .count();
        int inviteStudents = (int) asyncInvitationServiceClient.loadByInviter(user.getId())
                .filter(t -> t.getType() == InvitationType.STUDENT_INVITE_STUDENT_LINK)
                .count();
        return MapMessage.successMessage().add("inviteStudents", inviteStudents);
    }

    @RequestMapping(value = "teachersource.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTeacherSource() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录再参加此次活动!");
        }
        if (user.fetchUserType() != UserType.TEACHER) {
            return MapMessage.errorMessage("只有老师才能参加此活动");
        }
        String source = getRequestString("source");//答案
        int awardStatus = getRequestInt("awardStatus");//0 不发金币  1 发金币
        if (StringUtils.isBlank(source)) {
            return MapMessage.errorMessage("请选择正确的答案");
        }
        try {
            return atomicLockManager.wrapAtomic(miscServiceClient)
                    .expirationInSeconds(30)
                    .keyPrefix("TEACHER_SAVE_SOURCE")
                    .keys(user.getId())
                    .proxy()
                    .saveTeacherSourceCampaignAward(user.getId(), source, awardStatus);
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }
}
