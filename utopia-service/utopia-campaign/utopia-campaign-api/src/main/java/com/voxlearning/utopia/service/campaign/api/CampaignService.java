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

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.*;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.concurrent.TimeUnit;

/**
 * Campaign service abstraction.
 *
 * @author Xiaohai Zhang
 * @since Aug 5, 2016
 */
@ServiceVersion(version = "2018.05.24")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface CampaignService extends IPingable {

    boolean $updateCampaignLotteryRate(Long id, Integer rate);

    CampaignAward $insertCampaignAward(CampaignAward document);

    CampaignLotteryHistory $insertCampaignLotteryHistory(CampaignLotteryHistory document);

    CampaignLotteryBigHistory $insertCampaignLotteryBigHistory(CampaignLotteryBigHistory document);

    CampaignLotterySendHistory $insertCampaignLotterySendHistory(CampaignLotterySendHistory document);

    CampaignLotteryFragmentHistory $insertCampaignLotteryFragmentHistory(CampaignLotteryFragmentHistory document);

    MapMessage saveFirstGradeCampaignAward(Long userId);

    MapMessage addLotteryFreeChance(CampaignType campaignType, Long userId, int delta);

    MapMessage drawLottery(CampaignType campaignType, User user, LotteryClientType clientType);

    int getTeacherLotteryFreeChance(CampaignType campaignType, Long userId);

    MapMessage studentSendLotteryChance(CampaignType type, User user, Long studentId);

    MapMessage hadArrangedHomework(CampaignType campaignType, Long userId);

    CampaignLottery $insertCampaignLottery(CampaignLottery campaignLottery);

    CampaignLottery $updateCampaignLottery(CampaignLottery campaignLottery);

    MapMessage validateCampaignLottery(CampaignType campaignType, CampaignLottery campaignLottery);

}
