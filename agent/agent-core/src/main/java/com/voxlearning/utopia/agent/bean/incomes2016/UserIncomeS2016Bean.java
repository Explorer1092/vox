package com.voxlearning.utopia.agent.bean.incomes2016;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 2016春季用户收入
 * Created by alex on 2016/3/29.
 */
@Data
public class UserIncomeS2016Bean implements Serializable {

    private Long userId;
    private String userName;
    private Map<String, UserRegionIncomeS2016Bean> userRegionIncomeData;
    private Long userIncome;   // 现金收入

    private Boolean marketChecked;
    private Boolean financeChecked;

    private Date starTime;
    private Date endTime;

    public UserIncomeS2016Bean(Long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.userRegionIncomeData = new LinkedHashMap<>();
        this.userIncome = 0L;
        this.marketChecked = true;
        this.financeChecked = true;
    }

    public void appendIncome(AgentUserKpiResultSpring2016 kpiResult) {
        if (kpiResult == null) {
            return;
        }

        String regionName = kpiResult.getCountyName();
        //FIXME
        if (StringUtils.isBlank(regionName)) {
            regionName = "-";
        }

        UserRegionIncomeS2016Bean regionIncome = userRegionIncomeData.get(regionName);
        if (regionIncome == null) {
            regionIncome = new UserRegionIncomeS2016Bean(regionName);
            userRegionIncomeData.put(regionName, regionIncome);
        }

        regionIncome.appendIncome(kpiResult);

        userIncome += kpiResult.getCpaSalary();

        if (!kpiResult.getFinanceCheck()) {
            financeChecked = false;
        }

        if (!kpiResult.getMarketCheck()) {
            marketChecked = false;
        }

        if (starTime == null) {
            starTime = kpiResult.getStartDate();
        }

        if (endTime == null) {
            endTime = kpiResult.getEndDate();
        }

    }

    public int getDataSize() {
        int retDataSize = 0;

        Set<String> keys = userRegionIncomeData.keySet();
        for (String key : keys) {
            retDataSize += userRegionIncomeData.get(key).getDataSize();
        }

        // 加上用户小计数据
        retDataSize++;

        return retDataSize;
    }

}
