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

package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ChargeType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Business dubbo proxy service.
 */
@ServiceVersion(version = "2016.08.19")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface DPBusinessService {

    // ========================================================================
    // com.voxlearning.utopia.business.api.BusinessTeacherService
    // ========================================================================

    MapMessage teacherInviteTeacherBySms(User user, String mobile, String realname, InvitationType type, String subject);

    MapMessage wakeUpInvitedTeacherBySms(User inviter, User invitee, InvitationType type);

    // ========================================================================
    // com.voxlearning.utopia.business.api.BusinessFinanceService
    // ========================================================================

    MapMessage saveWirelessCharging_junior(Long userId, ChargeType chargeType, Integer amount, String smsMessage, String extraDesc);

    // ========================================================================
    // com.voxlearning.utopia.business.api.MiscService
    // ========================================================================

    MapMessage addLotteryFreeChance(CampaignType campaignType, Long userId, int delta);

    MapMessage contentFeedBackReward(Long teacherId, Integer rewardIntegral, String message, String questionId, String linkUrl);

    MapMessage fetchSameAppActiveNumByStudentId(Long studentId, List<String> appkeys);
}
