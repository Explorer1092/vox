/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.utopia.agent.service.AbstractAgentService;

import javax.inject.Named;

/**
 * Created by Shuai.Huan on 2014/7/8.
 */
@Named
public class GroupConfigService extends AbstractAgentService {

//    @Inject private AgentNotifyService agentNotifyService;
//    @Inject private BaseGroupService baseGroupService;
//    @Inject private BaseUserService baseUserService;
//    @Inject private UserConfigService userConfigService;
//
//    private Map<String, Map<String, Object>> buildAllGroupTreeMap() {
//        List<AgentGroup> groups = baseGroupService.getAllAgentGroups();
//
//        Map<String, Map<String, Object>> retMap = new HashMap<>();
//        for (AgentGroup group : groups) {
//            // 转换成要使用的HashMap对象
//            Map<String, Object> groupItemMap = new HashMap<>();
//            groupItemMap.put("title", group.getGroupName());
//            groupItemMap.put("key", String.valueOf(group.getId()));
//            groupItemMap.put("type", "group");
//            groupItemMap.put("role", group.fetchRoleType());
//            if (group.getParentId() != 0) {
//                groupItemMap.put("parentId", String.valueOf(group.getParentId()));
//            }
//            groupItemMap.put("children", new ArrayList());
//
//            retMap.put(String.valueOf(group.getId()), groupItemMap);
//        }
//
//        // 第二次循环，根据Id和ParentID构建父子关系
//        for (AgentGroup group : groups) {
//            Long parentId = group.getParentId();
//            if (parentId == 0) {
//                continue;
//            }
//
//            Map<String, Object> parentObj = retMap.get(String.valueOf(parentId));
//            Map<String, Object> childObj = retMap.get(String.valueOf(group.getId()));
//
//            // 如果父节点存在，将此结点加入到父结点的子节点中
//            if (parentObj != null) {
//                List children = (List) parentObj.get("children");
//                if (!children.contains(childObj)) {
//                    children.add(childObj);
//                }
//            }
//        }
//
//        return retMap;
//    }
//
//    /**
//     * 根据用户所在群组的role，在创建新群组时挑选出可供选择的role列表
//     *
//     * @param user 当前用户
//     * @return 可供选择的role列表
//     */
//    public List<AgentRoleType> getAvailableAgentRoleForGroup(AuthCurrentUser user) {
//        List<AgentGroupUser> agentGroupUsers = baseGroupService.getGroupUsersByUserId(user.getUserId());
//        if (agentGroupUsers == null || agentGroupUsers.size() == 0)
//            return Collections.emptyList();
//        int roleId = 10000;
//        for (AgentGroupUser agentGroupUser : agentGroupUsers) {
//            AgentGroup agentGroup = baseGroupService.getById(agentGroupUser.getGroupId());
//            roleId = agentGroup.getRoleId() < roleId ? agentGroup.getRoleId() : roleId;
//        }
//
//        return AgentRoleType.getManageableRoleList(roleId);
//    }
//
//    /**
//     * 添加群组
//     *
//     * @param groupName   群组名称
//     * @param desc        群组描述
//     * @param parentId    父群组id
//     * @param roleId      群组角色
//     * @return 群组id
//     */
//    public Long addSysGroup(String groupName, String desc, Long parentId, Integer roleId, Integer schoolLevel) {
//        if (StringUtils.isEmpty(groupName) || baseGroupService.sysGroupExist(groupName)) {
//            return 0L;
//        }
//
//        AgentGroup agentGroup = new AgentGroup();
//        agentGroup.setGroupName(StringUtils.trim(groupName));
//        if (!StringUtils.isEmpty(desc)) {
//            agentGroup.setDescription(desc);
//        }
//        agentGroup.setParentId(parentId);
//        agentGroup.setRoleId(roleId);
//
//        Long groupId = baseGroupService.saveAgentGroup(agentGroup);
//        return groupId;
//    }
//
//    /**
//     * 更新群组
//     *
//     * @param groupId     群组id
//     * @param groupName   群组名称
//     * @param desc        群组描述
//     * @param parentId    父群组id
//     * @param roleId      群组角色
//     * @param schoolLevel      学校级别
//     * @return 更新成功与否
//     */
//    public boolean updateSysGroup(Long groupId, String groupName, String desc, Long parentId, Integer roleId, Integer schoolLevel) {
//        if (groupId <= 0 || StringUtils.isEmpty(groupName)) {
//            return false;
//        }
//
//        AgentGroup agentGroup = baseGroupService.getById(groupId);
//        agentGroup.setGroupName(StringUtils.trim(groupName));
//        agentGroup.setParentId(parentId);
//        agentGroup.setRoleId(roleId);
//        if (!StringUtils.isEmpty(desc)) {
//            agentGroup.setDescription(desc);
//        }
//
//        baseGroupService.updateAgentGroup(agentGroup);
//
//        return true;
//    }
//
//    /**
//     * 删除群组
//     *
//     * @param groupId 群组id
//     * @return 删除成功标志
//     */
//    public boolean deleteSysGroup(Long groupId) {
//        if (groupId < 0) {
//            return false;
//        }
//
//        baseGroupService.deleteGroup(groupId);
//
//        List<AgentGroupRegion> groupRegions = baseGroupService.getGroupRegionsByGroupId(groupId);
//        if (CollectionUtils.isNotEmpty(groupRegions)) {
//            for (AgentGroupRegion groupRegion : groupRegions) {
//                baseGroupService.deleteGroupRegion(groupRegion.getId());
//            }
//        }
//
//        List<AgentGroupSchool> agentGroupSchools = baseGroupService.getGroupSchoolsByGroupId(groupId);
//        if (CollectionUtils.isNotEmpty(agentGroupSchools)) {
//            for (AgentGroupSchool groupSchool : agentGroupSchools) {
//                baseGroupService.deleteGroupSchool(groupSchool.getId());
//            }
//        }
//
//        List<AgentGroupUser> agentGroupUserList = baseGroupService.getGroupUsersByGroupId(groupId);
//        for (AgentGroupUser agentGroupUser : agentGroupUserList) {
//            baseGroupService.deleteGroupUser(agentGroupUser.getId());
//        }
//
//        return true;
//    }
//
//    public void sendGroupNotify(Long parentGroupId, String content) {
//
//        List<Long> groupIds = baseGroupService.getParentGroupIds(parentGroupId);
//        Set<Long> receivers = new HashSet<>();
//        for (Long groupId : groupIds) {
//            List<AgentGroupUser> agentGroupUsers = baseGroupService.getGroupUsersByGroupId(groupId);
//            if (CollectionUtils.isNotEmpty(agentGroupUsers)) {
//                for (AgentGroupUser agentGroupUser : agentGroupUsers) {
//                    receivers.add(agentGroupUser.getUserId());
//                }
//            }
//        }
//        agentNotifyService.sendNotify(AgentNotifyType.ADD_NEW_GROUP.getType(), content, new ArrayList<>(receivers));
//    }
//
//    //构建父节点信息
//    private void constructPCodeList(Integer parentId, List<String> regionNames) {
//        if (parentId != 0L) {
//            ExRegion region = regionLoaderClient.loadRegion(parentId);
//            if (region == null) return;
//            regionNames.add(region.getName());
//            constructPCodeList(region.getPcode(), regionNames);
//        }
//    }
//
//    /**
//     * 组装Region的Names，例如:北京市/朝阳区/
//     *
//     * @param parentId 父区域code
//     * @return 组装过的区域名称
//     */
//    private String constructRegionName(Integer parentId) {
//        if (parentId == 0) {
//            return "区域未知";
//        }
//
//        StringBuilder result = new StringBuilder();
//        List<String> info = new ArrayList<>();
//        constructPCodeList(parentId, info);
//        Collections.reverse(info);
//        String token = "/";
//        for (String s : info) {
//            result.append(token).append(s);
//        }
//        return result.toString().replaceFirst(token, "");
//    }
//
//    public List<Map<String, Object>> loadGroupRegionTree(AuthCurrentUser user) {
//        Map<String, Map<String, Object>> allGroupTreeMap = buildAllGroupTreeMap();
//        List<AgentGroup> managedGroupList;
//        if (user.isAdmin()) {
//            managedGroupList = baseGroupService.getAllRootGroups();
//        } else {
//            managedGroupList = baseGroupService.getDirectManagedGroups(user.getUserId());
//        }
//
//        List<Map<String, Object>> groupList = new ArrayList<>();
//        for (AgentGroup group : managedGroupList) {
//            MiscUtils.addNonNullElement(groupList, allGroupTreeMap.get(String.valueOf(group.getId())));
//        }
//
//        return groupList;
//    }
//
//    public List<Map<String, Object>> loadUserGroupRegionTree(AuthCurrentUser user, Long userId) {
//        Map<String, Map<String, Object>> allGroupTreeMap = buildAllGroupTreeMap();
//
//        List<AgentGroup> managedGroupList;
//        if (user.isAdmin()) {
//            managedGroupList = baseGroupService.getAllRootGroups();
//        } else {
//            managedGroupList = baseGroupService.getDirectManagedGroups(user.getUserId());
//        }
//
//        List<Map<String, Object>> groupList = new ArrayList<>();
//        for (AgentGroup group : managedGroupList) {
//            MiscUtils.addNonNullElement(groupList, allGroupTreeMap.get(String.valueOf(group.getId())));
//        }
//
//        // 根据用户情况设置是否被选中状态
//        if (userId == 0L) {
//            return groupList;
//        }
//
//        List<AgentGroup> userGroupList = baseUserService.getUserGroupList(userId);
//        if (userGroupList == null || userGroupList.size() == 0) {
//            return groupList;
//        }
//
//        for (AgentGroup userGroup : userGroupList) {
//            Map<String, Object> groupInfo = allGroupTreeMap.get(String.valueOf(userGroup.getId()));
//            groupInfo.put("selected", Boolean.TRUE);
//        }
//
//        return groupList;
//    }
}
