package com.voxlearning.utopia.agent.bean.incomes2016;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 2016春季用户地区收入详细
 * Created by alex on 2016/3/29.
 */
@Data
public class UserRegionIncomeS2016Bean implements Serializable {

    private String regionName;
    private List<IncomeDetailS2016Bean> incomeList;

    public UserRegionIncomeS2016Bean(String regionName) {
        this.regionName = regionName;
        this.incomeList = new ArrayList<>();
    }

    protected void appendIncome(AgentUserKpiResultSpring2016 kpiResult) {
        IncomeDetailS2016Bean incomeDetail = new IncomeDetailS2016Bean();
        incomeDetail.setSource(kpiResult.getCpaType());
        incomeDetail.setIncome(kpiResult.getCpaSalary());
        incomeDetail.setStartTime(kpiResult.getStartDate());
        incomeDetail.setEndTime(kpiResult.getEndDate());

        StringBuilder extInfo = new StringBuilder();
        extInfo.append("目标:").append(kpiResult.getCpaTarget());
        extInfo.append(",结果:").append(kpiResult.getCpaResult());
        if (StringUtils.isNoneBlank(kpiResult.getCpaNote())) {
            extInfo.append(",").append(kpiResult.getCpaNote());
        }

        incomeDetail.setExtInfo(extInfo.toString());

        incomeList.add(incomeDetail);
    }

    public int getDataSize() {
        return incomeList.size();
    }

}
