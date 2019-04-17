package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.agent.persist.entity.statistics.PaymentDataSummary;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.entity.afenti.AfentiOrder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * Created by Alex on 15-1-21.
 */
@Data
public class PaymentSummaryBean implements Serializable {

    private Date startDate;
    private Date endDate;
    private boolean dailySplit;
    private boolean productSplit;
    private String groupType;

    private Map<String, GroupPaymentSummaryInfoData> groupSummaryData;

    public PaymentSummaryBean(Date startDate, Date endDate, Boolean dailySplit, Boolean productSplit, String groupType) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailySplit = dailySplit;
        this.productSplit = productSplit;
        this.groupType = groupType;
        groupSummaryData = new LinkedHashMap<>();
    }

    public void append(AfentiOrder order) {
        if (order == null || !DailyPaymentSummaryInfoData.isValidProduct(OrderProductServiceType.safeParse(order.getProductServiceType()))) {
            return;
        }

        if (!"clazz".equals(groupType) && !"school".equals(groupType)) {
            throw new RuntimeException("Can't append afenti order when group type is " + groupType);
        }

        String groupId = String.valueOf(order.getSchoolId());
        String groupName = order.getSchoolName();
        if ("clazz".equals(groupType)) {
            groupId = String.valueOf(order.getClazzId());
            groupName = order.getClazzName();
        }

        GroupPaymentSummaryInfoData groupData;
        if (groupSummaryData.containsKey(groupId)) {
            groupData = groupSummaryData.get(groupId);
        } else {
            groupData = new GroupPaymentSummaryInfoData(groupType, groupId, groupName, startDate, endDate, dailySplit, productSplit);
            groupSummaryData.put(groupId, groupData);
        }

        groupData.append(order);
    }

    public void append(PaymentDataSummary order) {
        if (order == null) {
            return;
        }

        if (!"region".equals(groupType)) {
            throw new RuntimeException("Can't append DailyIncreasementPaymentData when group type is " + groupType);
        }

        String groupId = String.valueOf(order.getRegion_code());
        String groupName = order.getRegion_name();

        GroupPaymentSummaryInfoData groupData;
        if (groupSummaryData.containsKey(groupId)) {
            groupData = groupSummaryData.get(groupId);
        } else {
            groupData = new GroupPaymentSummaryInfoData(groupType, groupId, groupName, startDate, endDate, dailySplit, productSplit);
            groupSummaryData.put(groupId, groupData);
        }

        groupData.append(order);
    }

}
