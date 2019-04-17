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

package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Shuai.Huan on 2014/7/14.
 */
@Named
@CacheBean(type = AgentOrder.class)
public class AgentOrderPersistence extends AlpsStaticJdbcDao<AgentOrder, Long> {

    @Override
    protected void calculateCacheDimensions(AgentOrder document, Collection<String> dimensions) {
        dimensions.add(AgentOrder.ck_id(document.getId()));
        dimensions.add(AgentOrder.ck_creator(document.getRealCreator()));
        dimensions.add(AgentOrder.ck_wid(document.getWorkflowId()));
        dimensions.add(AgentOrder.ck_platform_uid(document.getUserPlatform(), document.getAccount()));
    }

    public int updateAgentOrderInvoiceId(Long orderId) {
        AgentOrder oldOrder = load(orderId);
        Update update = Update.update("INVOICE_ID", null);
        update.set("ORDER_STATUS", 4);
        Criteria criteria = Criteria.where("ID").is(orderId);
        Integer lows = (int) $update(update, criteria);
        if (lows > 0) {
            evictDocumentCache(oldOrder);
        }
        return lows;
    }


    @UtopiaCacheable
    public List<AgentOrder> findByCreator(@UtopiaCacheKey(name = "creator") Long creator) {
        Criteria criteria = Criteria.where("CREATOR").is(creator.toString());
        Sort sort = new Sort(Sort.Direction.DESC, "ORDER_TIME");
        return query(Query.query(criteria).with(sort));
    }

//    @UtopiaCacheable
//    public AgentOrder findByNote(@UtopiaCacheKey(name = "notes") String notes) {
//        Criteria criteria = Criteria.where("ORDER_NOTES").is(notes)
//                .and("ORDER_STATUS").ne(99);
//        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
//    }

    public boolean updateCrmUserInfo(final Long orderId, String creator, String latestProcessor) {
        AgentOrder agentOrder = new AgentOrder();
        agentOrder.setId(orderId);
        agentOrder.setRealCreator(creator);
        agentOrder.setRealLatestProcessor(latestProcessor);
        AgentOrder modified = replace(agentOrder);
        return modified != null;
    }

    public List<AgentOrder> findByStatus(AgentOrderStatus status){
        Criteria criteria = Criteria.where("ORDER_STATUS").is(status.getStatus());
        return query(Query.query(criteria));
    }

    public List<AgentOrder> findByInvoiceId(Long invoiceId){
        Criteria criteria = Criteria.where("INVOICE_ID").is(invoiceId);
        return query(Query.query(criteria));
    }

    public List<AgentOrder> findByOrderTime(AgentOrderStatus status, Date startDate, Date endDate){
        Criteria criteria = new Criteria();
        criteria.and("ORDER_STATUS").is(status.getStatus());

        if(startDate != null || endDate != null){
            criteria.and("ORDER_TIME");
            if(startDate != null){
                criteria.gte(startDate);
            }
            if(endDate != null){
                criteria.lt(endDate);
            }
        }
        Sort sort = new Sort(Sort.Direction.DESC, "ORDER_TIME");
        return query(Query.query(criteria).with(sort));
    }


    public List<AgentOrder> findByUserIdAndStatus(String userId, ApplyStatus status){
        Criteria criteria = Criteria.where("ACCOUNT").is(userId).and("STATUS").is(status);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public AgentOrder findByWorkflowId(@CacheParameter("wid")Long workflowId){
        Criteria criteria = Criteria.where("WORKFLOW_ID").is(workflowId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public List<AgentOrder> findByUserAndStatus(@CacheParameter("platform")SystemPlatformType userPlatform, @CacheParameter("uid")String userAccount, ApplyStatus status){
        Criteria criteria = Criteria.where("USER_PLATFORM").is(userPlatform).and("ACCOUNT").is(userAccount).and("STATUS").is(status);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<AgentOrder> findByUser(@CacheParameter("platform") SystemPlatformType userPlatform, @CacheParameter("uid") String userAccount) {
        Criteria criteria = Criteria.where("USER_PLATFORM").is(userPlatform).and("ACCOUNT").is(userAccount);
        return query(Query.query(criteria));
    }

    public List<AgentOrder> findByCreateTime(Date startDate, Date endDate) {
        Criteria criteria = new Criteria();
        criteria.and("ORDER_STATUS").ne(AgentOrderStatus.DRAFT.getStatus());
        if (startDate != null || endDate != null) {
            criteria.and("ORDER_TIME");
            if (startDate != null) {
                criteria.gte(startDate);
            }
            if (endDate != null) {
                criteria.lt(endDate);
            }
        }
        return query(Query.query(criteria));
    }

    /**
     * 按照条件查询
     * @param startDate
     * @param endDate
     * @param userIds
     * @param orderId
     * @param applyStatus
     * @return
     */
    public List<AgentOrder> loads(Date startDate, Date endDate,Collection<Long> userIds,Long orderId,ApplyStatus applyStatus){
        Criteria criteria = Criteria.where("USER_PLATFORM").is(SystemPlatformType.AGENT);
        criteria.and("ORDER_STATUS").ne(AgentOrderStatus.DRAFT.getStatus());
        if (startDate != null || endDate != null) {
            criteria.and("ORDER_TIME");
            if (startDate != null) {
                criteria.gte(startDate);
            }
            if (endDate != null) {
                criteria.lt(endDate);
            }
        }
        if (null != orderId){
            criteria.and("ID").is(orderId);
        }
        if (CollectionUtils.isNotEmpty(userIds)){
            criteria.and("ACCOUNT").in(userIds);
        }
        if (null != applyStatus){
            criteria.and("STATUS").is(applyStatus);
        }
        return query(Query.query(criteria));
    }

}
