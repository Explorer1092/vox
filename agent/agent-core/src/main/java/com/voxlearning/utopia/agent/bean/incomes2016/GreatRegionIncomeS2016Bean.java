package com.voxlearning.utopia.agent.bean.incomes2016;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 2016春季大区的IncomeBean
 * Created by alex on 2016/3/29.
 */
@Data
public class GreatRegionIncomeS2016Bean implements Serializable {

    private String groupName;
    private Map<String, PartRegionIncomeS2016Bean> partRegionIncomeData;

    private Long totalIncome;

    public GreatRegionIncomeS2016Bean(String groupName) {
        this.groupName = groupName;
        this.partRegionIncomeData = new LinkedHashMap<>();
        this.totalIncome = 0L;
    }

    public void appendIncome(AgentUserKpiResultSpring2016 kpiResult) {
        if (kpiResult == null) {
            return;
        }

        String partRegionName = kpiResult.getProvinceName();
        if(StringUtils.isBlank(partRegionName)){
            partRegionName = "-";
        }

        PartRegionIncomeS2016Bean partRegionIncome = partRegionIncomeData.get(partRegionName);
        if (partRegionIncome == null) {
            partRegionIncome = new PartRegionIncomeS2016Bean(partRegionName);
            partRegionIncomeData.put(partRegionName, partRegionIncome);
        }

        partRegionIncome.appendIncome(kpiResult);

        totalIncome += kpiResult.getCpaSalary();

    }

    public int getDataSize() {
        int retDataSize = 0;

        Set<String> keys = partRegionIncomeData.keySet();
        for (String key : keys) {
            retDataSize += partRegionIncomeData.get(key).getDataSize();
        }

        // 加上区域合计
        retDataSize++;

        return retDataSize;
    }

}
