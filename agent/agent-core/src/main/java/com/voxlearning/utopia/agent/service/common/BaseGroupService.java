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

import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentGroupUserServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * group base service
 * <p>
 * Created by Alex on 14-8-4.
 */
@Named
public class BaseGroupService extends AbstractAgentService {

    @Inject
    private AgentGroupLoaderClient agentGroupLoaderClient;
    @Inject
    private AgentGroupUserLoaderClient agentGroupUserLoaderClient;
    @Inject
    private AgentGroupUserServiceClient agentGroupUserServiceClient;


    //AgentGroup 基础方法
    public AgentGroup getById(Long groupId) {
        AgentGroup group = agentGroupLoaderClient.load(groupId);
        return (group != null && !group.isDisabledTrue()) ? group : null;
    }

    public List<AgentGroup> getAgentGroupByRoleId(Integer roleId) {
        return agentGroupLoaderClient.findByRoleId(roleId);
    }

    public Map<Long, List<AgentGroupUser>> getGroupUsersByUserIds(Collection<Long> userIds) {
        try {
            return agentGroupUserLoaderClient.findByUserIds(userIds);
        } catch (Throwable e) {
            logger.error("getComposedAgentGroupById-baseUserService.getById Excp : {}; userId = {}", e, userIds);
        }
        return Collections.emptyMap();
    }

    public List<AgentGroupUser> getGroupUsersByGroupId(Long groupId) {
        return agentGroupUserLoaderClient.findByGroupId(groupId);
    }


    public void saveGroupUser(AgentGroupUser agentGroupUser) {
        agentGroupUserServiceClient.persist(agentGroupUser);
    }



}
