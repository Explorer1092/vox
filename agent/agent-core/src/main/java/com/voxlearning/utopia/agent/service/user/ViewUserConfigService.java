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

package com.voxlearning.utopia.agent.service.user;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.Password;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentUserType;
import com.voxlearning.utopia.agent.persist.AgentViewUserPersistence;
import com.voxlearning.utopia.agent.persist.AgentViewUserRegionPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentViewUser;
import com.voxlearning.utopia.agent.persist.entity.AgentViewUserRegion;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseGroupService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by Alex on 14-11-3.
 */
@Named
public class ViewUserConfigService extends AbstractAgentService {

    @Inject private AgentViewUserPersistence agentViewUserPersistence;
    @Inject private AgentViewUserRegionPersistence agentViewUserRegionPersistence;
    @Inject private BaseUserService baseUserService;
    @Inject private AgentRegionService agentRegionService;
    @Inject private UserConfigService userConfigService;
    @Inject private BaseGroupService baseGroupService;

    public List<Map<String, Object>> getManagedViewUserList(Long parentUserId) {
        List<AgentViewUser> viewUserList = agentViewUserPersistence.findByParentUserId(parentUserId);
        if (viewUserList == null || viewUserList.size() == 0) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> retUserList = new ArrayList<>();
        for (AgentViewUser viewUser : viewUserList) {
            Long userId = viewUser.getUserId();
            AgentUser user = baseUserService.getById(userId);
            if (user == null) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", user.getId());
            item.put("accountName", user.getAccountName());
            item.put("realName", user.getRealName());
            item.put("userComment", user.getUserComment());

            List<AgentViewUserRegion> viewRegionList = agentViewUserRegionPersistence.findByUserId(userId);
            List<String> regionList = new ArrayList<>();
            if (viewRegionList != null && viewRegionList.size() > 0) {
                for (AgentViewUserRegion viewRegion : viewRegionList) {
                    regionList.add(viewRegion.getRegionName());
                }
            }
            item.put("viewRegions", regionList);
            retUserList.add(item);
        }

        return retUserList;
    }

    public List<Map<String, Object>> loadViewUserRegions(AuthCurrentUser user, Long userId) {
        List<Map<String, Object>> allRegionTree = agentRegionService.loadUserRegionTree(user, false);

        List<AgentViewUserRegion> viewUserRegions = agentViewUserRegionPersistence.findByUserId(userId);
        List<String> selectedRegions = new ArrayList<>();
        for (AgentViewUserRegion viewUserRegion : viewUserRegions) {
            selectedRegions.add(String.valueOf(viewUserRegion.getRegionCode()));
        }

        agentRegionService.markSelectedRegion(allRegionTree, selectedRegions);
        return allRegionTree;
    }

    public Long addViewUser(String accountName, String realName, String password, String tel, String email, String imAccount, String address,
                            String userComment, List<Integer> regions, Long parentUserId) {
        if (StringUtils.isEmpty(accountName) || StringUtils.isEmpty(realName) || StringUtils.isEmpty(password)
                || baseUserService.getByAccountName(accountName) != null) {
            return 0L;
        }

        // 保存AgentUser
        AgentUser agentUser = new AgentUser();
        agentUser.setAccountName(accountName);
        agentUser.setRealName(realName);
        if (!StringUtils.isEmpty(userComment)) {
            agentUser.setUserComment(userComment);
        }
        Password passWd = userConfigService.encryptPassword(password);
        agentUser.setPasswd(passWd.getPassword());
        agentUser.setPasswdSalt(passWd.getSalt());
        agentUser.setStatus(AgentUserType.INITIAL.getStatus());
        agentUser.setCashAmount(0f);
        agentUser.setPointAmount(0f);
        agentUser.setUsableCashAmount(0f);
        agentUser.setUsablePointAmount(0f);
        agentUser.setTel(tel);
        agentUser.setEmail(email);
        agentUser.setImAccount(imAccount);
        agentUser.setAddress(address);
        Long userId = baseUserService.createAgentUser(agentUser);

        // 保存AgentViewUser
        AgentViewUser viewUser = new AgentViewUser();
        viewUser.setUserId(userId);
        viewUser.setParentUserId(parentUserId);
        agentViewUserPersistence.insert(viewUser);

        // 保存AgentViewUserRegion
        if (CollectionUtils.isNotEmpty(regions)) {
            for (Integer regionCode : regions) {
                if (regionCode == null) continue;
                AgentViewUserRegion userRegion = new AgentViewUserRegion();
                userRegion.setUserId(userId);
                userRegion.setRegionCode(regionCode);
                String regionName = agentRegionService.getRegionName(regionCode);
                userRegion.setRegionName(regionName);
                agentViewUserRegionPersistence.insert(userRegion);
            }
        }

        // 保存用户到协作组里面
        List<AgentGroup> dataViewerList = baseGroupService.getAgentGroupByRoleId(AgentRoleType.DataViewer.getId());
        if (dataViewerList != null && dataViewerList.size() > 0) {
            AgentGroup group = dataViewerList.get(0);
            AgentGroupUser groupUser = new AgentGroupUser();
            groupUser.setGroupId(group.getId());
            groupUser.setUserId(userId);
            baseGroupService.saveGroupUser(groupUser);
        }

        return userId;
    }

    public boolean updateViewUser(final Long userId, final String realName, final String userComment,
                                  final String tel, final String email, final String imAccount, final String address,
                                  final List<Integer> regions) {
        if (userId <= 0) {
            return false;
        }

        AgentUser agentUser = new AgentUser();
        agentUser.setId(userId);
        agentUser.setRealName(StringUtils.trim(realName));
        if (!StringUtils.isEmpty(userComment)) {
            agentUser.setUserComment(userComment);
        }
        agentUser.setTel(tel);
        agentUser.setEmail(email);
        agentUser.setImAccount(imAccount);
        agentUser.setAddress(address);
        baseUserService.updateAgentUser(agentUser);

        //更新AgentViewUserRegion
        List<AgentViewUserRegion> userRegions = agentViewUserRegionPersistence.findByUserId(userId);
        for (AgentViewUserRegion userRegion : userRegions) {
            agentViewUserRegionPersistence.delete(userRegion.getId());
        }

        // 保存AgentViewUserRegion
        if (CollectionUtils.isNotEmpty(regions)) {
            for (Integer regionCode : regions) {
                if (regionCode == null) continue;
                AgentViewUserRegion userRegion = new AgentViewUserRegion();
                userRegion.setUserId(userId);
                userRegion.setRegionCode(regionCode);
                String regionName = agentRegionService.getRegionName(regionCode);
                userRegion.setRegionName(regionName);
                agentViewUserRegionPersistence.insert(userRegion);
            }
        }

        return true;
    }

    public boolean deleteViewUser(Long userId) {
        if (userId < 0) {
            return false;
        }

        AgentUser agentUser = baseUserService.getById(userId);
        if (agentUser == null) {
            return false;
        }

        agentUser.setStatus(9);
        baseUserService.updateAgentUser(agentUser);

        return true;
    }
}
