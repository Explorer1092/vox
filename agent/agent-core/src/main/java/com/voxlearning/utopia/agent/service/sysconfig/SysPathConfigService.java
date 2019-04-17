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

package com.voxlearning.utopia.agent.service.sysconfig;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.persist.AgentSysPathPersistence;
import com.voxlearning.utopia.agent.persist.AgentSysPathRolePersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentSysPath;
import com.voxlearning.utopia.agent.persist.entity.AgentSysPathRole;
import com.voxlearning.utopia.agent.persist.internal.InternalAuthDataLoader;
import com.voxlearning.utopia.agent.service.AbstractAgentService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * AGENT_SYS_PATH and AGENT_SYS_PATH_ROLE Maintenance Service.
 */
@Named
public class SysPathConfigService extends AbstractAgentService {

    @Inject private AgentSysPathPersistence agentSysPathPersistence;
    @Inject private AgentSysPathRolePersistence agentSysPathRolePersistence;
    @Inject private InternalAuthDataLoader internalAuthDataLoader;

    public AgentSysPath getAgentSysPathById(Long id) {
        AgentSysPath agentSysPath = agentSysPathPersistence.load(id);
        agentSysPath.setAuthRoleList(agentSysPathRolePersistence.findByPathId(agentSysPath.getId()));
        return agentSysPath;
    }

    public List<AgentSysPath> getAllSysPathList() {
        List<AgentSysPath> sysPathList = agentSysPathPersistence.findAll();
        for (AgentSysPath sysPath : sysPathList) {
            sysPath.setAuthRoleList(agentSysPathRolePersistence.findByPathId(sysPath.getId()));
        }
        return sysPathList;
    }

    public boolean sysPathExist(String appName, String pathName) {
        if (StringUtils.isEmpty(appName) || StringUtils.isEmpty(pathName)) {
            return false;
        }

        return (getByName(appName, pathName) != null);
    }

    public Long addSysPath(final String appName, final String pathName, final String description, final List<Integer> authRoleList) {
        if (StringUtils.isEmpty(appName) || StringUtils.isEmpty(pathName) || sysPathExist(appName, pathName)) {
            return 0L;
        }

        AgentSysPath sysPath = new AgentSysPath();
        sysPath.setAppName(StringUtils.trim(appName));
        sysPath.setPathName(StringUtils.trim(pathName));
        if (!StringUtils.isEmpty(description)) {
            sysPath.setDescription(description);
        }
        agentSysPathPersistence.insert(sysPath);
        Long pathId = sysPath.getId();

        if (authRoleList == null || authRoleList.size() == 0) {
            return pathId;
        }

        for (Integer roleId : authRoleList) {
            AgentSysPathRole sysPathRole = new AgentSysPathRole();
            sysPathRole.setPathId(pathId);
            sysPathRole.setRoleId(roleId);
            agentSysPathRolePersistence.insert(sysPathRole);
        }

        return pathId;
    }

    public boolean updateSysPath(final Long pathId, final String appName, final String pathName,
                                 final String description, final List<Integer> authRoleList) {
        if (pathId <= 0 || StringUtils.isEmpty(appName) || StringUtils.isEmpty(pathName)) {
            return false;
        }

        AgentSysPath sysPath = agentSysPathPersistence.load(pathId);
        sysPath.setAppName(StringUtils.trim(appName));
        sysPath.setPathName(StringUtils.trim(pathName));
        if (!StringUtils.isEmpty(description)) {
            sysPath.setDescription(description);
        }
        sysPath.setId(pathId);
        agentSysPathPersistence.replace(sysPath);

        if (authRoleList == null || authRoleList.size() == 0) {
            return true;
        }

        List<AgentSysPathRole> sysPathRoleList = agentSysPathRolePersistence.findByPathId(pathId);
        for (AgentSysPathRole sysPathRole : sysPathRoleList) {
            agentSysPathRolePersistence.delete(sysPathRole.getId());
        }

        for (Integer roleId : authRoleList) {
            AgentSysPathRole sysPathRole = new AgentSysPathRole();
            sysPathRole.setPathId(pathId);
            sysPathRole.setRoleId(roleId);
            agentSysPathRolePersistence.insert(sysPathRole);
        }

        return true;
    }

    public boolean deleteSysPath(Long pathId) {
        if (pathId < 0) {
            return false;
        }

        agentSysPathPersistence.delete(pathId);

        List<Integer> roleIds = new ArrayList<>();
        List<AgentSysPathRole> sysPathRoleList = agentSysPathRolePersistence.findByPathId(pathId);
        for (AgentSysPathRole sysPathRole : sysPathRoleList) {
            agentSysPathRolePersistence.delete(sysPathRole.getId());
            roleIds.add(sysPathRole.getRoleId());
        }

        return true;
    }

    public AgentSysPath getByName(String appName, String pathName) {
        return agentSysPathPersistence.findAll().stream()
                .filter(e -> StringUtils.equals(appName, e.getAppName()))
                .filter(e -> StringUtils.equals(pathName, e.getPathName()))
                .findFirst()
                .orElse(null);
    }
}
