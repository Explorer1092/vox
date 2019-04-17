package com.voxlearning.utopia.service.campaign.impl.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import org.slf4j.Logger;

abstract public class CampaignLotteryHelper {

    private static final Logger logger = LoggerFactory.getLogger(CampaignLotteryHelper.class);

    public static boolean isBig(CampaignLottery campaignLottery) {
        //超级大奖肯定是大奖
        if (isSuperBig(campaignLottery)) {
            return true;
        }
        String awardContent = campaignLottery.getAwardContent();
        if (StringUtils.isEmpty(awardContent)) {
            return false;
        }
        try {
            JSONArray jsonArray = JSON.parseArray(awardContent);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            Boolean big = jsonObject.getBoolean("big");
            if (big != null && big) {
                return true;
            }
        } catch (Exception e) {
            logger.error("学生app抽奖中心大奖配置错误", e);
        }
        return false;
    }

    public static boolean isSuperBig(CampaignLottery campaignLottery) {
        String awardContent = campaignLottery.getAwardContent();
        if (StringUtils.isEmpty(awardContent)) {
            return false;
        }
        try {
            JSONArray jsonArray = JSON.parseArray(awardContent);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            Boolean superBig = jsonObject.getBoolean("superBig");
            if (superBig != null && superBig) {
                return true;
            }
        } catch (Exception e) {
            logger.error("学生app抽奖中心超级大奖配置错误", e);
        }
        return false;
    }
}
