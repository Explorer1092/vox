package com.voxlearning.utopia.agent.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.service.permission.SystemRolePermissionService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AgentUserSupport
 *
 * @author song.wang
 * @date 2018/8/1
 */
@Named
public class AgentUserSupport {

    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private SystemRolePermissionService systemRolePermissionService;
    @Inject
    private AgentCacheSystem agentCacheSystem;
    @Inject
    private AgentNotifyService agentNotifyService;

    public AuthCurrentUser createCurrentUserById(Long userId){

        AgentUser agentUser = baseOrgService.getUser(userId);
        if(agentUser == null){
            return null;
        }
        AuthCurrentUser currentUser = new AuthCurrentUser();
        currentUser.setUserId(userId);
        currentUser.setUserName(agentUser.getAccountName());
        currentUser.setRealName(agentUser.getRealName());
        currentUser.setStatus(agentUser.getStatus());
        currentUser.setUserPhone(agentUser.getTel());
        currentUser.setRoleList(loadUserRoleList(userId));
        currentUser.setDeviceId(agentUser.getDeviceId());

        currentUser.setPageElementCodes(loadUserPageElementCodes(userId));
        currentUser.setOperationCodes(loadUserOperationCodes(userId));
        return currentUser;
    }

    public void refreshCurrentUserData(AuthCurrentUser currentUser){
        if(currentUser == null){
            return;
        }
        Long userId = currentUser.getUserId();
        AgentUser agentUser = baseOrgService.getUser(userId);
        if(agentUser == null){ // 用户不存在或已关闭
            currentUser.setStatus(9);
            return;
        }
        currentUser.setStatus(agentUser.getStatus());
        currentUser.setRoleList(loadUserRoleList(userId));
        currentUser.setPageElementCodes(loadUserPageElementCodes(userId));
        currentUser.setOperationCodes(loadUserOperationCodes(userId));
    }

    public List<Integer> loadUserRoleList(Long userId){
        return baseOrgService.getUserRoleList(userId).stream().map(AgentRoleType::getId).collect(Collectors.toList());
    }

    public List<String> loadUserPageElementCodes(Long userId){
        return systemRolePermissionService.loadUserPageElementCodes(userId);
    }

    public List<String> loadUserOperationCodes(Long userId){
        return systemRolePermissionService.loadUserOperationCodes(userId);
    }

    public List<String> getUserJpushTagList(Long userId){
        List<String> tagList = new ArrayList<>();
        List<AgentGroup> groupList = baseOrgService.getDirectAndParentGroupList(userId);
        if(CollectionUtils.isNotEmpty(groupList)){
            groupList.forEach(p -> tagList.add("agent_group_" + p.getId()));
        }

        Map<Long, Integer> groupRoleMap = baseOrgService.getGroupUserRoleMapByUserId(userId);
        if(MapUtils.isNotEmpty(groupRoleMap)){
            groupRoleMap.values().forEach(p -> {
                if(p != null){
                    tagList.add("agent_role_" + p);
                }
            });
        }
        return tagList;
    }

    public Integer getUnreadNotifyCount(Long userId){
        Integer unreadCount = agentCacheSystem.getUserUnreadNotifyCount(userId);
        if (unreadCount == null) {
            unreadCount = agentNotifyService.getTotalUnreadNotifyCount(null, userId);
            agentCacheSystem.updateUserUnreadNotifyCount(userId, unreadCount);
        }
        return unreadCount;
    }






}
