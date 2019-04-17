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

package com.voxlearning.utopia.agent.service.common;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.dao.mongo.AgentUserCashRecordDao;
import com.voxlearning.utopia.agent.dao.mongo.AgentUserMaterialBudgetRecordDao;
import com.voxlearning.utopia.agent.persist.AgentUserAccountHistoryPersistence;
import com.voxlearning.utopia.agent.persist.AgentViewUserRegionPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentUserCashRecord;
import com.voxlearning.utopia.agent.persist.entity.AgentUserMaterialBudgetRecord;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentUserServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * User related service class.
 * <p>
 * Created by Shuai.Huan on 2014/7/8.
 */
@Named
public class BaseUserService extends AbstractAgentService {

    @Inject
    AgentUserLoaderClient agentUserLoaderClient;
    @Inject
    AgentUserServiceClient agentUserServiceClient;
    @Inject AgentUserAccountHistoryPersistence agentUserAccountHistoryPersistence;
    @Inject AgentRegionService agentRegionService;
    @Inject BaseGroupService baseGroupService;
    @Inject AgentViewUserRegionPersistence agentViewUserRegionPersistence;
    @Inject
    AgentUserMaterialBudgetRecordDao agentUserMaterialBudgetRecordDao;
    @Inject
    AgentUserCashRecordDao agentUserCashRecordDao;

    public AgentUser getUser(Long userId) {
        return getById(userId);
    }

    public Map<Long, AgentUser> getUsers(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        return agentUserLoaderClient.findByIds(userIds);
    }

    public AgentUser getById(Long userId) {
        if (userId == null || userId < 0) {
            return null;
        }
        AgentUser agentUser = agentUserLoaderClient.load(userId);
        return (agentUser != null && agentUser.isValidUser()) ? agentUser : null;
    }

    public Map<String, AgentUser> getAllAgentUsers() {
        Map<String, AgentUser> all = new HashMap<>();
        List<AgentUser> agentUsers = agentUserLoaderClient.findAll();
        for (AgentUser agentUser : agentUsers) {
            if (agentUser.isValidUser()) {
                all.put(String.valueOf(agentUser.getId()), agentUser);
            }
        }
        return all;
    }


    public AgentUser getByAccountName(String accountName) {
        if (StringUtils.isEmpty(accountName)) {
            return null;
        }
        AgentUser user = agentUserLoaderClient.findByName(accountName);
        return (user != null && user.isValidUser()) ? user : null;
    }

    public AgentUser getByMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return null;
        }
        return agentUserLoaderClient.findByMobile(mobile);
    }

    public Long createAgentUser(AgentUser agentUser) {
        if (agentUser == null) {
            return 0L;
        }
        return agentUserServiceClient.persist(agentUser);
    }

    public void updateAgentUser(AgentUser agentUser) {
        if (agentUser == null) {
            return;
        }
        agentUserServiceClient.update(agentUser.getId(), agentUser);
    }


    // 添加物料预算变动记录， 或者 添加余额变动记录  type=1:物料预算变动记录  type=2:余额变动记录
    public void addAgentUserCashDataRecord(Integer type, Long userId, Long operatorId, Float preCash, Float afterCash, Float quantity, String comment){
        AgentUser user = getById(userId);
        if(user == null){
            return;
        }

        AgentUser operator = getById(operatorId);
        if(operator == null){
            return;
        }
        if(type == 1){
            AgentUserMaterialBudgetRecord record = new AgentUserMaterialBudgetRecord();
            record.setUserId(user.getId());
            record.setUserName(user.getRealName());
            record.setOperatorId(operator.getId());
            record.setOperatorName(operator.getRealName());
            record.setPreCash(preCash);
            record.setAfterCash(afterCash);
            record.setQuantity(quantity);
            record.setComment(comment);
            agentUserMaterialBudgetRecordDao.insert(record);
        }else if(type == 2){
            AgentUserCashRecord record = new AgentUserCashRecord();
            record.setUserId(user.getId());
            record.setUserName(user.getRealName());
            record.setOperatorId(operator.getId());
            record.setOperatorName(operator.getRealName());
            record.setPreCash(preCash);
            record.setAfterCash(afterCash);
            record.setQuantity(quantity);
            record.setComment(comment);
            agentUserCashRecordDao.insert(record);
        }
    }
}
