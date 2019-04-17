package com.voxlearning.utopia.service.campaign.impl.lottery.wrapper;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryBigHistory;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.impl.lottery.AbstractCampaignWrapper;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2017/3/30.
 */
@Named
public class TeacherScholarshipSilverWrapper extends AbstractCampaignWrapper {
    private static final String lotteryChanceKey = "TEACHER_SCHOLARSHIP_LOTTERY_FREE_CHANCE:";
    private static final int activityExpiration = (int) (DateUtils.stringToDate("2017-07-15 23:59:59").getTime() / 1000);

    @Override
    public int getLotteryFreeChance(Long userId) {
        String key = CacheKeyGenerator.generateCacheKey(lotteryChanceKey, null, new Object[]{userId});
        CacheObject<String> cacheObject = campaignCacheSystem.CBS.persistence.get(key);
        if (cacheObject != null && StringUtils.isNotBlank(cacheObject.getValue())) {
            return NumberUtils.toInt(StringUtils.trim(cacheObject.getValue()));
        }
        return 0;
    }

    @Override
    public MapMessage doLottery(CampaignType campaignType, final User user, LotteryClientType clientType) {
        if (getLotteryFreeChance(user.getId()) <= 0) {
            return MapMessage.errorMessage("对不起，您没有抽奖次数了");
        }
        try {
            // 抽奖处理
            CampaignLottery lotteryResult = drawLottery(campaignType.getId());
            if (lotteryResult == null) {
                return MapMessage.successMessage().add("win", false);
            }
            // 总大奖个数限制
            if (lotteryResult.getAwardId() <= 3) {
                boolean emptyFlag = false;
                // 未认证老师不能中大奖
                if (user.fetchCertificationState() != AuthenticationState.SUCCESS) {
                    emptyFlag = true;
                } else {
                    // 所有大奖记录
                    List<CampaignLotteryBigHistory> histories = campaignLoader.findCampaignLotteryBigHistories(campaignType.getId(), lotteryResult.getAwardId());
                    // 过滤大奖个数
                    if (histories != null && histories.size() >= getMaxCount(lotteryResult.getAwardId())) {
                        emptyFlag = true;
                    }
                    //判断当前用户是否中过大奖
                    List<CampaignLotteryBigHistory> bigHis = campaignLoader.loadAllCampaignLotteryBigHistorys()
                            .stream().filter(h -> Objects.equals(user.getId(), h.getUserId())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(bigHis)) {
                        emptyFlag = true;
                    }
                }
                if (emptyFlag) {
                    lotteryResult = getLastLottery(campaignType.getId());
                }
            }
            MapMessage result = MapMessage.successMessage();
            result.add("win", true);
            Map<String, Object> winLottery = new HashMap<>();
            winLottery.put("campaignId", lotteryResult.getCampaignId());
            winLottery.put("awardId", lotteryResult.getAwardId());
            winLottery.put("awardLevelName", lotteryResult.getAwardLevelName());
            winLottery.put("awardName", lotteryResult.getAwardName());
            result.add("lottery", winLottery);
            // 记录抽奖结果
            CampaignLotteryHistory userLotteryHistory = new CampaignLotteryHistory();
            userLotteryHistory.setUserId(user.getId());
            userLotteryHistory.setCampaignId(campaignType.getId());
            userLotteryHistory.setAwardId(SafeConverter.toInt(winLottery.get("awardId")));
            campaignService.$insertCampaignLotteryHistory(userLotteryHistory);
            // 记录最近的用户中奖信息
            if (lotteryResult.getAwardId() <= 7 && lotteryResult.getAwardId() > 3) {
                addRecentCampaignLotteryForWeek(user, lotteryResult, CampaignType.TEACHER_SCHOLARSHIP_GOLD_LOTTERY.getId());
            }
            if (SafeConverter.toInt(winLottery.get("awardId")) <= 3) {
                addCampaignLotteryResultBig(user, lotteryResult, CampaignType.TEACHER_SCHOLARSHIP_GOLD_LOTTERY.getId(), activityExpiration);
                //发邮件
                if (RuntimeMode.ge(Mode.STAGING) && SafeConverter.toInt(winLottery.get("awardId")) <= 3) {
                    Map<String, Object> content = new HashMap<>();
                    content.put("info", "大奖产生，得奖人ID：" + user.getId() + "，中了" + winLottery.get("awardName"));
                    emailServiceClient.createTemplateEmail(EmailTemplate.office)
                            .to("xiaohan.yu@17zuoye.com;jing.ning@17zuoye.com")
                            .cc("xiaopeng.yang@17zuoye.com")
                            .subject("中奖老师资料（奖学金活动银奖池）")
                            .content(content)
                            .send();
                }
                // 记录大奖记录
                CampaignLotteryBigHistory bigHistory = new CampaignLotteryBigHistory();
                bigHistory.setAwardId(ConversionUtils.toInt(winLottery.get("awardId")));
                bigHistory.setUserId(user.getId());
                bigHistory.setCampaignId(campaignType.getId());
                campaignService.$insertCampaignLotteryBigHistory(bigHistory);
            }
            //发奖
            int value = 0;
            switch (SafeConverter.toInt(winLottery.get("awardId"))) {
                case 4:
                    value = 50;
                    break;
                case 5:
                    value = 10;
                    break;
                case 6:
                    value = 5;
                    break;
                case 7:
                    value = 1;
                    break;
                default:
            }
            // 发金币
            if (value > 0) {
                IntegralHistory integralHistory = new IntegralHistory(user.getId(), IntegralType.TEACHER_SCHOLARSHIP_LOTTERY_ACTIVITY, value * 10);
                integralHistory.setComment("抽奖奖励");
                if (!userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory).isSuccess()) {
                    logger.error("teacher scholarship lottery add integral error, tid {}, amount {}", user.getId(), value);
                }
            }
            // 免费机会减1
            String key = CacheKeyGenerator.generateCacheKey(lotteryChanceKey, null, new Object[]{user.getId()});
            campaignCacheSystem.CBS.persistence.decr(
                    key,
                    1,
                    1,
                    activityExpiration);
            return result;
        } catch (Exception ex) {
            logger.error("TEACHER DRAW SCHOLARSHIP LOTTERY ERROR, {}", ex.getMessage());
            return MapMessage.errorMessage("抽奖失败");
        }
    }

    private int getMaxCount(Integer awardId) {
        if (awardId == 1) {
            return 2;
        }
        if (awardId == 2) {
            return 2;
        }
        if (awardId == 3) {
            return 3;
        }
        return 0;
    }

    public Long addLotteryChance(Long userId, int delta) {
        if (delta == 0) {
            return 0L;
        }
        String key = CacheKeyGenerator.generateCacheKey(lotteryChanceKey, null, new Object[]{userId});
        Long ret = campaignCacheSystem.CBS.persistence.incr(key, delta, delta, activityExpiration);
        if (ret == null) {
            logger.error("Failed increase {} teacher scholarship lottery free chance with delta {}", userId, delta);
            return 0L;
        }
        return ret;
    }
}
