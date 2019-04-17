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

package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.campaign.api.document.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Campaign loader abstraction.
 *
 * @author Xiaohai Zhang
 * @since Aug 5, 2016
 */
@ServiceVersion(version = "2018.05.24")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface CampaignLoader extends IPingable {

    @Idempotent
    @CacheMethod(type = CampaignAward.class, writeCache = false)
    List<CampaignAward> findCampaignAwards(@CacheParameter("CID") Integer campaignId,
                                           @CacheParameter("UID") Long userId);

    CampaignLottery findCampaignLotterie(@CacheParameter("ID") Long id);

    @Idempotent
    @CacheMethod(type = CampaignLottery.class, writeCache = false)
    List<CampaignLottery> findCampaignLotteries(@CacheParameter("CID") Integer campaignId);

    @Idempotent
    @CacheMethod(type = CampaignLottery.class, writeCache = false)
    CampaignLottery findCampaignLottery(@CacheParameter("CID") Integer campaignId,@CacheParameter("AID") Integer awardId);

    @Idempotent
    @CacheMethod(type = CampaignLotteryHistory.class, writeCache = false)
    List<CampaignLotteryHistory>
    findCampaignLotteryHistories(@CacheParameter("CID") Integer campaignId,
                                 @CacheParameter("UID") Long userId);

    @Idempotent
    @CacheMethod(type = CampaignLotteryBigHistory.class, writeCache = false)
    List<CampaignLotteryBigHistory>
    findCampaignLotteryBigHistories(@CacheParameter("CID") Integer campaignId);

    @Idempotent
    @CacheMethod(type = CampaignLotteryBigHistory.class, writeCache = false)
    List<CampaignLotteryBigHistory>
    findCampaignLotteryBigHistories(@CacheParameter("CID") Integer campaignId,
                                    @CacheParameter("AID") Integer awardId);

    @CacheMethod(type = CampaignLotteryBigHistory.class, writeCache = false)
    List<CampaignLotteryBigHistory>
    findCampaignLotteryBigHistoriesUnderUser(@CacheParameter("UID") Long userId);

    @Idempotent
    @CacheMethod(type = CampaignLotterySendHistory.class, writeCache = false)
    List<CampaignLotterySendHistory>
    findCampaignLotterySendHistories(@CacheParameter("CID") Integer campaignId,
                                     @CacheParameter("RID") Long receiverId);

    @Idempotent
    @CacheMethod(type = CampaignLotteryFragmentHistory.class, writeCache = false)
    List<CampaignLotteryFragmentHistory>
    findCampaignLotteryFragmentHistories(@CacheParameter("CID") Integer campaignId,
                                         @CacheParameter("UID") Long userId);

    @Idempotent
    List<CampaignLotteryBigHistory> loadAllCampaignLotteryBigHistorys();
}
