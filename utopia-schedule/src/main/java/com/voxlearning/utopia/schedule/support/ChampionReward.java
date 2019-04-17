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

package com.voxlearning.utopia.schedule.support;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import org.slf4j.Logger;

import javax.inject.Named;
import java.util.List;

@Named
public class ChampionReward {
    private static final Logger logger = LoggerFactory.getLogger(ChampionReward.class);

    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;

    public void reward(final WeekRange week, List<Long> championIds) {
        championIds = CollectionUtils.toLinkedList(championIds);
        if (CollectionUtils.isEmpty(championIds)) {
            return;
        }
        for (final Long championId : championIds) {
            try {
                doReward(week, championId);
            } catch (Exception ex) {
                logger.warn("CHAMPION REWARD FAILED [{}]: {}", championId, ex.getMessage(), ex);
            }
        }
    }

    private void doReward(WeekRange week, Long championId) {
        String firstWeekDay = DateUtils.dateToString(week.getStartDate(), "yyyy-MM-dd");

        IntegralHistory integralHistory = new IntegralHistory(championId, IntegralType.PK周冠军);
        integralHistory.setIntegral(10);
        integralHistory.setComment("周冠军！加10学豆");
        integralHistory.setPkFirstWeekDayUniqueKey(firstWeekDay);
        MapMessage message = userIntegralService.changeIntegral(integralHistory);
        if (!message.isSuccess()) {
            throw new RuntimeException("ALREADY ADDED @ " + firstWeekDay);
        }
    }
}
