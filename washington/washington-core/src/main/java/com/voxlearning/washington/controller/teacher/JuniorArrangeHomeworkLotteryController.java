package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.client.CampaignLoaderClient;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.temp.NewSchoolYearActivity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Xiaochao.Wei
 * @since 2018/3/1
 */

@Controller
@RequestMapping("/teacher/activity/term2018")
public class JuniorArrangeHomeworkLotteryController extends AbstractTeacherController {

    @Inject CampaignServiceClient campaignServiceClient;
    @Inject CampaignLoaderClient campaignLoaderClient;

    /**
     * 初中英语老师布置作业抽奖-前端页面
     */
    @RequestMapping(value = "lotteryindex.vpage", method = RequestMethod.GET)
    public String teacherAwardSpring() {
        return "project/teacherawardjunior/index";
    }

    /**
     * 初中英语老师布置作业抽奖
     *
     * @return
     */
    @RequestMapping(value = "dolottery.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage doLottery() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("用户未登录!");
        }
        if (!NewSchoolYearActivity.isInTeacherLotteryPeriod()) {
            return MapMessage.errorMessage("活动已过期");
        }
        if (!teacher.isJuniorTeacher() && !teacher.isEnglishTeacher()) {
            return MapMessage.errorMessage("本次活动只支持初中英语老师参与，请您关注其他活动哦~");
        }

        if (teacher.fetchCertificationState() == null || teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            return MapMessage.errorMessage("认证老师才能参与活动哦\n快去达成认证吧~");
        }

        try {
            return atomicLockManager.wrapAtomic(campaignServiceClient.getCampaignService())
                    .expirationInSeconds(30)
                    .keyPrefix("JUNIOR_ARRANGE_HOMEWORK_LOTTERY")
                    .keys(teacher.getId())
                    .proxy()
                    .drawLottery(CampaignType.JUNIOR_ARRANGE_HOMEWORK_LOTTERY, teacher, LotteryClientType.PC);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    /**
     * 查看当天剩余抽奖次数
     *
     * @return
     */
    @RequestMapping(value = "getchance.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getChance() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("用户未登录!");
        }
//        MapMessage msg = campaignServiceClient.getCampaignService().addLotteryFreeChance(CampaignType.JUNIOR_ARRANGE_HOMEWORK_LOTTERY, user.getId(),10);
        int count = campaignServiceClient.getCampaignService().getTeacherLotteryFreeChance(CampaignType.JUNIOR_ARRANGE_HOMEWORK_LOTTERY, user.getId());

        return MapMessage.successMessage().add("chanceCount", count);
    }

    /**
     * 查看当天是否布置过作业
     *
     * @return
     */
    @RequestMapping(value = "hadArranged.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage hadArranged() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("用户未登录!");
        }

        return campaignServiceClient.getCampaignService().hadArrangedHomework(CampaignType.JUNIOR_ARRANGE_HOMEWORK_LOTTERY, user.getId());
    }

    /**
     * 获取奖品列表
     *
     * @return
     */
    @RequestMapping(value = "lotteryhistories.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage lotteryHistories() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("用户未登录!");
        }

        List<Map<String, Object>> bigLotteryHistories =
                campaignLoaderClient.loadCampaignLotteryResultBigForTime(CampaignType.JUNIOR_ARRANGE_HOMEWORK_LOTTERY.getId());

        List<CampaignLotteryHistory> campaignLotteryHistories = campaignLoaderClient.getCampaignLoader()
                .findCampaignLotteryHistories(CampaignType.JUNIOR_ARRANGE_HOMEWORK_LOTTERY.getId(), user.getId())
                .stream()
                .filter(p -> p.getAwardId() != 7)//7是谢谢参与
                .collect(Collectors.toList());

        List<Map<String, Object>> lotteryHistories = new ArrayList<>();
        for (CampaignLotteryHistory history : campaignLotteryHistories) {
            Map<String, Object> map = new HashMap<>();
            CampaignLottery campaignLottery = campaignLoaderClient.getCampaignLoader()
                    .findCampaignLottery(history.getCampaignId(), history.getAwardId());
            SimpleDateFormat format = new SimpleDateFormat("MM月dd日 HH:mm");
            String dateString = format.format(history.getCreateDatetime());
            map.put("awardName", campaignLottery.getAwardName());
            map.put("awardId", campaignLottery.getAwardId());
            map.put("date", dateString);
            lotteryHistories.add(map);
        }

        return MapMessage.successMessage().add("bigLotteryHistories", bigLotteryHistories)
                .add("lotteryHistories", lotteryHistories);
    }
}
