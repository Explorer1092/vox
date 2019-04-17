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

package com.voxlearning.utopia.agent.persist.internal;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.persist.AgentSysPathPersistence;
import com.voxlearning.utopia.agent.persist.AgentSysPathRolePersistence;
import com.voxlearning.utopia.agent.persist.SystemPageElementPersistence;
import com.voxlearning.utopia.agent.persist.SystemRolePageElementPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentSysPath;
import com.voxlearning.utopia.agent.persist.entity.AgentSysPathRole;
import com.voxlearning.utopia.agent.persist.entity.permission.SystemPageElement;
import com.voxlearning.utopia.agent.persist.entity.permission.SystemRolePageElement;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.permission.SystemRolePermissionService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * Created by Alex on 14-7-4.
 */
@Named
public class InternalAuthDataLoader extends SpringContainerSupport {

    @Inject private AgentSysPathPersistence agentSysPathPersistence;
    @Inject private AgentSysPathRolePersistence agentSysPathRolePersistence;

    @Inject private BaseOrgService baseOrgService;
    @Inject private SystemRolePermissionService systemRolePermissionService;


    /**
     * 获取用户权限列表
     */
    public List<String> loadUserAuthPathList(Long userId) {
        Set<String> existUserAuthPathList = new HashSet<>();
        List<Integer> roleList = loadUserRoleList(userId);
        for (Integer userRole  : roleList) {

            List<AgentSysPathRole> pathRoleList = agentSysPathRolePersistence.findByRoleId(userRole);
            if(CollectionUtils.isNotEmpty(pathRoleList)){
                for (AgentSysPathRole pathRole : pathRoleList) {
                    AgentSysPath agentSysPath = agentSysPathPersistence.load(pathRole.getPathId());
                    existUserAuthPathList.add(agentSysPath.getAppName() + "/" + agentSysPath.getPathName() + "/");
                }
            }
        }
        return new ArrayList<>(existUserAuthPathList);
    }

    /**
     * 获取用户所在部门权限列表
     */
    public List<Integer> loadUserRoleList(Long userId) {
        List<Integer> existUserRoleList = new ArrayList<>();

        Map<Long, Integer> userRoleMap = baseOrgService.getGroupUserRoleMapByUserId(userId);
        if(MapUtils.isNotEmpty(userRoleMap)){
            existUserRoleList.addAll(userRoleMap.values().stream().collect(Collectors.toSet()));
        }

        return existUserRoleList;
    }

    public List<String> loadUserPageElementCodes(Long userId){
        return systemRolePermissionService.loadUserPageElementCodes(userId);
    }

    public List<String> loadUserOperationCodes(Long userId){
        return systemRolePermissionService.loadUserOperationCodes(userId);
    }


}
