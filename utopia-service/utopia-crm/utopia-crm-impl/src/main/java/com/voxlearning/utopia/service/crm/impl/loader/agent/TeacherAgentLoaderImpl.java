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

package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.crm.api.loader.agent.TeacherAgentLoader;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by alex on 2017/1/9.
 */
@Named
@Service(interfaceClass = TeacherAgentLoader.class)
@ExposeService(interfaceClass = TeacherAgentLoader.class)
public class TeacherAgentLoaderImpl extends SpringContainerSupport implements TeacherAgentLoader {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;
    @Inject private AgentUserSchoolLoaderImpl agentUserSchoolLoader;
    @Inject private AgentGroupUserLoaderImpl agentGroupUserLoader;
    @Inject private AgentUserLoaderImpl agentUserLoader;
    @Inject private AgentGroupSchoolLoaderImpl agentGroupSchoolLoader;

    @Override
    public Map<String, Object> getSchoolManager(Long schoolId) {
        if (schoolId == null || Objects.equals(schoolId, 0L)) {
            return Collections.emptyMap();
        }

        School school = raikouSystem.loadSchoolIncludeDisabled(schoolId);
        if (school == null) {
            return Collections.emptyMap();
        }

        // 查找学校的负责专员信息,只查找第一个专员
        List<AgentUserSchool> userSchoolList = agentUserSchoolLoader.findBySchoolId(schoolId);
        for (AgentUserSchool userSchool : userSchoolList) {
            List<AgentGroupUser> groupUsers = agentGroupUserLoader.findByUserId(userSchool.getUserId());
            groupUsers = groupUsers.stream()
                    .filter(p -> AgentRoleType.BusinessDeveloper == p.getUserRoleType())
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(groupUsers)) {
                continue;
            }

            AgentUser agentUser = agentUserLoader.load(userSchool.getUserId());
            if (!agentUser.isValidUser()) {
                continue;
            }

            Map<String, Object> userInfo = new LinkedHashMap<>();
            userInfo.put("userId", agentUser.getId());
            userInfo.put("userName", agentUser.getRealName());
            userInfo.put("mobile", agentUser.getTel());
            userInfo.put("avatar", agentUser.getAvatar());
            return userInfo;
        }

        // 查找负责的市经理
        AgentGroupSchool groupSchool = agentGroupSchoolLoader.findBySchoolId(schoolId);
        if (groupSchool != null) {
            List<AgentGroupUser> groupUsers = agentGroupUserLoader.findByGroupId(groupSchool.getGroupId());
            groupUsers = groupUsers.stream()
                    .filter(p -> AgentRoleType.CityManager == p.getUserRoleType())
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(groupUsers)) {
                return Collections.emptyMap();
            }

            for (AgentGroupUser groupUser : groupUsers) {
                AgentUser agentUser = agentUserLoader.load(groupUser.getUserId());
                if (agentUser == null) {
                    continue;
                }

                Map<String, Object> userInfo = new LinkedHashMap<>();
                userInfo.put("userId", agentUser.getId());
                userInfo.put("userName", agentUser.getRealName());
                userInfo.put("mobile", agentUser.getTel());
                userInfo.put("avatar", agentUser.getAvatar());

                return userInfo;
            }
        }

        return Collections.emptyMap();
    }
}
