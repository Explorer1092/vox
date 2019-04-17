package com.voxlearning.utopia.agent.bean.income;

import com.voxlearning.utopia.agent.persist.entity.AgentOnlinePayShareDetail;
import com.voxlearning.utopia.agent.persist.entity.AgentUserKpiResult;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 15-3-12.
 */
@Data
public class UserRegionIncomeBean implements Serializable {

    private String regionName;
    private List<UserIncomeDetailBean> incomeList;

    public UserRegionIncomeBean(String regionName) {
        this.regionName = regionName;
        this.incomeList = new ArrayList<>();
    }

    protected void appendIncome(AgentUserKpiResult userKpiResult) {
        UserIncomeDetailBean incomeDetail = new UserIncomeDetailBean();
        incomeDetail.setSource(userKpiResult.getKpiName());
        incomeDetail.setCashIncome(userKpiResult.getCashReward().doubleValue());
        incomeDetail.setPointIncome(userKpiResult.getPointReward().doubleValue());

        StringBuilder extInfo = new StringBuilder();
        if (userKpiResult.getKpiName().contains("新增认证人数")) {
            if (userKpiResult.getKpiTarget() > 0) {
                extInfo.append("目标:").append(userKpiResult.getKpiTarget()).append(" ");
                extInfo.append("实绩:").append(userKpiResult.getKpiResult()).append(" ");
                float rate = userKpiResult.getKpiResult() * 1.0f / userKpiResult.getKpiTarget() * 100;
                extInfo.append("完成率:").append(String.format("%.2f", rate)).append("%");
                extInfo.append("1-2年级新增认证人数:").append(userKpiResult.getStudentAuthNumLv());
            } else {
                extInfo.append("实绩:").append(userKpiResult.getKpiResult()).append(" ");
            }
        } else {
            extInfo.append("沃克大冒险线上付费总金额:").append(userKpiResult.getKpiResult());
        }

        incomeDetail.setExtInfo(extInfo.toString());

        incomeList.add(incomeDetail);
    }

    protected void appendIncome(AgentOnlinePayShareDetail onlineShareDetail) {
        UserIncomeDetailBean incomeDetail = new UserIncomeDetailBean();
        incomeDetail.setSource("线上付费分成");
        incomeDetail.setCashIncome(onlineShareDetail.getShareAmount());
        incomeDetail.setPointIncome(0d);

        StringBuilder extInfo = new StringBuilder();
        extInfo.append("月份:").append(onlineShareDetail.getPayMonth()).append(" ");
        extInfo.append("产品:").append(onlineShareDetail.getProductName()).append(" ");
        extInfo.append("总收入:").append(String.format("%.2f", onlineShareDetail.getTotalIncome())).append(" ");
        extInfo.append("实体卡金额:").append(String.format("%.2f", onlineShareDetail.getCardPayAmount())).append(" ");
        extInfo.append("退款金额:").append(String.format("%.2f", onlineShareDetail.getRefundAmount())).append(" ");
        extInfo.append("可分成收入:").append(String.format("%.2f", onlineShareDetail.getShareableAmount())).append(" ");
        extInfo.append("付费人数:").append(onlineShareDetail.getPayUserNum()).append(" ");
        extInfo.append("月活人数:").append(onlineShareDetail.getMonthlyActiveUsers()).append(" ");
        extInfo.append("付费率:").append(String.format("%.2f", onlineShareDetail.getMonthlyPayRate())).append("%");

        incomeDetail.setExtInfo(extInfo.toString());
        incomeList.add(incomeDetail);
    }

    public int getDataSize() {
        return incomeList.size();
    }
}
