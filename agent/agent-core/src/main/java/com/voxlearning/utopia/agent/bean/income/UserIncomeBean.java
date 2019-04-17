/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.bean.income;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.persist.entity.AgentOnlinePayShareDetail;
import com.voxlearning.utopia.agent.persist.entity.AgentUserKpiResult;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * Created by Alex on 15-3-12.
 */
@Data
public class UserIncomeBean implements Serializable {

    private Long userId;
    private String userName;
    private Map<String, UserDurationIncomeBean> durationIncomeData;
    private Double totalCashIncome;   // 现金收入
    private Double totalPointIncome;  // 点数收入

    public UserIncomeBean(Long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.durationIncomeData = new LinkedHashMap<>();
        totalCashIncome = 0d;
        totalPointIncome = 0d;

    }

    public void appendIncome(AgentUserKpiResult userKpiResult) {
        if (userKpiResult == null) {
            return;
        }

        String startTime = DateUtils.dateToString(userKpiResult.getStartTime(), DateUtils.FORMAT_SQL_DATE);
        String endTime = DateUtils.dateToString(userKpiResult.getEndTime(), DateUtils.FORMAT_SQL_DATE);
        String duration = StringUtils.join(startTime, " - ", endTime);

        UserDurationIncomeBean durationIncome = durationIncomeData.get(duration);
        if (durationIncome == null) {
            durationIncome = new UserDurationIncomeBean(duration);
            durationIncomeData.put(duration, durationIncome);
        }

        durationIncome.appendIncome(userKpiResult);

        totalCashIncome += userKpiResult.getCashReward().doubleValue();
        totalPointIncome += userKpiResult.getPointReward().doubleValue();

    }

    public void appendIncome(AgentOnlinePayShareDetail onlineShareDetail) {
        if (onlineShareDetail == null) {
            return;
        }

        String startTime = DateUtils.dateToString(onlineShareDetail.getStartTime(), DateUtils.FORMAT_SQL_DATE);
        String endTime = DateUtils.dateToString(onlineShareDetail.getEndTime(), DateUtils.FORMAT_SQL_DATE);
        String duration = StringUtils.join(startTime, " - ", endTime);

        UserDurationIncomeBean durationIncome = durationIncomeData.get(duration);
        if (durationIncome == null) {
            durationIncome = new UserDurationIncomeBean(duration);
            durationIncomeData.put(duration, durationIncome);
        }

        durationIncome.appendIncome(onlineShareDetail);

        totalCashIncome += onlineShareDetail.getShareAmount();
    }

    public int getDataSize() {
        int retDataSize = 0;

        Set<String> keys = durationIncomeData.keySet();
        for (String key : keys) {
            retDataSize += durationIncomeData.get(key).getDataSize();
        }

        // 加上用户小计数据
        retDataSize++;

        return retDataSize;
    }

}
