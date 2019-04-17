/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.reward.base.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.reward.api.RewardLoader;
import com.voxlearning.utopia.service.reward.entity.RewardLogistics;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.reward.mapper.RewardOrderMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/7/28.
 */
public class RewardOrderMapperGenerator {

    private final RewardLoader rewardLoader;
    private final RewardOrderLoader rewardOrderLoader;

    public RewardOrderMapperGenerator(RewardLoader rewardLoader,
                                      RewardOrderLoader rewardOrderLoader) {
        this.rewardLoader = Objects.requireNonNull(rewardLoader);
        this.rewardOrderLoader = Objects.requireNonNull(rewardOrderLoader);
    }

    public List<RewardOrderMapper> generateUserRewardOrderMapper(Long userId) {
        List<RewardOrder> orderList = rewardOrderLoader.loadUserRewardOrders(userId);
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
