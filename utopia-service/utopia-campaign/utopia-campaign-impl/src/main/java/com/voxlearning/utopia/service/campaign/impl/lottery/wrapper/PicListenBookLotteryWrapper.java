package com.voxlearning.utopia.service.campaign.impl.lottery.wrapper;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryBigHistory;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.impl.lottery.AbstractCampaignWrapper;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xinxin
 * @since 8/10/17.
 * 点读机打包购买抽奖活动
 */
@Named
public class PicListenBookLotteryWrapper extends AbstractCampaignWrapper {
    private static final String LOTTERY_CHANCE_CACHE_KEY = "PICLISTENBOOK_LOTTERY_CHANCE_";
    private static final int activityExpiration = (int) (CampaignType.PICLISTENBOOK_ORDER_LOTTERY.getExpiredTime().getTime() / 1000);

    @Inject
    private CouponServiceClient couponServiceClient;

    @Override
    public MapMessage doLottery(CampaignType campaignType, User user, LotteryClientType clientType) {
        if (getLotteryFreeChance(user.getId()) <= 0) {
            return MapMessage.errorMessage("对不起，你的抽奖次数已用完");
        }

        try {
            CampaignLottery lotteryResult = drawLottery(campaignType.getId());
            if (null == lotteryResult) {
                lotteryResult = getLastLottery(campaignType.getId());
            }

            //控制奖品数量
            if (lotteryResult.getAwardId() <= 3) {
                boolean emptyFlag = false;
                // 所有大奖记录
                List<CampaignLotteryBigHistory> histories = campaignLoader.findCampaignLotteryBigHistories(campaignType.getId(), lotteryResult.getAwardId());
                if (histories != null && histories.size() >= getMaxCount(lotteryResult.getAwardId())) {
                    emptyFlag = true;
                }
                if (emptyFlag) {
                    lotteryResult = getLastLottery(campaignType.getId());
                }
            }
            //1、2等奖每人只能抽中一次
            if (lotteryResult.getAwardId() <= 2) {
                List<CampaignLotteryHistory> campaignLotteryHistories = campaignLoader.findCampaignLotteryHistories(campaignType.getId(), user.getId());
                if (CollectionUtils.isNotEmpty(campaignLotteryHistories)) {
                    for (CampaignLotteryHistory history : campaignLotteryHistories) {
                        if (Objects.equals(history.getAwardId(), lotteryResult.getAwardId())) {
                            lotteryResult = getLastLottery(campaignType.getId());
                            break;
                        }
                    }
                }

            }

            //前端要显示最近的10条，1、2等奖要一直显示
            if (lotteryResult.getAwardId() <= 2) {
                addRecentCampaignLotteryResultBig(user, lotteryResult, campaignType.getId(), activityExpiration);
            } else {
                addRecentCampaignLotteryForWeek(user, lotteryResult, campaignType.getId());
            }

            //4、5等奖发优惠券
            if (lotteryResult.getAwardId() >= 4) {
                String couponId = getCouponId(lotteryResult.getAwardId());
                if (StringUtils.isNotBlank(couponId)) {
                    couponServiceClient.sendCoupon(couponId, user.getId());
                }
            }
            //1、2、3等奖记录大奖记录
            if (lotteryResult.getAwardId() <= 3) {
                CampaignLotteryBigHistory bigHistory = new CampaignLotteryBigHistory();
                bigHistory.setAwardId(lotteryResult.getAwardId());
                bigHistory.setUserId(user.getId());
                bigHistory.setCampaignId(campaignType.getId());
                campaignService.$insertCampaignLotteryBigHistory(bigHistory);
            }

            // 记录抽奖结果
            CampaignLotteryHistory userLotteryHistory = new CampaignLotteryHistory();
            userLotteryHistory.setUserId(user.getId());
            userLotteryHistory.setCampaignId(campaignType.getId());
            userLotteryHistory.setAwardId(SafeConverter.toInt(lotteryResult.getAwardId()));
            campaignService.$insertCampaignLotteryHistory(userLotteryHistory);

            campaignCacheSystem.CBS.persistence.decr(LOTTERY_CHANCE_CACHE_KEY + user.getId(), 1, 1, activityExpiration);

            MapMessage result = MapMessage.successMessage();
            Map<String, Object> winLottery = new HashMap<>();
            winLottery.put("campaignId", lotteryResult.getCampaignId());
            winLottery.put("awardId", lotteryResult.getAwardId());
            winLottery.put("awardLevelName", lotteryResult.getAwardLevelName());
            winLottery.put("awardName", lotteryResult.getAwardName());
            result.add("lottery", winLottery);

            return result;
        } catch (Exception ex) {
            logger.error("type:{},uid:{},client:{}", campaignType.name(), user.getId(), clientType.name(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private String getCouponId(Integer awardId) {
        if (RuntimeModeLoader.getInstance().current().ge(Mode.STAGING)) {
            //线上环境
            if (awardId == 4) {
                return "598c2333e485abdc8eab71d4";
            } else if (awardId == 5) {
                return "598c23875d9d4b2ed0cf8a0f";
            }
        } else {
            if (awardId == 4) {
                return "598c23e0e92b1b8bc406266d";
            } else if (awardId == 5) {
                return "598c2417777487034f8af1a4";
            }
        }
        return null;
    }

    @Override
    public Long addLotteryChance(Long userId, int delta) {
        if (0 == userId || 0 >= delta) {
            return 0L;
        }

        Long ret = campaignCacheSystem.CBS.persistence.incr(LOTTERY_CHANCE_CACHE_KEY + userId, 1, 1, activityExpiration);
        if (null == ret) {
            return 0L;
        }

        return ret;
    }

    @Override
    public int getLotteryFreeChance(Long userId) {
        if (0 == userId) {
            return 0;
        }

        CacheObject<Object> objectCacheObject = campaignCacheSystem.CBS.persistence.get(LOTTERY_CHANCE_CACHE_KEY + userId);
        if (null == objectCacheObject || null == objectCacheObject.getValue()) {
            return 0;
        }

        return SafeConverter.toInt(objectCacheObject.getValue());
    }

    private int getMaxCount(Integer awardId) {
        if (awardId == 1) {
            return 1;
        }
        if (awardId == 2) {
            return 20;
        }
        if (awardId == 3) {
            return 8000;
        }
        if (awardId == 4) {
            return Integer.MAX_VALUE;
        }
        if (awardId == 5) {
            return Integer.MAX_VALUE;
        }
        return 0;
    }
}
