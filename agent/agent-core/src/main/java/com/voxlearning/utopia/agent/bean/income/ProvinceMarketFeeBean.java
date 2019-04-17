package com.voxlearning.utopia.agent.bean.income;

import com.voxlearning.utopia.agent.persist.entity.AgentOnlinePayShareDetail;
import com.voxlearning.utopia.agent.persist.entity.AgentUserKpiResult;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

/**
 *
 * Created by Alex on 15-3-13.
 */
@Data
public class ProvinceMarketFeeBean implements Serializable {

    private String provinceName;
    private Map<String, UserIncomeBean> userIncomeData;

    private Double totalCashIncome;   // 现金收入
    private Double totalPointIncome;  // 点数收入

    private Boolean managerChecked;
    private Boolean financeChecked;

    private List<Long> authNumDataIds;
    private List<Long> onlineShareDataIds;

    public ProvinceMarketFeeBean(String provinceName) {
        this.provinceName = provinceName;
        this.userIncomeData = new LinkedHashMap<>();
        totalCashIncome = 0d;
        totalPointIncome = 0d;

        managerChecked = true;
        financeChecked = true;
        authNumDataIds = new ArrayList<>();
        onlineShareDataIds = new ArrayList<>();
    }

    public void appendIncome(AgentUserKpiResult userKpiResult) {
        if (userKpiResult == null) {
            return;
        }

        String userId = String.valueOf(userKpiResult.getUserId());

        UserIncomeBean userIncome = userIncomeData.get(userId);
        if (userIncome == null) {
            userIncome = new UserIncomeBean(userKpiResult.getUserId(), userKpiResult.getUserName());
            userIncomeData.put(userId, userIncome);
        }

        userIncome.appendIncome(userKpiResult);

        totalCashIncome += userKpiResult.getCashReward().doubleValue();
        totalPointIncome += userKpiResult.getPointReward().doubleValue();

        if (!userKpiResult.getFinanceCheck()) {
            financeChecked = false;
        }

        if (!userKpiResult.getManagerCheck()) {
            managerChecked = false;
        }

        authNumDataIds.add(userKpiResult.getId());
    }

    public void appendIncome(AgentOnlinePayShareDetail onlineShareDetail) {
        if (onlineShareDetail == null) {
            return;
        }

        String userId = String.valueOf(onlineShareDetail.getUserId());

        UserIncomeBean userIncome = userIncomeData.get(userId);
        if (userIncome == null) {
            userIncome = new UserIncomeBean(onlineShareDetail.getUserId(), onlineShareDetail.getUserName());
            userIncomeData.put(userId, userIncome);
        }

        userIncome.appendIncome(onlineShareDetail);

        totalCashIncome += onlineShareDetail.getShareAmount();

        if (!onlineShareDetail.getFinanceCheck()) {
            financeChecked = false;
        }

        if (!onlineShareDetail.getManagerCheck()) {
            managerChecked = false;
        }

        onlineShareDataIds.add(onlineShareDetail.getId());
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

    public String getDataIdList() {
        StringBuilder builder = new StringBuilder();

        if (authNumDataIds.size() > 0) {
            for(Long authNumDataId : authNumDataIds) {
                builder.append(authNumDataId).append(",");
            }

            builder.deleteCharAt(builder.toString().length() - 1);
        }

        builder.append("#");

        if (onlineShareDataIds.size() > 0) {
            for(Long onlineShareDataId : onlineShareDataIds) {
                builder.append(onlineShareDataId).append(",");
            }

            builder.deleteCharAt(builder.toString().length() - 1);
        }

        return builder.toString();
    }

}
