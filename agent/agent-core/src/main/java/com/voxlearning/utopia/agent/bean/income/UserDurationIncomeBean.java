package com.voxlearning.utopia.agent.bean.income;

import com.voxlearning.utopia.agent.persist.entity.AgentOnlinePayShareDetail;
import com.voxlearning.utopia.agent.persist.entity.AgentUserKpiResult;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Alex on 15-3-12.
 */
@Data
public class UserDurationIncomeBean implements Serializable {

    private String duration;

    private Map<String, UserRegionIncomeBean> regionIncomeData;

    public UserDurationIncomeBean(String duration) {
        this.duration = duration;
        regionIncomeData = new LinkedHashMap<>();
    }

    protected void appendIncome(AgentUserKpiResult userKpiResult) {
        String regionName = userKpiResult.getRegionName();
        UserRegionIncomeBean regionIncome = regionIncomeData.get(regionName);
        if (regionIncome == null) {
            regionIncome = new UserRegionIncomeBean(regionName);
            regionIncomeData.put(regionName, regionIncome);
        }

        regionIncome.appendIncome(userKpiResult);
    }

    protected void appendIncome(AgentOnlinePayShareDetail onlineShareDetail) {
        String regionName = onlineShareDetail.getRegionName();
        UserRegionIncomeBean regionIncome = regionIncomeData.get(regionName);
        if (regionIncome == null) {
            regionIncome = new UserRegionIncomeBean(regionName);
            regionIncomeData.put(regionName, regionIncome);
        }

        regionIncome.appendIncome(onlineShareDetail);
    }

    public int getDataSize() {
        int retDataSize = 0;

        Set<String> keys = regionIncomeData.keySet();
        for (String key : keys) {
            retDataSize += regionIncomeData.get(key).getDataSize();
        }

        return retDataSize;
    }
}
