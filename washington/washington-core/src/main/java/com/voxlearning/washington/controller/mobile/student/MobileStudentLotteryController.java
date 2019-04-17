package com.voxlearning.washington.controller.mobile.student;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.entity.AwardContext;
import com.voxlearning.utopia.service.campaign.api.CampaignLoader;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.client.CampaignLoaderClient;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.temp.NewSchoolYearActivity;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.dateToString;
import static com.voxlearning.alps.core.util.MapUtils.m;

/**
 * @author Xiaochao.Wei
 * @since 2018/1/30
 */

@Controller
@RequestMapping(value = "/studentMobile/award")
public class MobileStudentLotteryController extends AbstractMobileController {

    @Inject CampaignServiceClient campaignServiceClient;
    @Inject CampaignLoaderClient campaignLoaderClient;


    /**
     * 学生APP奖品中心抽奖
     *
     * @return
     */
    @RequestMapping(value = "lottery.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage doLottery() {
        return MapMessage.errorMessage("功能已下线，试试别的吧!");
//        User user = currentUser();
//        if (user == null) {
//            return MapMessage.errorMessage("用户未登录!").add("code", 400);
//        }
//
//        if (!NewSchoolYearActivity.isInStudentLotteryPeriod()) {
//            return MapMessage.errorMessage("活动已过期");
//        }
//
//        MapMessage mapMessage;
//        try {
//            mapMessage = atomicLockManager.wrapAtomic(campaignServiceClient.getCampaignService())
//                    .expirationInSeconds(30)
//                    .keyPrefix("STUDENT_APP_LOTTERY")
//                    .keys(user.getId())
//                    .proxy()
//                    .drawLottery(CampaignType.STUDENT_APP_LOTTERY, user, LotteryClientType.APP);
//        } catch (DuplicatedOperationException ignore) {
//            return MapMessage.errorMessage("您点击太快了，请重试");
//        }
//        return mapMessage;
    }

    @RequestMapping(value = "lotterylist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lotteryList() {
        List<Map<String, Object>> result = new ArrayList<>();

        List<CampaignLottery> campaignLotteries = campaignLoaderClient.getCampaignLoader().findCampaignLotteries(CampaignType.STUDENT_APP_LOTTERY.getId())
                .stream().peek(campaignLottery -> {
                    List<AwardContext> awardContexts = JSON.parseArray(campaignLottery.getAwardContent(), AwardContext.class);
                    campaignLottery.setSort(awardContexts.get(0).getSort());
                }).sorted((o1, o2) -> {
                    if (o1.getSort() == null) {
                        o1.setSort(Integer.MAX_VALUE);
                    }
                    if (o2.getSort() == null) {
                        o2.setSort(Integer.MAX_VALUE);
                    }
                    return o1.getSort().compareTo(o2.getSort());
                }).collect(Collectors.toList());

        for (CampaignLottery campaignLottery : campaignLotteries) {
            List<AwardContext> awardContexts = JSON.parseArray(campaignLottery.getAwardContent(), AwardContext.class);
            AwardContext awardContext = awardContexts.get(0);
            if (awardContext.isEnable()) {
                result.add(
                        MapUtils.m("name", campaignLottery.getAwardName(),
                                "img", awardContext.getImg(),
                                "describe", awardContext.getDescribe(),
                                "awardId", campaignLottery.getAwardId())
                );
            }
        }
        return MapMessage.successMessage().add("data", result);
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
            return MapMessage.errorMessage("用户未登录!").add("code", 400);
        }

        int count = campaignServiceClient.getCampaignService().getTeacherLotteryFreeChance(CampaignType.STUDENT_APP_LOTTERY, user.getId());

        return MapMessage.successMessage().add("chanceCount", count);
    }

    /**
     * 判断是否是第一次抽奖
     *
     * @return
     */
    @RequestMapping(value = "firstlottery.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage firstLottery() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("用户未登录!").add("code", 400);
        }

        List<CampaignLotteryHistory> campaignLotteryHistories = campaignLoaderClient.getCampaignLoader()
                .findCampaignLotteryHistories(CampaignType.STUDENT_APP_LOTTERY.getId(), user.getId());
        if (CollectionUtils.isEmpty(campaignLotteryHistories)) {
            return MapMessage.successMessage();
        }

        return MapMessage.errorMessage();
    }

    /**
     * 获取大奖列表
     *
     * @return
     */
    @RequestMapping(value = "biglotteryhistories.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage bigLotteryHistories() {

        List<Map<String, Object>> result = campaignLoaderClient.loadCampaignLotteryResultBigForTime(CampaignType.STUDENT_APP_LOTTERY.getId());

        return MapMessage.successMessage().add("result", result);
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
            return MapMessage.errorMessage("用户未登录!").add("code", 400);
        }

        CampaignLoader campaignLoader = campaignLoaderClient.getCampaignLoader();
        int campaignId = CampaignType.STUDENT_APP_LOTTERY.getId();
        String datPattern = "MM.dd HH:mm";
        List<CampaignLotteryHistory> histories = campaignLoader.findCampaignLotteryHistories(campaignId, user.getId());

        List<Map<String, Object>> result = new ArrayList<>();
        boolean isProduct;
        Date createTime;
        for (CampaignLotteryHistory history : histories) {
            Map<String, Object> map = new HashMap<>();

            CampaignLottery cl = campaignLoader.findCampaignLottery(campaignId, history.getAwardId());
            isProduct = Optional.ofNullable(JsonUtils.fromJsonToList(cl.getAwardContent(),Map.class))
                    .orElse(Collections.emptyList())
                    .stream()
                    .anyMatch(c -> Objects.equals(c.get("type"),"RC_PRODUCT"));
            // 实物的中奖记录跳过，从订单里面拿，不然有碰瓷的
            if(isProduct){
                continue;
            }

            map.put("awardId", cl.getAwardId());
            map.put("awardName", cl.getAwardName());

            createTime= history.getCreateDatetime();
            map.put("orgDate", createTime);// 用来比较的
            map.put("date",dateToString(createTime,datPattern));
            result.add(map);
        }

        // 取到抽奖订单，和上面的混一起排序
        rewardLoaderClient.loadUserRewardOrders(user.getId())
                .stream()
                .filter(o -> !SafeConverter.toBoolean(o.getDisabled()))
                .filter(o -> o.getSource() == RewardOrder.Source.gift)
                .map(o -> m("awardName",o.getProductName(),
                        "orgDate",o.getCreateDatetime(),
                        "date",dateToString(o.getCreateDatetime(),datPattern)))
                .forEach(result::add);

        // 倒序排列
        Function<Map<String,Object>,Date> dateFunc = r -> (Date)r.get("orgDate");
        result.sort((r1,r2)-> dateFunc.apply(r2).compareTo(dateFunc.apply(r1)));

        return MapMessage.successMessage().add("result", result);
    }

    @RequestMapping(value = "lotteryshow.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage showLottery() {
        StudentDetail user = currentStudentDetail();
        if (user == null) {
            return MapMessage.errorMessage("用户未登录!").add("code", 400);
        }
        boolean hideShiWu = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(user,"Reward","OfflineShiWu", false);
        /** 毕业班、毕业生也不显示抽奖 **/
        boolean isGraduate = rewardLoaderClient.isGraduate(user);
        return MapMessage.successMessage().add("show", !hideShiWu && !isGraduate);
    }

}
