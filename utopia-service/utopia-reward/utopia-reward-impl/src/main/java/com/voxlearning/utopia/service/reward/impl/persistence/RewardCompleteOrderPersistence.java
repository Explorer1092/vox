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

package com.voxlearning.utopia.service.reward.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.entity.RewardCompleteOrder;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;

import javax.inject.Named;
import java.util.List;

/**
 * Created by XiaoPeng.Yang on 14-8-1.
 */
@Named("com.voxlearning.utopia.service.reward.impl.persistence.RewardCompleteOrderPersistence")
public class RewardCompleteOrderPersistence extends NoCacheStaticMySQLPersistence<RewardCompleteOrder, Long> {

    public int updateRewardCompleteOrderLogisticsId(Long completeOrderId, Long logisticsId) {
        Criteria criteria = Criteria.where("ID").is(completeOrderId);
        Update update = Update.update("LOGISTICS_ID", logisticsId);
        return (int) $update(update, criteria);
    }

    public int updateCompleteOrderStatus(Long id, RewardOrderStatus status) {
        Criteria criteria = Criteria.where("ID").is(id);
        Update update = Update.update("STATUS", status);
        return (int) $update(update, criteria);
    }

    public List<RewardCompleteOrder> findByLogisticsId(Long logisticId){
        Criteria criteria = Criteria.where("LOGISTICS_ID").is(logisticId);
        return query(Query.query(criteria));
    }

    public boolean deleteCompleteOrder(Long id){
        Criteria criteria = Criteria.where("ID").is(id);
        Update update = Update.update("disabled",true);
        return (int)$update(update, criteria) > 0;
    }

}
