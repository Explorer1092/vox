package com.voxlearning.utopia.agent.bean.incomes2016;

import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 2016春季地区region的income bean
 * Created by alex on 2016/3/29.
 */
@Data
public class PartRegionIncomeS2016Bean implements Serializable {

    private String groupName;
    private Map<String, UserIncomeS2016Bean> userIncomeData;

    private Long totalIncome;

    public PartRegionIncomeS2016Bean(String groupName) {
        this.groupName = groupName;
        this.userIncomeData = new LinkedHashMap<>();
        totalIncome = 0L;
    }

    public void appendIncome(AgentUserKpiResultSpring2016 kpiResult) {
        if (kpiResult == null) {
            return;
        }

        String userId = String.valueOf(kpiResult.getUserId());

        UserIncomeS2016Bean userIncome = userIncomeData.get(userId);
        if (userIncome == null) {
            userIncome = new UserIncomeS2016Bean(kpiResult.getUserId(), kpiResult.getUserName());
            userIncomeData.put(userId, userIncome);
        }

        userIncome.appendIncome(kpiResult);

        totalIncome += kpiResult.getCpaSalary();

    }

    public int getDataSize() {
        int retDataSize = 0;

        Set<String> keys = userIncomeData.keySet();
        for (String key : keys) {
            retDataSize += userIncomeData.get(key).getDataSize();
        }

        // 加上区域合计
        retDataSize++;

        return retDataSize;
    }

}
