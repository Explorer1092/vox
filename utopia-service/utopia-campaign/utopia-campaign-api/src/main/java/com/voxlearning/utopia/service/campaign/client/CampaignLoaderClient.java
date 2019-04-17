package com.voxlearning.utopia.service.campaign.client;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.campaign.api.CampaignLoader;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CampaignLoaderClient {

    @Getter
    @ImportService(interfaceClass = CampaignLoader.class)
    private CampaignLoader campaignLoader;

    public List<Map<String, Object>> loadRecentCampaignLotteryResult(Integer campaignId) {
        String key = CacheKeyGenerator.generateCacheKey("YACampaignLotteryHistory",
                new String[]{"recentCampaignLotteries"},
                new Object[]{campaignId});
        List<Map<String, Object>> recentCampaignLotteries = CacheSystem.CBS.getCache("flushable").load(key);
        if (recentCampaignLotteries == null) {
            return new ArrayList<>();
        } else {
            return recentCampaignLotteries;
        }
    }

    public List<Map<String, Object>> loadRecentCampaignLotteryResultBig(Integer campaignId) {
        String key = CacheKeyGenerator.generateCacheKey("YACampaignLotteryHistory",
                new String[]{"recentCampaignLotteriesBig"},
                new Object[]{campaignId});
        List<Map<String, Object>> recentCampaignLotteries = CacheSystem.CBS.getCache("persistence").load(key);
        if (recentCampaignLotteries == null) {
            return new ArrayList<>();
        } else {
            return recentCampaignLotteries;
        }
    }

    public List<Map<String, Object>> loadCampaignLotteryResultBigForScholarship(Integer campaignId) {
        String key = CacheKeyGenerator.generateCacheKey("YACampaignLotteryHistory",
                new String[]{"campaignLotteriesBig"},
                new Object[]{campaignId});
        List<Map<String, Object>> recentCampaignLotteries = CacheSystem.CBS.getCache("persistence").load(key);
        if (recentCampaignLotteries == null) {
            return new ArrayList<>();
        } else {
            return recentCampaignLotteries;
        }
    }

    public List<Map<String, Object>> loadCampaignLotteryResultBigForTime(Integer campaignId) {
        String key = CacheKeyGenerator.generateCacheKey("YACampaignLotteryHistory",
                new String[]{"campaignLotteriesBigForTime"},
                new Object[]{campaignId});
        List<Map<String, Object>> recentCampaignLotteries = CacheSystem.CBS.getCache("persistence").load(key);
        if (recentCampaignLotteries == null) {
            return new ArrayList<>();
        } else {
            return recentCampaignLotteries;
        }
    }

    public List<Map<String, Object>> loadRecentCampaignLotteryResultForWeek(Integer campaignId) {
        String key = CacheKeyGenerator.generateCacheKey("YACampaignLotteryHistory",
                new String[]{"CampaignWeek"},
                new Object[]{campaignId});
        List<Map<String, Object>> recentCampaignLotteries = CacheSystem.CBS.getCache("flushable").load(key);
        if (recentCampaignLotteries == null) {
            return new ArrayList<>();
        } else {
            return recentCampaignLotteries;
        }
    }


    public List<Map<String, Object>> loadRecentCampaignLotteryResultForStudent(Integer campaignId, Long clazzId) {
        String key = CacheKeyGenerator.generateCacheKey("YACampaignLotteryHistory",
                new String[]{"CAMPID", "CID"},
                new Object[]{campaignId, clazzId});
        List<Map<String, Object>> recentCampaignLotteries = CacheSystem.CBS.getCache("flushable").load(key);
        if (recentCampaignLotteries == null) {
            return new ArrayList<>();
        } else {
            return recentCampaignLotteries;
        }
    }

    public CampaignLottery loadByCampaignIdAndAwardId(Integer campaignId,Integer awardId) {
        return campaignLoader.findCampaignLottery(campaignId, awardId);
    }

    public List<CampaignLotteryHistory> findCampaignLotteryHistories(Integer campaignId, Long userId) {
        return campaignLoader.findCampaignLotteryHistories(campaignId,userId);
    }
}
