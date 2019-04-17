/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.admin.dao.CrmRewardOrderDao;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.service.reward.api.RewardLoader;
import com.voxlearning.utopia.service.reward.entity.RewardLogistics;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.reward.mapper.RewardOrderMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Longlong Yu
 * @since 下午12:15,13-11-22.
 */
@Named
public class CrmRewardService extends AbstractAdminService {

    @Inject
    private CrmRewardOrderDao crmRewardOrderDao;
    @ImportService(interfaceClass = RewardLoader.class)
    private RewardLoader rewardLoader;

    public List<RewardOrderMapper> generateUserRewardOrderMapper(Long userId) {
        List<RewardOrder> orderList = crmRewardOrderDao.loadAllByUserIds(Collections.singleton(userId)).get(userId);
        if (CollectionUtils.isEmpty(orderList)) {
            return Collections.emptyList();
        }
        Set<Long> logisticIds = orderList.stream()
                .map(RewardOrder::getLogisticsId).collect(Collectors.toSet());
        List<RewardOrderMapper> orderMappers = new ArrayList<>();
        for (RewardOrder order : orderList) {
            RewardOrderMapper mapper = new RewardOrderMapper();
            mapper.setId(order.getId());
            mapper.setProductId(order.getProductId());
            mapper.setProductName(order.getProductName());
            mapper.setSkuId(order.getSkuId());
            mapper.setSkuName(order.getSkuName());
            mapper.setQuantity(order.getQuantity());
            mapper.setPrice(order.getPrice());
            mapper.setTotalPrice(order.getTotalPrice());
            mapper.setDiscount(order.getDiscount());
            mapper.setUnit(order.getUnit());
            mapper.setBuyerId(order.getBuyerId());
            mapper.setBuyerName(order.getBuyerName());
            mapper.setStatus(order.getStatus());
            mapper.setSaleGroup(order.getSaleGroup());
            mapper.setCreateDatetime(order.getCreateDatetime());
            mapper.setSource(Optional.ofNullable(order.getSource()).orElse(RewardOrder.Source.pc).name());
            mapper.setDisabled(order.getDisabled() == null ? false : order.getDisabled());
            // 物流信息
            if (order.getLogisticsId() != null && order.getLogisticsId() != 0) {
                RewardLogistics logistics = rewardLoader.loadRewardLogistics(order.getLogisticsId());
                if (logistics != null) {
                    mapper.setLogisticsId(order.getLogisticsId());
                    mapper.setLogisticNo(logistics.getLogisticNo());
                    mapper.setReceiverName(logistics.getReceiverName());
                    mapper.setReceiverId(logistics.getReceiverId());
                    mapper.setCompanyName(logistics.getCompanyName());
                }

            }
            orderMappers.add(mapper);
        }
        return orderMappers;
    }

}
