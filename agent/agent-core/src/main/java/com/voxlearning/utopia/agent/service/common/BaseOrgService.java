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

package com.voxlearning.utopia.agent.service.common;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.group.GroupData;
import com.voxlearning.utopia.agent.bean.hierarchicalstructure.NodeStructure;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.utils.NodeStructureUtil;
import com.voxlearning.utopia.agent.utils.Pinyin4jUtils;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRegionType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.*;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentGroupServiceClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentUserSchoolServiceClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * BaseOrgService
 *
 * @author song.wang
 * @since 2016/6/15
 */
@Named
public class BaseOrgService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AgentGroupLoaderClient agentGroupLoaderClient;
    @Inject private AgentGroupServiceClient agentGroupServiceClient;
    @Inject private AgentGroupRegionLoaderClient agentGroupRegionLoaderClient;
    @Inject private AgentGroupUserLoaderClient agentGroupUserLoaderClient;
    @Inject private AgentUserLoaderClient agentUserLoaderClient;
    @Inject private AgentUserSchoolLoaderClient agentUserSchoolLoaderClient;
    @Inject private AgentRegionService agentRegionService;

    @Inject private AgentDictSchoolService agentDictSchoolService;

    @Inject private AgentUserSchoolServiceClient agentGroupSchooServiceClient;
    @Inject private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;
    @Inject private BaseDictService baseDictService;

    public Boolean updateUserSchool(Long userSchoolId, AgentUserSchool userSchool) {
        return agentGroupSchooServiceClient.update(userSchoolId, userSchool);
    }

    public Set<Long> loadGroupUserByGroupId(Long groupId, AgentRoleType roleType) {
        Set<Long> result;
        List<AgentGroupUser> groupUserByGroup = getGroupUserByGroup(groupId);
        result = groupUserByGroup.stream().filter(p -> roleType == null || Objects.equals(p.getUserRoleType(), roleType)).map(AgentGroupUser::getUserId).collect(toSet());
        if (CollectionUtils.isEmpty(result)) {
            List<AgentGroup> groupListByParentId = getGroupListByParentId(groupId);
            if (CollectionUtils.isNotEmpty(groupListByParentId)) {
                groupListByParentId.forEach(p -> result.addAll(loadGroupUserByGroupId(p.getId(), roleType)));
            }
        }
        return result;
    }

    public List<Long> loadBusinessSchoolByUserId(Long userId) {
        AgentRoleType userRole = getUserRole(userId);
        List<Long> managedSchools = getManagedSchoolList(userId);
        if (userRole == AgentRoleType.CityManager) {
            List<AgentGroupUser> groupUser = getGroupUserByUser(userId);
            if (CollectionUtils.isEmpty(groupUser)) {
                return Collections.emptyList();
            }
            List<Long> bdIds = getAllSubGroupUserIdsByGroupIdAndRole(groupUser.get(0).getGroupId(), AgentRoleType.BusinessDeveloper.getId());
            Set<Long> schoolIds = new HashSet<>();
            bdIds.forEach(p -> schoolIds.addAll(getManagedSchoolList(p)));
            managedSchools = managedSchools.stream().filter(p -> !schoolIds.contains(p)).collect(toList());
        }
        return managedSchools;
    }


    public List<AgentGroup> getRootAgentGroups() {
        return agentGroupLoaderClient.findByParentId(0L);
    }


    // 层级结构
    public MapMessage loadRegionTree(Long userId, Integer level, Collection<Integer> isSelectedId) {
        AgentRoleType roleType = getUserRole(userId);
        if (roleType == null) {
            return MapMessage.errorMessage("用户角色不存在");
        }
        if (AgentRegionType.typeOf(level) == null) {
            return MapMessage.errorMessage("用户所选级别错误");
        }

        if (roleType == AgentRoleType.Country || roleType == AgentRoleType.BUManager || roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager
                || roleType == AgentRoleType.CityManager || roleType == AgentRoleType.BusinessDeveloper) {
            Collection<ExRegion> counties = getCountyRegion(userId, roleType);
            if (CollectionUtils.isEmpty(counties)) {
                return MapMessage.errorMessage("该用户下无地区");
            }
            return createRegionTree(counties, level, isSelectedId);
        }
        return MapMessage.errorMessage("用户角色无对应的地区");
    }

    /**
     * 不限制角色获取负责区域层级结构
     *
     * @param userId
     * @param level
     * @param isSelectedId
     * @return
     */
    public MapMessage loadRegionTreeWiderRole(Long userId, Integer level, Collection<Integer> isSelectedId) {
        AgentRoleType roleType = getUserRole(userId);
        if (roleType == null) {
            return MapMessage.errorMessage("用户角色不存在");
        }
        if (AgentRegionType.typeOf(level) == null) {
            return MapMessage.errorMessage("用户所选级别错误");
        }

        Collection<ExRegion> counties = getCountyRegion(userId, roleType);
        if (CollectionUtils.isEmpty(counties)) {
            return MapMessage.errorMessage("该用户下无地区");
        }
        return createRegionTree(counties, level, isSelectedId);
    }

    public AgentGroup getGroupFirstOne(Long userId, AgentRoleType role) {
        List<AgentGroupUser> groupUsers = getGroupUserByUser(userId).stream().filter(p -> role == null || p.getUserRoleType() == role).collect(Collectors.toList());
        Set<Long> groupIds = groupUsers.stream().map(AgentGroupUser::getGroupId).collect(Collectors.toSet());
        List<AgentGroup> groups = getGroupByIds(groupIds);
        if (CollectionUtils.isEmpty(groups)) {
            return null;
        }
        return groups.get(0);
    }

    public Collection<ExRegion> getCountyRegion(Long userId, AgentRoleType roleType) {
        AgentGroup group = getGroupFirstOne(userId, roleType);
        if (group == null) {
            return Collections.emptyList();
        }
        List<AgentGroupRegion> groupRegionList = new ArrayList<>();
        if (roleType == AgentRoleType.Country) {
            List<AgentGroup> agentGroups = getGroupListByParentId(group.getId());
            List<AgentGroupRegion> tmpGroupRegionList = getGroupRegionsByGroupSet(agentGroups.stream().map(AgentGroup::getId).collect(Collectors.toSet()));
            if (CollectionUtils.isNotEmpty(tmpGroupRegionList)) {
                groupRegionList.addAll(tmpGroupRegionList);
            }
        } else {
            List<AgentGroupRegion> tmpGroupRegionList = getGroupRegionByGroup(group.getId());
            if (CollectionUtils.isNotEmpty(tmpGroupRegionList)) {
                groupRegionList.addAll(tmpGroupRegionList);
            }
        }

        Set<Integer> regionList = groupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet());
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionList);


        return getExRegionMap(exRegionMap);
    }

    private Collection<ExRegion> getExRegionMap(Map<Integer, ExRegion> exRegionMap) {
        Map<Integer, ExRegion> resultRegionMap = new HashMap<>();
        exRegionMap.forEach((k, v) -> {
            if (v.fetchRegionType() == RegionType.COUNTY) {
                resultRegionMap.put(k, v);
            } else if (v.fetchRegionType() == RegionType.CITY) {
                List<ExRegion> countyRegionList = v.getChildren();
                countyRegionList.forEach(t -> resultRegionMap.put(t.getId(), t));
            } else if (v.fetchRegionType() == RegionType.PROVINCE) {
                List<ExRegion> cityRegionList = v.getChildren();
                cityRegionList.forEach(city -> {
                    List<ExRegion> countyRegionList = city.getChildren();
                    countyRegionList.forEach(t -> resultRegionMap.put(t.getId(), t));
                });
            }
        });
        return resultRegionMap.values();
    }

    public Collection<ExRegion> getCountyRegionByUserId(Long userId, AgentRoleType roleType) {
        AgentGroup group = getGroupFirstOne(userId, roleType);
        if (group == null) {
            return Collections.emptyList();
        }
        return getCountyRegionByGroupId(group.getId());
    }

    public Collection<ExRegion> getCountyRegionByGroupId(Long groupId) {
        AgentGroup group = agentGroupLoaderClient.load(groupId);
        if (group == null) {
            return Collections.emptyList();
        }
        List<AgentGroupRegion> groupRegionList = new ArrayList<>();
        List<AgentGroupRegion> tmpGroupRegionList = getGroupRegionByGroup(group.getId());
        if (CollectionUtils.isNotEmpty(tmpGroupRegionList)) {
            groupRegionList.addAll(tmpGroupRegionList);
        }

        Set<Integer> regionList = groupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet());
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionList);
        return getExRegionMap(exRegionMap);
    }

    /**
     * 获取部门负责区域地址信息
     *
     * @param userId
     * @param roleType
     * @param type
     * @return
     */
    public Collection<ExRegion> getGroupCountyRegion(Long userId, AgentRoleType roleType, Integer type) {
        AgentGroup group = getGroupFirstOne(userId, roleType);
        if (group == null) {
            return Collections.emptyList();
        }
        List<AgentGroupRegion> groupRegionList = new ArrayList<>();
        //如果是全国角色
        if (roleType == AgentRoleType.Country) {
//            List<AgentGroup> agentGroups = getGroupListByParentId(group.getId());
//            List<AgentGroupRegion> tmpGroupRegionList = getGroupRegionsByGroupSet(agentGroups.stream().map(AgentGroup::getId).collect(Collectors.toSet()));
//            if(CollectionUtils.isNotEmpty(tmpGroupRegionList)){
//                groupRegionList.addAll(tmpGroupRegionList);
//            }
            List<AgentGroup> allGroups = findAllGroups();
            //小学市场
            if (type == 1) {
                group = allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).findFirst().orElse(null);
                //中学市场
            } else if (type == 2) {
                group = allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).findFirst().orElse(null);
            }
        }
        if (null != group) {
            List<AgentGroupRegion> tmpGroupRegionList = getGroupRegionByGroup(group.getId());
            if (CollectionUtils.isNotEmpty(tmpGroupRegionList)) {
                groupRegionList.addAll(tmpGroupRegionList);
            }
        }
        Set<Integer> regionList = groupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet());
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionList);
        return getExRegionMap(exRegionMap);
    }


    public List<AgentGroupRegion> getGroupRegionsByGroupSet(Collection<Long> userGroups) {
        if (CollectionUtils.isEmpty(userGroups)) {
            return Collections.emptyList();
        }
        return agentGroupRegionLoaderClient.findByGroupSet(userGroups);
    }

    public MapMessage createRegionTree(Collection<ExRegion> countys, Integer level, Collection<Integer> isSelectedId) {
        List<NodeStructure> allRegion = new ArrayList<>();
        Set<Integer> provinceCode = countys.stream().map(ExRegion::getProvinceCode).collect(Collectors.toSet());
        List<NodeStructure> regions = raikouSystem.getRegionBuffer().loadRegions(provinceCode).values()
                .stream()
                .map(p -> createNodeStructureByRegion(SafeConverter.toString(p.getProvinceCode()), "0", SafeConverter.toString(p.getProvinceName())))
                .collect(Collectors.toList());
        if (AgentRegionType.typeOf(level) == AgentRegionType.PROVINCE) {
            allRegion.addAll(regions);
            allRegion.forEach(p -> {
                if (isSelectedId.contains(SafeConverter.toInt(p.getId()))) {
                    p.setIsSelected(true);
                } else {
                    p.setIsSelected(false);
                }
            });
            return NodeStructureUtil.formatNode(allRegion);
        }
        Set<Integer> cityCode = countys.stream().map(ExRegion::getCityCode).collect(Collectors.toSet());
        List<NodeStructure> citys = raikouSystem.getRegionBuffer().loadRegions(cityCode).values()
                .stream()
                .map(p -> createNodeStructureByRegion(SafeConverter.toString(p.getCityCode()), SafeConverter.toString(p.getProvinceCode()), SafeConverter.toString(p.getCityName())))
                .collect(Collectors.toList());
        if (AgentRegionType.typeOf(level) == AgentRegionType.CITY) {
            allRegion.addAll(regions);
            allRegion.addAll(citys);
            allRegion.forEach(p -> {
                if (isSelectedId.contains(SafeConverter.toInt(p.getId()))) {
                    p.setIsSelected(true);
                } else {
                    p.setIsSelected(false);
                }
            });
            return NodeStructureUtil.formatNode(allRegion);
        }
        List<NodeStructure> countyNodes = countys.stream().filter(p -> p.fetchRegionType() == RegionType.COUNTY).map(p -> createNodeStructureByRegion(SafeConverter.toString(p.getCountyCode()), SafeConverter.toString(p.getCityCode()), SafeConverter.toString(p.getCountyName()))).collect(Collectors.toList());
        if (AgentRegionType.typeOf(level) == AgentRegionType.COUNTY) {
            allRegion.addAll(regions);
            allRegion.addAll(citys);
            allRegion.addAll(countyNodes);
            allRegion.forEach(p -> {
                if (isSelectedId.contains(SafeConverter.toInt(p.getId()))) {
                    p.setIsSelected(true);
                } else {
                    p.setIsSelected(false);
                }
            });
            return NodeStructureUtil.formatNode(allRegion);
        }
        return MapMessage.errorMessage("所选的级别错误");
    }

    private NodeStructure createNodeStructureByRegion(String id, String pId, String name) {
        NodeStructure node = new NodeStructure();
        node.setId(id);
        node.setPId(pId);
        node.setName(name);
        return node;
    }

    // AgentUser
    public AgentUser getUser(Long userId) {
        AgentUser user = agentUserLoaderClient.load(userId);
        return (user != null && user.isValidUser()) ? user : null;
    }

    // AgentUser
    public AgentUser getUserIncludeDel(Long userId) {
        return agentUserLoaderClient.loadIncludeDel(userId);
    }

    public List<AgentUser> findAllAgentUsers() {
        return agentUserLoaderClient.findAll();
    }

    public List<AgentUser> getUsers(Collection<Long> userIds) {
        Map<Long, AgentUser> userMap = agentUserLoaderClient.findByIds(userIds);
        return userMap.values().stream().filter(p -> p.isValidUser() && userIds.contains(p.getId())).collect(Collectors.toList());
    }

    public AgentUser getUserByName(String accountName) {
        if (StringUtils.isEmpty(accountName)) {
            return null;
        }
        AgentUser user = agentUserLoaderClient.findByName(accountName);
        return (user != null && user.isValidUser()) ? user : null;
    }


    /**
     * 全匹配查询
     *
     * @param realName
     * @return
     */
    public List<AgentUser> getUserByRealName(String realName) {
        if (StringUtils.isEmpty(realName)) {
            return Collections.emptyList();
        }
        return agentUserLoaderClient.getUserByRealName(realName);
    }

    /**
     * 模糊匹配查询
     *
     * @param realName
     * @return
     */
    public List<AgentUser> findUserByRealName(String realName) {
        if (StringUtils.isEmpty(realName)) {
            return Collections.emptyList();
        }
        return agentUserLoaderClient.findByRealName(realName);
    }

    /**
     * 根据工号查询用户
     *
     * @param accountNumber
     * @return
     */
    public List<AgentUser> getUserByAccountNumber(String accountNumber) {
        if (StringUtils.isEmpty(accountNumber)) {
            return Collections.emptyList();
        }
        return agentUserLoaderClient.getUserByAccountNumber(accountNumber);
    }


    //****************  Group 相关 Start ***************
    public AgentGroup getGroupById(Long groupId) {
        return agentGroupLoaderClient.load(groupId);
    }

    public List<AgentGroup> getGroupByIds(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        return agentGroupLoaderClient.loads(groupIds).values().stream().filter(p -> p != null).collect(Collectors.toList());
    }

    public AgentGroup getGroupByName(String groupName) {
        return agentGroupLoaderClient.findByGroupName(groupName);
    }

    public List<AgentGroup> getGroupListByParentId(Long parentGroupId) {
        if (parentGroupId == null) {
            return Collections.emptyList();
        }

        return agentGroupLoaderClient.findByParentId(parentGroupId);
    }

    public List<AgentGroup> getAgentGroupByRole(AgentGroupRoleType roleType) {
        return agentGroupLoaderClient.findByRoleId(roleType.getId());
    }

    public Map<Long, List<AgentGroup>> getGroupListByParentIds(Collection<Long> parentGroupIds) {
        if (CollectionUtils.isEmpty(parentGroupIds)) {
            return Collections.emptyMap();
        }
        return agentGroupLoaderClient.findByParentIds(parentGroupIds);
    }

    public AgentGroup getParentGroup(Long groupId) {
        AgentGroup agentGroup = getGroupById(groupId);
        return getParentGroup(agentGroup);
    }

    public AgentGroup getParentGroup(AgentGroup agentGroup) {
        AgentGroup parentGroup = null;
        if (agentGroup != null && agentGroup.getParentId() > 0L) {
            parentGroup = getGroupById(agentGroup.getParentId());
        }
        return parentGroup;
    }

    public List<AgentGroup> getUserGroups(Long userId) {
        List<AgentGroup> retGroups = new ArrayList<>();

        List<AgentGroupUser> userGroups = agentGroupUserLoaderClient.findByUserId(userId);
        for (AgentGroupUser userGroup : userGroups) {
            CollectionUtils.addNonNullElement(retGroups, getGroupById(userGroup.getGroupId()));
        }

        return retGroups;
    }

    public Map<Long, List<AgentGroup>> getUserGroups(Set<Long> userIds) {
        Map<Long, List<AgentGroup>> retGroupsMap = new HashMap<>();

        Map<Long, List<AgentGroupUser>> userGroupsMap = agentGroupUserLoaderClient.findByUserIds(userIds);
        userGroupsMap.forEach((key, userGroups) -> {
            List<AgentGroup> retGroups = getGroupByIds(userGroups.stream().map(AgentGroupUser::getGroupId).collect(toSet()));
            retGroupsMap.put(key, retGroups);
        });

        return retGroupsMap;
    }

    public Map<Long, List<Long>> getUserGroupIdList(Collection<Long> userIds) {
        Map<Long, List<Long>> retGroupsMap = new HashMap<>();
        Map<Long, List<AgentGroupUser>> userGroupsMap = agentGroupUserLoaderClient.findByUserIds(userIds);
        userGroupsMap.forEach((k, v) -> {
            retGroupsMap.put(k, v.stream().map(AgentGroupUser::getGroupId).filter(Objects::nonNull).collect(toList()));
        });
        return retGroupsMap;
    }

    public List<AgentGroup> getUserGroups(Long userId, AgentRoleType role) {
        List<AgentGroup> retGroups = new ArrayList<>();
        List<AgentGroupUser> userGroups = agentGroupUserLoaderClient.findByUserId(userId).stream().filter(p -> role == null || p.getUserRoleType() == role).collect(Collectors.toList());
        for (AgentGroupUser userGroup : userGroups) {
            CollectionUtils.addNonNullElement(retGroups, getGroupById(userGroup.getGroupId()));
        }
        return retGroups;
    }

    public AgentGroup getUserGroupsFirstOne(Long userId, AgentRoleType role) {
        List<AgentGroup> result = getUserGroups(userId, role);
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        return result.get(0);
    }


    //获取制定部门下面的所有子部门
    public void getAllSubGroupList(List<AgentGroup> groupList, Long groupId) {
        List<AgentGroup> subGroupList = getGroupListByParentId(groupId);
        if (CollectionUtils.isEmpty(subGroupList)) {
            return;
        }
        groupList.addAll(subGroupList);
        for (AgentGroup agentGroup : subGroupList) {
            getAllSubGroupList(groupList, agentGroup.getId());
        }
    }

    // 递归获取指定部门下面的所有子部门
    public List<AgentGroup> getSubGroupList(Long groupId) {
        List<AgentGroup> groupList = new ArrayList<>();
        List<AgentGroup> subGroupList = getGroupListByParentId(groupId);
        if (CollectionUtils.isEmpty(subGroupList)) {
            return groupList;
        }
        groupList.addAll(subGroupList);

        for (AgentGroup agentGroup : subGroupList) {
            List<AgentGroup> tempList = getSubGroupList(agentGroup.getId());
            if (CollectionUtils.isNotEmpty(tempList)) {
                groupList.addAll(tempList);
            }
        }
        return groupList;
    }


    // 获取市场部所有人员信息， 把这个收集到这里，以后如果要改也方便一些
    public List<AgentGroupUser> getAllMarketDepartmentUsers() {
        List<AgentGroup> marketGroup = getAgentGroupByRole(AgentGroupRoleType.Country);
        if (CollectionUtils.isEmpty(marketGroup)) {
            return Collections.emptyList();
        }
        return getAllGroupUsersByGroupId(marketGroup.get(0).getId());
    }

    //****************  GroupRegion 相关 Start ***************
    public List<AgentGroupRegion> getGroupRegionByGroup(Long groupId) {
        return agentGroupRegionLoaderClient.findByGroupId(groupId);
    }

    public List<AgentGroupRegion> getGroupRegionByRegion(Integer regionCode) {
        return agentGroupRegionLoaderClient.findByRegionCode(regionCode);
    }

    public AgentGroupRegion getGroupRegion(Long groupId, Integer regionCode) {
        List<AgentGroupRegion> groupRegions = getGroupRegionByGroup(groupId);
        return groupRegions.stream()
                .filter(p -> Objects.equals(p.getRegionCode(), regionCode))
                .findFirst()
                .orElse(null);
    }

    //
    public AgentGroupRegion getParentGroupRegion(Long groupId, Integer regionCode) {
        AgentGroup agentGroup = getGroupById(groupId);
        AgentGroupRegion parentRegion = null;
        if (agentGroup != null && agentGroup.getParentId() > 0) { // 有上级部门
            ExRegion exRegion = raikouSystem.loadRegion(regionCode);

            List<AgentGroupRegion> parentRegionList = getGroupRegionByGroup(agentGroup.getParentId());
            parentRegion = parentRegionList.stream().filter(p -> Objects.equals(p.getRegionCode(), regionCode)).findFirst().orElse(null);
            if (parentRegion == null) {
                if (exRegion.fetchRegionType() == RegionType.COUNTY) {
                    parentRegion = parentRegionList.stream().filter(p -> Objects.equals(p.getRegionCode(), exRegion.getCityCode())).findFirst().orElse(null);
                    if (parentRegion == null) {
                        parentRegion = parentRegionList.stream().filter(p -> Objects.equals(p.getRegionCode(), exRegion.getProvinceCode())).findFirst().orElse(null);
                    }
                } else if (exRegion.fetchRegionType() == RegionType.CITY) {
                    parentRegion = parentRegionList.stream().filter(p -> Objects.equals(p.getRegionCode(), exRegion.getProvinceCode())).findFirst().orElse(null);
                }
            }
        }
        return parentRegion;
    }

    // 判断部门是否覆盖本区域
    public boolean isGroupCoverRegion(Long groupId, Integer regionCode) {
        List<Integer> groupRegionCodeList = getGroupRegionCodeList(groupId);
        if (CollectionUtils.isEmpty(groupRegionCodeList)) {
            return false;
        }

        if (groupRegionCodeList.contains(regionCode)) {
            return true;
        } else {
            ExRegion exRegion = raikouSystem.loadRegion(regionCode);
            if (exRegion == null) {
                return false;
            }
            if (exRegion.fetchRegionType() == RegionType.COUNTY) {
                return groupRegionCodeList.contains(exRegion.getCityCode()) || groupRegionCodeList.contains(exRegion.getProvinceCode());
            } else {
                return groupRegionCodeList.contains(exRegion.getProvinceCode());
            }
        }
    }

    public List<Integer> getGroupRegionCountyCodeList(Long groupId) {
        List<Integer> regionList = getGroupRegionCodeList(groupId);
        return agentRegionService.getCountyCodes(regionList);
    }

    public List<Integer> getGroupRegionCodeList(Long groupId) {
        List<AgentGroupRegion> groupRegionList = getGroupRegionByGroup(groupId);
        if (CollectionUtils.isEmpty(groupRegionList)) {
            return Collections.emptyList();
        }
        return groupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toList());
    }


    //***************** GroupUser 相关 Start***************
    public List<AgentGroupUser> getGroupUserByGroup(Long groupId) {
        return agentGroupUserLoaderClient.findByGroupId(groupId);
    }

    public List<AgentGroupUser> getGroupUserByGroups(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        Map<Long, List<AgentGroupUser>> groupUserMap = agentGroupUserLoaderClient.findByGroupIds(groupIds);
        if (MapUtils.isEmpty(groupUserMap)) {
            return Collections.emptyList();
        }
        List<AgentGroupUser> retList = new ArrayList<>();
        groupUserMap.values().forEach(p -> {
            if (CollectionUtils.isNotEmpty(p)) {
                retList.addAll(p);
            }
        });
        return retList;
    }

    public List<AgentGroupUser> getGroupUserByUser(Long userId) {
        return agentGroupUserLoaderClient.findByUserId(userId);
    }

    public Boolean checkUserRole(Long userId, AgentRoleType role) {
        return CollectionUtils.isNotEmpty(getGroupUserByUser(userId).stream().filter(p -> p.getUserRoleType() == role).collect(Collectors.toList()));
    }

    public String getUserGroupNames(Long userId) {
        List<AgentGroupUser> groupUsers = getGroupUserByUser(userId);
        StringBuilder builder = new StringBuilder();
        for (AgentGroupUser groupUser : groupUsers) {
            AgentGroup group = getGroupById(groupUser.getGroupId());
            if (group != null) {
                builder.append(group.getGroupName()).append(" ");
            }
        }
        return builder.toString();
    }

    public List<AgentGroupUser> getGroupUserByRole(Integer roleId) {
        return agentGroupUserLoaderClient.findByRoleId(roleId);
    }

    // 获取部门下面的员工Id
    public List<Long> getGroupUserIds(Long groupId) {
        List<AgentUser> groupUsers = getGroupUsers(groupId);
        return groupUsers.stream().map(AgentUser::getId).collect(Collectors.toList());
    }

    // 获取部门下面的员工
    public List<AgentUser> getGroupUsers(Long groupId) {
        List<AgentGroupUser> groupUsers = getGroupUserByGroup(groupId);
        if (CollectionUtils.isEmpty(groupUsers)) {
            return Collections.emptyList();
        }

        List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
        return userIds.stream().map(agentUserLoaderClient::load).filter(p -> p != null && p.isValidUser()).collect(Collectors.toList());
    }

    /**
     * 获取部门下专员信息
     *
     * @param groupId
     * @return
     */
    public List<AgentUser> getGroupBusinessDevelopers(Long groupId) {
        List<AgentGroupUser> groupUsers = getGroupUserByGroup(groupId);
        if (CollectionUtils.isEmpty(groupUsers)) {
            return Collections.emptyList();
        }

        List<Long> userIds = groupUsers.stream().filter(item -> Objects.equals(item.getUserRoleType(), AgentRoleType.BusinessDeveloper)).map(AgentGroupUser::getUserId).collect(Collectors.toList());
        return userIds.stream().map(agentUserLoaderClient::load).filter(p -> p != null && p.isValidUser()).collect(Collectors.toList());
    }

    // 获取用户所在的部门Id列表 FIXME refactor later
    public List<Long> getGroupIdListByUserId(Long userId) {
        List<AgentGroupUser> groupUsersList = getGroupUserByUser(userId);
        if (CollectionUtils.isEmpty(groupUsersList)) {
            return Collections.emptyList();
        }
        return groupUsersList.stream().map(AgentGroupUser::getGroupId).collect(Collectors.toList());
    }

    // 根据用户ID获取用户所在各个部门里面的角色
    public Map<Long, Integer> getGroupUserRoleMapByUserId(Long userId) {
        List<AgentGroupUser> groupUsersList = agentGroupUserLoaderClient.findByUserId(userId);
        if (CollectionUtils.isEmpty(groupUsersList)) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> groupUserRoleMap = new HashMap<>();
        groupUsersList.forEach(p -> groupUserRoleMap.put(p.getGroupId(), p.getUserRoleId()));
        return groupUserRoleMap;
    }

    public Map<Long, Integer> getGroupUserRoleMapByGroupId(Long groupId) {
        List<AgentGroupUser> groupUsersList = agentGroupUserLoaderClient.findByGroupId(groupId);
        if (CollectionUtils.isEmpty(groupUsersList)) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> groupUserRoleMap = new HashMap<>();
        groupUsersList.forEach(p -> groupUserRoleMap.put(p.getUserId(), p.getUserRoleId()));
        return groupUserRoleMap;
    }

    public Map<Long, List<Integer>> getGroupUserRoleMapByUserIds(Collection<Long> userIds) {
        Map<Long, List<Integer>> userRoles = new HashMap<>();
        if (CollectionUtils.isNotEmpty(userIds)) {
            Map<Long, List<AgentGroupUser>> groupUsersMap = agentGroupUserLoaderClient.findByUserIds(userIds);
            groupUsersMap.forEach((k, v) -> {
                userRoles.put(k, v.stream().map(AgentGroupUser::getUserRoleId).collect(toList()));
            });
        }
        return userRoles;
    }

    /**
     * 获取用户负责的部门列表
     *
     * @param userId userId
     * @return list
     */
    public List<Long> getManagedGroupIdListByUserId(Long userId) {
        List<AgentGroupUser> groupUsersList = agentGroupUserLoaderClient.findByUserId(userId);
        if (CollectionUtils.isEmpty(groupUsersList)) {
            return Collections.emptyList();
        }
        return groupUsersList.stream()
                .filter(p -> (Objects.equals(p.getUserRoleId(), AgentRoleType.Country.getId())
                        || Objects.equals(p.getUserRoleId(), AgentRoleType.BUManager.getId())
                        || Objects.equals(p.getUserRoleId(), AgentRoleType.Region.getId())
                        || Objects.equals(p.getUserRoleId(), AgentRoleType.AreaManager.getId())
                        || Objects.equals(p.getUserRoleId(), AgentRoleType.CityManager.getId()))
                        || Objects.equals(p.getUserRoleId(), AgentRoleType.ChannelDirector.getId())
                        || Objects.equals(p.getUserRoleId(), AgentRoleType.ChannelManager.getId())
                        || Objects.equals(p.getUserRoleId(), AgentRoleType.CityAgent.getId())
                )
                .map(AgentGroupUser::getGroupId).collect(Collectors.toList());
    }

    public AgentGroupUser getGroupUser(Long groupId, Long userId) {
        List<AgentGroupUser> groupUsersList = getGroupUserByGroup(groupId);
        return groupUsersList.stream().filter(p -> Objects.equals(p.getUserId(), userId))
                .findFirst()
                .orElse(null);
    }

    public Set<Long> findAllGroupUserIds() {
        return findAllGroupUserIds(null);
    }

    public Set<Long> findAllGroupUserIds(AgentRoleType roleType) {
        List<AgentGroupUser> groupUsersList = agentGroupUserLoaderClient.findAll();
        if (CollectionUtils.isEmpty(groupUsersList)) {
            return Collections.emptySet();
        }
        return groupUsersList.stream().filter(p -> roleType == null || Objects.equals(p.getUserRoleType(), roleType)).map(AgentGroupUser::getUserId).collect(Collectors.toSet());
    }

    // ****************  userSchool 相关 Start ***************

    // 获取用户所负责的学校列表
    public List<AgentUserSchool> getUserSchoolByUser(Long userId) {
        return agentUserSchoolLoaderClient.findByUserId(userId);
    }

    public Map<Long, List<AgentUserSchool>> getUserSchoolByUsers(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        return agentUserSchoolLoaderClient.findByUserIds(userIds);
    }

    public List<AgentUserSchool> getUserSchoolBySchool(Long schoolId) {
        return agentUserSchoolLoaderClient.findBySchoolId(schoolId);
    }

    public Map<Long, List<AgentUserSchool>> getUserSchoolBySchools(Collection<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyMap();
        }
        return agentUserSchoolLoaderClient.findBySchoolIds(schoolIds)
                .stream()
                .collect(Collectors.groupingBy(AgentUserSchool::getSchoolId, Collectors.toList()));
    }


    // 获取负责指定用户负责的学校Id
    public List<Long> getUserSchools(Long userId) {
        return getUserSchools(userId, 0);
    }

    public List<Long> getUserSchools(Long userId, Integer schoolLevel) {
        List<AgentUserSchool> userSchoolList = getUserSchoolByUser(userId);
        return userSchoolList.stream().filter(p -> schoolLevel == 0 || Objects.equals(schoolLevel, p.getSchoolLevel())).map(AgentUserSchool::getSchoolId).collect(Collectors.toList());
    }

    public AgentUserSchool getUserSchool(Long userId, Long schoolId) {
        List<AgentUserSchool> userSchoolList = getUserSchoolByUser(userId);
        return userSchoolList.stream().filter(p -> Objects.equals(p.getSchoolId(), schoolId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取用户在指定部门里面的角色
     *
     * @param userId  userId
     * @param groupId groupId
     * @return role
     */
    public Integer getGroupUserRole(Long userId, Long groupId) {
        Map<Long, Integer> groupUserRoleMap = getGroupUserRoleMapByUserId(userId);
        return groupUserRoleMap.get(groupId);
    }

    /**
     * 判断用户是否是指定部门的负责人
     *
     * @param userId  userId
     * @param groupId groupId
     * @return boolean
     */
    public boolean isGroupManager(Long userId, Long groupId) {
        Integer userRoleId = getGroupUserRole(userId, groupId);
        return userRoleId != null
                && (Objects.equals(userRoleId, AgentRoleType.Country.getId()) || Objects.equals(userRoleId, AgentRoleType.BUManager.getId())
                || Objects.equals(userRoleId, AgentRoleType.Region.getId()) || Objects.equals(userRoleId, AgentRoleType.AreaManager.getId())
                || Objects.equals(userRoleId, AgentRoleType.CityManager.getId()));
    }

    public Long getGroupManager(Long groupId) {
        List<AgentGroupUser> groupUserList = getGroupUserByGroup(groupId);
        if (CollectionUtils.isEmpty(groupUserList)) {
            return null;
        }
        for (AgentGroupUser groupUser : groupUserList) {
            if (Objects.equals(groupUser.getUserRoleId(), AgentRoleType.Country.getId())
                    || Objects.equals(groupUser.getUserRoleId(), AgentRoleType.BUManager.getId())
                    || Objects.equals(groupUser.getUserRoleId(), AgentRoleType.Region.getId())
                    || Objects.equals(groupUser.getUserRoleId(), AgentRoleType.AreaManager.getId())
                    || Objects.equals(groupUser.getUserRoleId(), AgentRoleType.CityManager.getId())) {
                return groupUser.getUserId();
            }
        }
        return null;
    }

    /**
     * 获取部门管理员
     *
     * @param groupId
     * @return
     */
    public Set<Long> getGroupManagers(Long groupId) {
        List<AgentGroupUser> groupUserList = getGroupUserByGroup(groupId);
        if (CollectionUtils.isEmpty(groupUserList)) {
            return Collections.emptySet();
        }
        return groupUserList.stream().filter(groupUser ->
                Objects.equals(groupUser.getUserRoleId(), AgentRoleType.Country.getId())
                        || Objects.equals(groupUser.getUserRoleId(), AgentRoleType.BUManager.getId())
                        || Objects.equals(groupUser.getUserRoleId(), AgentRoleType.Region.getId())
                        || Objects.equals(groupUser.getUserRoleId(), AgentRoleType.AreaManager.getId())
                        || Objects.equals(groupUser.getUserRoleId(), AgentRoleType.CityManager.getId()))
                .map(AgentGroupUser::getUserId).collect(toSet());
    }

    /**
     * 获取当前部门所有的管理者，以及父级部门的管理者、父父级部门管理者……，直到市场部结束
     *
     * @param groupId
     * @return
     */
    public Set<Long> getAllGroupManagers(final Long groupId) {
        Objects.requireNonNull(groupId);
        Set<Long> allGroupManagers = new HashSet<>();
        Long tempGroupId = groupId;
        while (true) {
            AgentGroup agentGroup = getGroupById(tempGroupId);
            if (null != agentGroup) {
                Set<Long> groupManagers = getGroupManagers(agentGroup.getId());
                allGroupManagers.addAll(groupManagers);
                if (Objects.equals(AgentGroupRoleType.Country, agentGroup.fetchGroupRoleType()) || null == agentGroup.getParentId() || agentGroup.getParentId() <= 0) {
                    break;
                }
                tempGroupId = agentGroup.getParentId();
            } else {
                break;
            }
        }
        return allGroupManagers;
    }


    /**
     * 根据用户名查找用户所在的部门列表
     *
     * @param userName userName
     * @return map
     */
    public Map<AgentUser, List<AgentGroup>> searchByUserName(String userName) {
        List<AgentUser> userList = agentUserLoaderClient.findByRealName(userName);
        if (CollectionUtils.isEmpty(userList)) {
            return Collections.emptyMap();
        }
        userList = userList.stream().filter(p -> p != null && p.isValidUser()).collect(Collectors.toList());
        Map<AgentUser, List<AgentGroup>> retMap = new HashMap<>();
        for (AgentUser user : userList) {
            List<AgentGroup> agentGroupList = getUserGroups(user.getId());
            if (CollectionUtils.isEmpty(agentGroupList)) {
                agentGroupList = Collections.emptyList();
            }
            retMap.put(user, agentGroupList);
        }
        return retMap;
    }

    /**
     * 查询所有Group
     *
     * @return
     */
    public List<AgentGroup> findAllGroups() {
        return agentGroupLoaderClient.findAllGroups();
    }

    /**
     * 返回部门的树形结构，不包含部门员工
     *
     * @return map
     */
    public Map<String, Map<String, Object>> buildAllGroupTree() {
        List<AgentGroup> allGroupList = findAllGroups();
        if (CollectionUtils.isEmpty(allGroupList)) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, Object>> retMap = new HashMap<>();
        for (AgentGroup group : allGroupList) {
            Map<String, Object> groupItemMap = new HashMap<>();
            groupItemMap.put("title", group.getGroupName());
            groupItemMap.put("key", String.valueOf(group.getId()));
            groupItemMap.put("role", group.fetchGroupRoleType());
            groupItemMap.put("type", "group");
            groupItemMap.put("pcode", String.valueOf(group.getParentId()));
            groupItemMap.put("children", new ArrayList());

            retMap.put(String.valueOf(group.getId()), groupItemMap);
        }

        for (AgentGroup group : allGroupList) {
            Long parentId = group.getParentId();
            if (parentId == 0) {
                continue;
            }
            Map<String, Object> parentObj = retMap.get(String.valueOf(parentId));
            Map<String, Object> childObj = retMap.get(String.valueOf(group.getId()));
            if (parentObj != null) {
                List childrenList = (List) parentObj.get("children");
                if (!childrenList.contains(childObj)) {
                    childrenList.add(childObj);
                }
            }
        }
        return retMap;
    }

    /**
     * 返回部门的树形结构，包含部门员工
     *
     * @return map
     */
    public Map<String, Map<String, Object>> buildAllGroupTreeIncludeUsers() {
        Map<String, Map<String, Object>> groupTreeMap = buildAllGroupTree();
        if (MapUtils.isEmpty(groupTreeMap)) {
            return groupTreeMap;
        }
        List<AgentUser> groupUserList;
        for (String groupId : groupTreeMap.keySet()) {
            groupUserList = getGroupUsers(Long.valueOf(groupId));
            if (CollectionUtils.isEmpty(groupUserList)) {
                continue;
            }
            Map<String, Object> groupMap = groupTreeMap.get(groupId);
            List childrenList = (List) groupMap.get("children");
            Map<String, Object> cityManagerItemMap = null;
            for (AgentUser user : groupUserList) {
                Map<String, Object> userItemMap = new HashMap<>();
                userItemMap.put("key", String.valueOf(user.getId()));
                userItemMap.put("type", "user");
                userItemMap.put("pcode", "");
                Integer userRole = getGroupUserRole(user.getId(), Long.valueOf(groupId));
                userItemMap.put("role", userRole);
                String title = user.getRealName();
                if (AgentRoleType.CityManager.getId().equals(userRole)) {
                    userItemMap.put("title", title + "（市经理）");
                    cityManagerItemMap = userItemMap;
                    continue;
                }
                userItemMap.put("title", title);
                childrenList.add(0, userItemMap);

            }
            if (null != cityManagerItemMap) {
                childrenList.add(0, cityManagerItemMap);
            }
        }
        return groupTreeMap;
    }

    /**
     * 获取指定用户所负责的部门列表包括用户（树形结构）
     *
     * @param user user
     * @return list
     */
    public List<Map<String, Object>> loadUserGroupTreeIncludeUsers(AuthCurrentUser user) {
        List<Map<String, Object>> retList = new ArrayList<>();
        Map<String, Map<String, Object>> allGroupTreeMap = buildAllGroupTreeIncludeUsers();

        List<Long> groupIdList = new ArrayList<>();
        if (user.isAdmin()) {
            List<AgentGroup> groupList = getRootAgentGroups();
            if (CollectionUtils.isNotEmpty(groupList)) {
                groupIdList.addAll(groupList.stream().map(AbstractDatabaseEntity::getId).collect(Collectors.toSet()));
            }
        } else {
            groupIdList.addAll(getManagedGroupIdListByUserId(user.getUserId()));
        }
        for (Long groupId : groupIdList) {
            CollectionUtils.addNonNullElement(retList, allGroupTreeMap.get(String.valueOf(groupId)));
        }
        return retList;
    }

    /**
     * 获取指定用户所负责的部门列表（树形结构）
     *
     * @param user user
     * @return list
     */
    public List<Map<String, Object>> loadUserGroupTree(AuthCurrentUser user) {
        List<Map<String, Object>> retList = new ArrayList<>();
        Map<String, Map<String, Object>> allGroupTreeMap = buildAllGroupTree();

        List<Long> groupIdList = new ArrayList<>();
        if (user.isAdmin()) {
            List<AgentGroup> groupList = getRootAgentGroups();
            if (CollectionUtils.isNotEmpty(groupList)) {
                groupIdList.addAll(groupList.stream().map(AbstractDatabaseEntity::getId).collect(Collectors.toSet()));
            }
        } else {
            groupIdList.addAll(getManagedGroupIdListByUserId(user.getUserId()));
        }
        for (Long groupId : groupIdList) {
            CollectionUtils.addNonNullElement(retList, allGroupTreeMap.get(String.valueOf(groupId)));
        }
        return retList;
    }

    public void markSelectedGroup(List<Map<String, Object>> mapList, Collection<Long> groupIds) {
        if (CollectionUtils.isNotEmpty(mapList) && CollectionUtils.isNotEmpty(groupIds)) {
            mapList.forEach(p -> markSelectedGroup(p, groupIds));
        }
    }

    private void markSelectedGroup(Map<String, Object> dataMap, Collection<Long> groupIds) {
        Long groupId = SafeConverter.toLong(dataMap.get("key"));
        if (groupIds.contains(groupId)) {
            dataMap.put("selected", Boolean.TRUE);
        }
        List children = (List) dataMap.get("children");
        if (CollectionUtils.isNotEmpty(children)) {
            for (Object p : children) {
                markSelectedGroup((Map<String, Object>) p, groupIds);
            }
        }
    }


    /**
     * 根据部门级别返回该部门下用户的角色列表
     *
     * @param groupRoleType groupRoleType
     * @return list
     */
    public List<AgentRoleType> getAgentRoleTypeList(AgentGroupRoleType groupRoleType) {
        List<AgentRoleType> agentRoleTypeList = new ArrayList<>();
        if (groupRoleType == null) {
            agentRoleTypeList.addAll(AgentRoleType.getAllAgentRoles().values());
        } else if (groupRoleType == AgentGroupRoleType.Admin) {
            agentRoleTypeList.add(AgentRoleType.Admin);
        } else if (groupRoleType == AgentGroupRoleType.Finance) {
            agentRoleTypeList.add(AgentRoleType.Finance);
        } else if (groupRoleType == AgentGroupRoleType.DataViewer) {
            agentRoleTypeList.add(AgentRoleType.DataViewer);
        } else if (groupRoleType == AgentGroupRoleType.Vendor) {
            agentRoleTypeList.add(AgentRoleType.Vendor);
        } else if (groupRoleType == AgentGroupRoleType.Country) {
            agentRoleTypeList.add(AgentRoleType.Country);
        } else if (groupRoleType == AgentGroupRoleType.BusinessUnit) {
            agentRoleTypeList.add(AgentRoleType.BUManager);
        } else if (groupRoleType == AgentGroupRoleType.Operation) {
//  todo          agentRoleTypeList.add(AgentRoleType.BUManager);
        } else if (groupRoleType == AgentGroupRoleType.Marketing) {
//  todo          agentRoleTypeList.add(AgentRoleType.BUManager);
        } else if (groupRoleType == AgentGroupRoleType.Region) {
            agentRoleTypeList.add(AgentRoleType.Region);
            agentRoleTypeList.add(AgentRoleType.ChannelManager);
        } else if (groupRoleType == AgentGroupRoleType.Area) {
            agentRoleTypeList.add(AgentRoleType.AreaManager);
            agentRoleTypeList.add(AgentRoleType.ChannelManager);
        } else if (groupRoleType == AgentGroupRoleType.City) {
            agentRoleTypeList.add(AgentRoleType.CityManager);
            agentRoleTypeList.add(AgentRoleType.CityAgent);
            agentRoleTypeList.add(AgentRoleType.BusinessDeveloper);
            agentRoleTypeList.add(AgentRoleType.CityAgentLimited);
        } else if (groupRoleType == AgentGroupRoleType.EVALUATION) {
            agentRoleTypeList.add(AgentRoleType.EVALUATION);
        } else if (groupRoleType == AgentGroupRoleType.USAGEINFO) {
            agentRoleTypeList.add(AgentRoleType.USAGEINFO);
        } else if (groupRoleType == AgentGroupRoleType.MATERIAL) {
            agentRoleTypeList.add(AgentRoleType.MATERIAL);
        } else if (groupRoleType == AgentGroupRoleType.RISK_MANAGEMENT) {
            agentRoleTypeList.add(AgentRoleType.RiskManager);
        } else if (groupRoleType == AgentGroupRoleType.PRODUCT_OPERATION) {
            agentRoleTypeList.add(AgentRoleType.PRODUCT_OPERATOR);
        } else {
            agentRoleTypeList.addAll(AgentRoleType.getAllAgentRoles().values());
        }
        return agentRoleTypeList;
    }

    /**
     * 获取部门负责的区域列表（树形结构）
     *
     * @param groupId groupId
     * @return list
     */
    public List<Map<String, Object>> loadGroupRegionTreeByGroupId(Long groupId) {
        List<Map<String, Object>> retList = new ArrayList<>();
        Map<String, Map<String, Object>> allRegionTreeMap = agentRegionService.getAllRegionTreeCopy();
        if (MapUtils.isEmpty(allRegionTreeMap)) {
            return Collections.emptyList();
        }
        AgentGroup agentGroup = getGroupById(groupId);
        if (groupId == 0 || (agentGroup != null && agentGroup.getParentId() == 0)) { //上级部门是根节点
            allRegionTreeMap.forEach((k, v) -> {
                String pcode = (String) v.get("pcode");
                if (StringUtils.isBlank(pcode)) {
                    retList.add(allRegionTreeMap.get(k));
                }
            });
        } else {
            List<AgentGroupRegion> groupRegionList = getGroupRegionByGroup(groupId);
            if (CollectionUtils.isNotEmpty(groupRegionList)) {
                List<Integer> regionCodeList = groupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toList());
                regionCodeList.forEach(p -> retList.add(allRegionTreeMap.get(String.valueOf(p))));
            }
//            }
        }
        return retList.stream().filter(p -> p != null).collect(Collectors.toList());
    }

    /*********************** CUD 操作 Start *************/

    /**
     * 更新AgentGroup
     */
    public void updateAgentGroup(AgentGroup agentGroup) {
        if (agentGroup != null) {
            agentGroupServiceClient.replace(agentGroup);
        }
    }


    /********************** CUD 操作 End *************/

    /**
     * 获取管理的部门中的其他人员
     *
     * @param userId userId
     * @return list
     */
    public List<AgentUser> getManagedGroupUsers(Long userId, boolean withSelf) {
        List<Long> groupIdList = getManagedGroupIdListByUserId(userId);
        if (CollectionUtils.isEmpty(groupIdList)) {
            return Collections.emptyList();
        }
        Set<Long> userIdList = new HashSet<>();
        for (Long groupId : groupIdList) {
            AgentGroup group = getGroupById(groupId);
            if (group == null) {
                continue;
            }
            AgentGroupRoleType groupRoleType = group.fetchGroupRoleType();
            if (groupRoleType == AgentGroupRoleType.City) { // 如果是市级部门， 获取本部门下其他的人员
                List<Long> groupUserIdList = getGroupUserIds(groupId);
                if (CollectionUtils.isNotEmpty(groupUserIdList)) {
                    userIdList.addAll(groupUserIdList);
                }
            } else { // 如果是全国，大区级别的部门，则获取子部门的管理者，
                List<AgentGroup> subGroupList = getGroupListByParentId(groupId);
                if (CollectionUtils.isNotEmpty(subGroupList)) {
                    for (AgentGroup subGroup : subGroupList) {
                        Long groupManagerId = getGroupManager(subGroup.getId());
                        if (groupManagerId != null) {
                            userIdList.add(groupManagerId);
                        }
                    }
                }
            }
        }
        return userIdList.stream().filter(p -> !Objects.equals(p, userId) || withSelf).map(agentUserLoaderClient::load).filter(p -> p != null && p.isValidUser()).collect(Collectors.toList());
    }

    public List<AgentGroupRegion> getManagedRegionList(Long userId) {
        List<Long> groupIdList = getManagedGroupIdListByUserId(userId);
        return getGroupRegionsByGroupSet(groupIdList);
    }

    /**
     * 获取用户负责的学校
     *
     * @param userId 用户ID
     * @return 学校ID列表
     */
    public List<Long> getManagedSchoolList(Long userId) {
        List<Long> groupIdList = getManagedGroupIdListByUserId(userId);
        if (CollectionUtils.isEmpty(groupIdList)) {
            return getUserSchools(userId);
        }
        Set<Long> schoolIds = new HashSet<>();
        groupIdList.forEach(p -> schoolIds.addAll(getManagedSchoolListByGroupId(p)));
        return new ArrayList<>(schoolIds);
    }

    /**
     * 获取部门负责的学校
     *
     * @param groupId 部门ID
     * @return 学校ID列表
     */
    public List<Long> getManagedSchoolListByGroupId(Long groupId) {
        List<SchoolLevel> schoolLevelList = getGroupServiceSchoolLevels(groupId);
        if (CollectionUtils.isEmpty(schoolLevelList)) {
            return new ArrayList<>();
        }

        Set<Long> schoolIds = new HashSet<>();
        List<Integer> regionCodes = getGroupRegionCodeList(groupId);
        if (CollectionUtils.isNotEmpty(regionCodes)) {
            List<Integer> countyCodes = agentRegionService.getCountyCodes(regionCodes);
            List<AgentDictSchool> dictSchoolList = agentDictSchoolService.loadSchoolDictDataByRegion(countyCodes);
            List<Integer> schoolLevelCodes = schoolLevelList.stream().map(SchoolLevel::getLevel).collect(Collectors.toList());
            dictSchoolList.stream().forEach(p -> {
                if (schoolLevelCodes.contains(p.getSchoolLevel())) {
                    schoolIds.add(p.getSchoolId());
                }
            });
        }
        return new ArrayList<>(schoolIds);
    }

    // 获取指定级别的上级部门（可用于指定的部门在哪个大区等）
    public AgentGroup getParentGroupByRole(Long groupId, AgentGroupRoleType groupRoleType) {
        AgentGroup group = getGroupById(groupId);
        if (group == null) {
            return null;
        }
        if (Objects.equals(group.getRoleId(), groupRoleType.getId())) {
            return group;
        }
        return getParentGroupByRole(group.getParentId(), groupRoleType);
    }

    // 获取该部门及子部门中指定角色的用户
    public List<Long> getAllSubGroupUserIdsByGroupIdAndRole(Long groupId, Integer roleId) {
        List<AgentGroupUser> groupUserList = getAllSubGroupUsersByGroupIdAndRole(groupId, roleId);
        if (CollectionUtils.isEmpty(groupUserList)) {
            return Collections.emptyList();
        }
        Set<Long> userSet = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
        return new ArrayList<>(userSet);
    }

    // 获取该部门及子部门中指定角色的用户
    public List<AgentGroupUser> getAllSubGroupUsersByGroupIdAndRole(Long groupId, Integer roleId) {
        List<AgentGroupUser> groupUserList = getAllGroupUsersByGroupId(groupId);
        if (CollectionUtils.isEmpty(groupUserList)) {
            return Collections.emptyList();
        }
        return groupUserList.stream().filter(p -> Objects.equals(p.getUserRoleId(), roleId)).collect(Collectors.toList());
    }


    // 获取该部门及子部门下所有的用户
    public List<AgentGroupUser> getAllGroupUsersByGroupId(Long groupId) {
        AgentGroup currentGroup = getGroupById(groupId);
        if (currentGroup == null) {
            return Collections.emptyList();
        }

        List<AgentGroup> groupList = new ArrayList<>();
        groupList.add(currentGroup);
        List<AgentGroup> subGroupList = this.getSubGroupList(groupId);
        if (CollectionUtils.isNotEmpty(subGroupList)) {
            groupList.addAll(subGroupList);
        }
        List<Long> groupIdList = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
        return getGroupUserByGroups(groupIdList);
    }


    // 获取用户管辖的地区信息，返回内容
    public List<Integer> getUserRegions(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<AgentGroupRegion> groupRegionList = getManagedRegionList(userId);
        if (CollectionUtils.isEmpty(groupRegionList)) {
            return Collections.emptyList();
        }
        return groupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toList());
    }

    // 获取用户的管理者
    // 专员/市代理 -〉市经理    市经理 -〉大区总监   大区总监 -〉全国总监
    public List<AgentUser> getUserManager(Long userId) {
        List<AgentUser> managerList = new ArrayList<>();

        List<AgentGroup> userGroups = getUserGroups(userId);
        for (AgentGroup group : userGroups) {
            AgentRoleType userRole = AgentGroupRoleType.getGroupManagerRoleType(AgentGroupRoleType.of(group.getRoleId()));
            List<AgentUser> managers = getGroupRoledUsers(group.getId(), userRole);
            if (CollectionUtils.isEmpty(managers)) {
                continue;
            }

            AgentUser manager = managers.get(0);

            if (!Objects.equals(manager.getId(), userId)) {
                managerList.add(manager);
            } else if (group.getParentId() > 0L) {
                AgentGroup parentGroup = getGroupById(group.getParentId());
                userRole = AgentGroupRoleType.getGroupManagerRoleType(AgentGroupRoleType.of(parentGroup.getRoleId()));
                managers = getGroupRoledUsers(parentGroup.getId(), userRole);
                if (CollectionUtils.isNotEmpty(managers)) {
                    managerList.add(managers.get(0));
                }
            }
        }

        return managerList;
    }

    /**
     * 获取用户的leader，逐层往上，直到找到一个为止，最大到全国总监
     * <p>
     * 专员->市经理->区域经理->大区经理->业务部经理->全国总监
     *
     * @param userId
     * @return
     */
    public AgentUser getUserRealManager(Long userId) {
        List<AgentGroupUser> groupUserList = getGroupUserByUser(userId);
        if (CollectionUtils.isNotEmpty(groupUserList)) {
            Long groupId = groupUserList.get(0).getGroupId();
            for (; ; ) {
                AgentGroup agentGroup = getGroupById(groupId);
                if (null != agentGroup) {
                    Long groupManagerId = getGroupManager(groupId);
                    if (null == groupManagerId || groupManagerId.equals(userId)) {
                        if (null != agentGroup.getParentId() && agentGroup.getParentId() > 0) {
                            groupId = agentGroup.getParentId();
                            continue;
                        }
                        break;
                    }
                    return getUser(groupManagerId);
                }
            }
        }
        return null;
    }

    // 获取组里指定角色的用户
    public List<AgentUser> getGroupRoledUsers(Long groupId, AgentRoleType userRole) {
        List<Long> userIds = getGroupUsersByRole(groupId, userRole);

        List<AgentUser> retUserList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userIds)) {
            Map<Long, AgentUser> userMap = agentUserLoaderClient.findByIds(userIds);
            if (MapUtils.isNotEmpty(userMap)) {
                userMap.values().forEach(retUserList::add);
            }
        }
        return retUserList;
    }

    public List<Long> getGroupUsersByRole(Long groupId, AgentRoleType userRole) {
        List<AgentGroupUser> groupUsers = getGroupUserByGroup(groupId);
        return groupUsers.stream()
                .filter(p -> Objects.equals(userRole.getId(), p.getUserRoleId()))
                .map(AgentGroupUser::getUserId)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定部门（多个）中，指定角色的用户
     *
     * @param groupIds
     * @param userRole
     * @return
     */
    public List<Long> getUserByGroupIdsAndRole(Collection<Long> groupIds, AgentRoleType userRole) {
        List<AgentGroupUser> groupUserList = getGroupUserByGroups(groupIds);
        return groupUserList.stream()
                .filter(p -> Objects.equals(userRole.getId(), p.getUserRoleId()))
                .map(AgentGroupUser::getUserId)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定部门（多个）中，指定角色的用户
     *
     * @param groupIds
     * @param userRoles
     * @return
     */
    public List<Long> getUserByGroupIdsAndRoles(Collection<Long> groupIds, List<AgentRoleType> userRoles) {
        List<Integer> rolesIds = userRoles.stream().map(p -> p.getId()).collect(toList());
        List<AgentGroupUser> groupUserList = getGroupUserByGroups(groupIds);
        return groupUserList.stream()
                .filter(p -> rolesIds.contains(p.getUserRoleId()))
                .map(AgentGroupUser::getUserId)
                .collect(Collectors.toList());
    }


    // 获取用户所在指定级别的部门列表(比如：这个专员在哪个大区里面)
    public List<Long> getGroupListByRole(Long userId, AgentGroupRoleType groupRoleType) {
        List<Long> groupIdList = getGroupIdListByUserId(userId);
        if (CollectionUtils.isEmpty(groupIdList)) {
            return null;
        }
        Set<Long> groupIdSet = new HashSet<>();
        AgentGroup group;
        for (Long groupId : groupIdList) {
            group = getParentGroupByRole(groupId, groupRoleType);
            if (group != null && !groupIdSet.contains(group.getId())) {
                groupIdSet.add(group.getId());
            }
        }
        return new ArrayList<>(groupIdSet);
    }

    /**
     * 获取负责指定学校的市场人员(一个学校可能有专员和代理同时负责), 不包括大客户，渠道， 运营等人员
     *
     * @param schoolId 学校ID
     * @return AgentUser
     */
    public List<AgentUser> getSchoolManager(Long schoolId) {
        return getSchoolManager(schoolId, AgentRoleType.getMarketRoleList());
    }

    public List<AgentUser> getSchoolManager(Long schoolId, Collection<AgentRoleType> roleTypeList) {
        if (schoolId == null) {
            return Collections.emptyList();
        }
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return Collections.emptyList();
        }
        Set<Long> userIds = new HashSet<>();
        List<AgentUserSchool> userSchoolList = getUserSchoolBySchool(schoolId);
        if (CollectionUtils.isEmpty(userSchoolList)) {
            return Collections.emptyList();
        }
        Set<Long> userIdList = userSchoolList.stream().map(AgentUserSchool::getUserId).collect(Collectors.toSet());
        Map<Long, List<Integer>> userRoleListMap = getGroupUserRoleMapByUserIds(userIdList);
        userRoleListMap.forEach((k, v) -> {
            if (v.stream().anyMatch(p -> roleTypeList.contains(AgentRoleType.of(p)))) {
                userIds.add(k);
            }
        });
        return getUsers(userIds);
    }

    // 获取用户的角色列表
    public List<AgentRoleType> getUserRoleList(Long userId) {
        Map<Long, Integer> groupRoleMap = getGroupUserRoleMapByUserId(userId);
        if (MapUtils.isEmpty(groupRoleMap)) {
            return Collections.emptyList();
        }
        Set<AgentRoleType> agentRoleTypeSet = groupRoleMap.values().stream().map(AgentRoleType::of).filter(p -> p != null).collect(Collectors.toSet());
        return new ArrayList<>(agentRoleTypeSet);
    }

    public AgentRoleType getUserRole(Long userId) {
        List<AgentRoleType> roleTypeList = getUserRoleList(userId);
        if (CollectionUtils.isEmpty(roleTypeList)) {
            return null;
        }
        return roleTypeList.get(0);
    }

    public AgentGroupRoleType getGroupRole(Long groupId) {
        AgentGroup group = getGroupById(groupId);
        if (group == null) {
            return null;
        }
        return group.fetchGroupRoleType();
    }

    public List<Long> getManagedJuniorSchoolList(Long userId) {
        List<Long> schoolIdList = this.getManagedSchoolList(userId);
        return getSchoolListByLevel(schoolIdList, SchoolLevel.JUNIOR);
    }

    public List<Long> getManagedMiddleSchoolList(Long userId) {
        List<Long> schoolIdList = this.getManagedSchoolList(userId);
        return this.getSchoolListByLevel(schoolIdList, SchoolLevel.MIDDLE);
    }

    public List<Long> getManagedSchoolList(Long userId, Collection<SchoolLevel> schoolLevels) {
        List<Long> schoolIdList = this.getManagedSchoolList(userId);
        return this.getSchoolListByLevels(schoolIdList, schoolLevels);
    }

    public List<Long> getSchoolListByLevel(Collection<Long> schoolList, SchoolLevel schoolLevel) {
        if (CollectionUtils.isEmpty(schoolList)) {
            return Collections.emptyList();
        }
        List<AgentDictSchool> dictSchoolList = agentDictSchoolService.loadSchoolDictDataBySchool(schoolList);
        if (CollectionUtils.isEmpty(dictSchoolList)) {
            return Collections.emptyList();
        }
        Set<Long> schoolSet = dictSchoolList.stream().filter(p -> p != null && p.getSchoolLevel() == schoolLevel.getLevel()).map(AgentDictSchool::getSchoolId).collect(Collectors.toSet());
        return new ArrayList<>(schoolSet);
    }

    public List<Long> getSchoolListByLevels(Collection<Long> schoolList, Collection<SchoolLevel> schoolLevelList) {
        if (CollectionUtils.isEmpty(schoolList) || CollectionUtils.isEmpty(schoolLevelList)) {
            return Collections.emptyList();
        }
        Set<Integer> levels = schoolLevelList.stream().map(SchoolLevel::getLevel).collect(Collectors.toSet());
        List<AgentDictSchool> dictSchoolList = agentDictSchoolService.loadSchoolDictDataBySchool(schoolList);
        if (CollectionUtils.isEmpty(dictSchoolList)) {
            return Collections.emptyList();
        }

        Set<Long> schoolSet = dictSchoolList.stream().filter(p -> p != null && levels.contains(p.getSchoolLevel())).map(AgentDictSchool::getSchoolId).collect(Collectors.toSet());
        return new ArrayList<>(schoolSet);
    }

    public List<Long> getSchoolListByLevels(Collection<SchoolLevel> schoolLevelList) {
        if (CollectionUtils.isEmpty(schoolLevelList)) {
            return Collections.emptyList();
        }
        Set<Integer> levels = schoolLevelList.stream().map(SchoolLevel::getLevel).collect(Collectors.toSet());
        List<AgentDictSchool> dictSchoolList = baseDictService.loadAllSchoolDictData();
        if (CollectionUtils.isEmpty(dictSchoolList)) {
            return Collections.emptyList();
        }

        Set<Long> schoolSet = dictSchoolList.stream().filter(p -> p != null && levels.contains(p.getSchoolLevel())).map(AgentDictSchool::getSchoolId).collect(Collectors.toSet());
        return new ArrayList<>(schoolSet);
    }

    // 根据地区编码查出所有子级地区并根据 根据地区结果查出所有部门列表（包括父部门与子部门）
    public List<Long> getGroupIdsByRegionCodeIncludeSub(Integer regionCode) {
        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        if (exRegion == null) {
            return Collections.emptyList();
        }
        Set<Integer> regionCodeList = new HashSet<>();
        regionCodeList.add(regionCode);
        if (exRegion.fetchRegionType() == RegionType.CITY) {
            regionCodeList.addAll(exRegion.getChildren().stream().map(ExRegion::getCountyCode).collect(toList()));
        } else if (exRegion.fetchRegionType() == RegionType.PROVINCE) {
            exRegion.getChildren().forEach(p -> {
                regionCodeList.add(p.getCityCode());
                regionCodeList.addAll(p.getChildren().stream().map(ExRegion::getCountyCode).collect(toList()));
            });
        }
        Set<Long> groupIds = new HashSet<>();
        Map<Integer, List<AgentGroupRegion>> regionGroupMap = agentGroupRegionLoaderClient.findByRegionCodes(regionCodeList);
        if (MapUtils.isNotEmpty(regionGroupMap)) {
            for (List<AgentGroupRegion> groupRegionList : regionGroupMap.values()) {
                if (CollectionUtils.isNotEmpty(groupRegionList)) {
                    groupIds.addAll(groupRegionList.stream().map(AgentGroupRegion::getGroupId).collect(Collectors.toList()));
                }
            }
        }
        return new ArrayList<>(groupIds);
    }

    // 获取市级部门没有分配给专员的学校
    public List<Long> getCityManageOtherSchoolByGroupId(Long groupId) {
        AgentGroup group = getGroupById(groupId);
        if (group == null || AgentGroupRoleType.City != group.fetchGroupRoleType()) {
            return Collections.emptyList();
        }
        List<Long> manageSchoolIds = getManagedSchoolListByGroupId(groupId);
        List<Long> userIds = getGroupUserByGroup(group.getId()).stream().filter(p -> p.getUserRoleType() == AgentRoleType.BusinessDeveloper).map(AgentGroupUser::getUserId).collect(Collectors.toList());
        Map<Long, List<AgentUserSchool>> userSchoolMap = getUserSchoolByUsers(userIds);
        List<AgentUserSchool> allSchool = new ArrayList<>();
        userSchoolMap.entrySet().forEach(p -> {
            if (p.getValue() != null) {
                allSchool.addAll(p.getValue());
            }
        });
        Set<Long> bdSchoolIds = allSchool.stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toSet());
        Set<Long> otherSchool = new HashSet<>();
        manageSchoolIds.forEach(p -> {
            if (bdSchoolIds.contains(p)) {
                return;
            }
            otherSchool.add(p);
        });
        return new ArrayList<>(otherSchool);
    }

    public List<Long> fetchGroupUnallocatedSchools(Long groupId, Collection<Integer> schoolLevels) {
        if (CollectionUtils.isEmpty(schoolLevels)) {
            return Collections.emptyList();
        }
        List<Long> schoolIds = getCityManageOtherSchoolByGroupId(groupId);
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }

        List<SchoolLevel> levelList = new ArrayList<>();
        schoolLevels.forEach(p -> {
            SchoolLevel level = SchoolLevel.safeParse(p, null);
            if (level != null) {
                levelList.add(level);
            }
        });
        return getSchoolListByLevels(schoolIds, levelList);
    }

    /**
     * 获取无效的组
     *
     * @param groupId
     * @return
     */
    public AgentGroup loadDisabledGroup(Long groupId) {
        return agentGroupLoaderClient.loadDisabledGroup(groupId);
    }

    /**
     * 获取无效的用户
     *
     * @param userId
     * @return
     */
    public AgentUser loadUnValidUser(Long userId) {
        return agentUserLoaderClient.loadUnValidUser(userId);
    }

    public Set<Long> getSchoolIdsByCityCode(Integer cityCode) {
        ExRegion exRegion = raikouSystem.loadRegion(cityCode);
        if (exRegion == null || CollectionUtils.isEmpty(exRegion.getChildren())) {
            return Collections.emptySet();
        }
        Set<Integer> countyCodes = exRegion.getChildren().stream().map(ExRegion::getCountyCode).collect(toSet());
        return baseDictService.loadAllSchoolDictData().stream().filter(item -> countyCodes.contains(item.getCountyCode())).map(AgentDictSchool::getSchoolId).collect(toSet());
    }

    // 获取用户所在的部门的业务类型
    public List<AgentServiceType> getUserServiceTypes(Long userId) {
        Set<AgentServiceType> serviceTypes = new HashSet<>();
        List<AgentGroup> groupList = getUserGroups(userId);
        if (CollectionUtils.isNotEmpty(groupList)) {
            for (AgentGroup group : groupList) {
                serviceTypes.addAll(group.fetchServiceTypeList());
            }
        }
        return new ArrayList<>(serviceTypes);
    }


    // 获取用户所在业务部的业务类型对应的学校阶段
    public List<SchoolLevel> getUserServiceSchoolLevels(Long userId) {
        List<SchoolLevel> serviceTypes = getUserServiceSchoolLevels(Collections.singleton(userId)).get(userId);
        if (CollectionUtils.isEmpty(serviceTypes)) {
            return new ArrayList<>();
        }
        return serviceTypes;
    }

    public Map<Long, List<SchoolLevel>> getUserServiceSchoolLevels(Collection<Long> userIds) {
        Map<Long, List<SchoolLevel>> resultMap = new HashMap<>();
        if (CollectionUtils.isEmpty(userIds)) {
            return resultMap;
        }
        Map<Long, List<Long>> userGroupIds = getUserGroupIdList(userIds);

        Set<Long> groupIds = new HashSet<>();
        userGroupIds.values().forEach(groupIds::addAll);
        Map<Long, List<SchoolLevel>> groupSchoolLevelMap = getGroupServiceSchoolLevels(groupIds);

        userIds.forEach(u -> {
            Set<SchoolLevel> schoolLevels = new HashSet<>();

            List<Long> groupList = userGroupIds.get(u);
            if (CollectionUtils.isNotEmpty(groupList)) {
                groupList.forEach(g -> {
                    List<SchoolLevel> list = groupSchoolLevelMap.get(g);
                    if (CollectionUtils.isNotEmpty(list)) {
                        schoolLevels.addAll(list);
                    }
                });
            }
            resultMap.put(u, new ArrayList<>(schoolLevels));
        });
        return resultMap;
    }

    public List<SchoolLevel> getGroupServiceSchoolLevels(Long groupId) {
        Map<Long, List<SchoolLevel>> map = getGroupServiceSchoolLevels(Collections.singleton(groupId));
        List<SchoolLevel> resultList = map.get(groupId);
        if (CollectionUtils.isEmpty(resultList)) {
            return new ArrayList<>();
        }
        return resultList;
    }

    public Map<Long, List<SchoolLevel>> getGroupServiceSchoolLevels(Collection<Long> groupIds) {
        List<AgentGroup> groupList = getGroupByIds(groupIds);
        if (CollectionUtils.isEmpty(groupList)) {
            return new HashMap<>();
        }
        Map<Long, List<SchoolLevel>> resultMap = new HashMap<>();
        for (AgentGroup group : groupList) {
            resultMap.put(group.getId(), group.fetchServiceTypeList().stream().map(AgentServiceType::toSchoolLevel).collect(Collectors.toList()));
        }
        return resultMap;
    }


    /**
     * 获取该部门及子部门下所有的用户，并且组装各部门与其所有父级部门之间的等级对应关系
     *
     * @param groupId
     * @param groupDataList 各部门与其所有父级部门之间的等级对应关系列表
     * @return
     */
    public List<AgentGroupUser> getAllGroupUsersByGroupIdWithGroupData(Long groupId, List<GroupData> groupDataList) {
        AgentGroup currentGroup = getGroupById(groupId);
        if (currentGroup == null) {
            return Collections.emptyList();
        }

        List<AgentGroup> groupList = new ArrayList<>();
        groupList.add(currentGroup);
        List<AgentGroup> subGroupList = this.getSubGroupListWithGroupData(groupId, groupDataList);
        if (CollectionUtils.isNotEmpty(subGroupList)) {
            groupList.addAll(subGroupList);
        }
        List<Long> groupIdList = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
        return getGroupUserByGroups(groupIdList);
    }

    /**
     * 递归获取指定部门下面的所有子部门，并且组装各部门与其所有父级部门之间的等级对应关系
     *
     * @param groupId
     * @param groupDataList 各部门与其所有父级部门之间的等级对应关系列表
     * @return
     */
    public List<AgentGroup> getSubGroupListWithGroupData(Long groupId, List<GroupData> groupDataList) {
        AgentGroup group = getGroupById(groupId);
        if (null == group) {
            return new ArrayList<>();
        }
        //如果groupDataList中存在该部门父级部门，则先把父级部门信息复制到本部门信息，再设置本部门信息
        GroupData groupData = new GroupData();
        Map<Long, GroupData> groupDataMap = groupDataList.stream().collect(Collectors.toMap(GroupData::getGroupId, Function.identity(), (o1, o2) -> o1));
        try {
            Long parentGroupId = group.getParentId();
            GroupData parentGroupData = groupDataMap.get(parentGroupId);
            if (null != parentGroupData) {
                groupData = (GroupData) BeanUtils.cloneBean(parentGroupData);
            }
        } catch (Exception e) {

        }
        //再设置本部门信息
        groupData.setGroupId(groupId);
        Integer roleId = group.getRoleId();
        groupData.setRoleId(roleId);
        if (AgentGroupRoleType.of(roleId) == AgentGroupRoleType.Marketing) {
            groupData.setMarketingId(groupId);
            groupData.setMarketingName(group.getGroupName());
        }
        if (AgentGroupRoleType.of(roleId) == AgentGroupRoleType.Region) {
            groupData.setRegionId(groupId);
            groupData.setRegionName(group.getGroupName());
        }
        if (AgentGroupRoleType.of(roleId) == AgentGroupRoleType.Area) {
            groupData.setAreaId(groupId);
            groupData.setAreaName(group.getGroupName());
        }
        if (AgentGroupRoleType.of(roleId) == AgentGroupRoleType.City) {
            groupData.setCityId(groupId);
            groupData.setCityName(group.getGroupName());
        }
        groupDataList.add(groupData);


        List<AgentGroup> groupList = new ArrayList<>();
        List<AgentGroup> subGroupList = getGroupListByParentId(groupId);
        if (CollectionUtils.isEmpty(subGroupList)) {
            return groupList;
        }
        groupList.addAll(subGroupList);

        for (AgentGroup agentGroup : subGroupList) {
            List<AgentGroup> tempList = getSubGroupListWithGroupData(agentGroup.getId(), groupDataList);
            if (CollectionUtils.isNotEmpty(tempList)) {
                groupList.addAll(tempList);
            }
        }
        return groupList;
    }

    /**
     * @param groupIdss  当前用户所在部门
     * @param regionCode 选择地区编码(区级)
     * @return
     */
    public List<AgentUser> findUserByReginCode(List<Long> groupIdss, Integer regionCode) {
        //当前部门下包含的部门
        Set<Long> agentGroups = getGroupByIds(groupIdss).stream().map(AgentGroup::getId).collect(Collectors.toSet());
        agentGroups.addAll(groupIdss);
        //当前区域包含的部门id
        List<Long> groupIds = getGroupIdsByRegionCodeIncludeSub(regionCode);
        //过滤出所在地区包含的部门id 且同时在用户所在部门下的部门id
        Set<Long> needGroupId = new HashSet<>();
        groupIds.forEach(p -> {
            if (agentGroups.contains(p)) needGroupId.add(p);
        });
        //同时有自己部门和父级部门把父级部门过滤掉
        List<AgentGroup> agentGroupList = getGroupByIds(needGroupId);
//        Map<Long,List<AgentGroup>> map = agentGroupList.stream().collect(Collectors.groupingBy(AgentGroup :: getId,Collectors.toList()));
//        Set<Long> finalGroupIds = agentGroupList.stream().filter(p -> !map.keySet().contains(p.getParentId())).map(AgentGroup :: getId).collect(Collectors.toSet());

        List<Long> users = getGroupUserByGroups(agentGroupList.stream().map(AgentGroup::getId).collect(toList())).stream().filter(p -> p.getUserRoleType() == AgentRoleType.BusinessDeveloper).map(AgentGroupUser::getUserId).collect(Collectors.toList());
        return getUsers(users);
    }

    /**
     * 省社区三级 构建树  按字母分组
     *
     * @param regions
     * @return
     */
    public List<Map<String, Object>> createRegionTreeGroupByFLetter(Collection<ExRegion> regions) {
        if (CollectionUtils.isEmpty(regions)) {
            return Collections.emptyList();
        }
        Map<String, List<ExRegion>> map = regions.stream().collect(Collectors.groupingBy(p -> Pinyin4jUtils.getFirstCapital(p.getProvinceName())));
        List<Map<String, Object>> resultList = new ArrayList<>();
        map.forEach((k, v) -> {
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> provinceList = new ArrayList<>();
            MapMessage mapMessage = createRegionTree(v, 3, Collections.emptyList());
            resultMap.put("key", k);
            List<NodeStructure> proviceList = (List<NodeStructure>) mapMessage.get("nodeList");
            proviceList.forEach(pro -> {
                List<Map<String, Object>> cityResult = new ArrayList<>();
                List<NodeStructure> cityList = pro.getSubNodes();
                Map<String, List<NodeStructure>> cityMap = cityList.stream().collect(Collectors.groupingBy(county -> Pinyin4jUtils.getFirstCapital(county.getName())));
                cityMap.forEach((cityKey, cityVal) -> {
                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("key", cityKey);
                    List<Map<String, Object>> letterCityList = new ArrayList<>();
                    cityVal.forEach(city -> {
                        List<Map<String, Object>> countyResult = new ArrayList<>();
                        List<NodeStructure> countyList = city.getSubNodes();
                        Map<String, List<NodeStructure>> countyMap = countyList.stream().collect(Collectors.groupingBy(county -> Pinyin4jUtils.getFirstCapital(county.getName())));
                        countyMap.forEach((counkey, counval) -> {
                            List<Map<String, Object>> letterCountyList = new ArrayList<>();
                            counval.forEach(county -> {
                                Map<String, Object> map4 = createSubNode(county, null);
                                letterCountyList.add(map4);
                            });
                            Map<String, Object> map3 = new HashMap<>();
                            map3.put("key", counkey);
                            map3.put("region", letterCountyList);
                            countyResult.add(map3);
                        });
                        Map<String, Object> cityMap1 = createSubNode(city, countyResult);
                        letterCityList.add(cityMap1);
                    });
                    map2.put("region", letterCityList);
                    cityResult.add(map2);
                });

                Map<String, Object> proMap = createSubNode(pro, cityResult);
                provinceList.add(proMap);
            });

            resultMap.put("region", provinceList);
            resultList.add(resultMap);
        });
        return resultList;
    }

    public Map<String, Object> createSubNode(NodeStructure nodeStructure, List<Map<String, Object>> list) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", nodeStructure.getId());
        map.put("name", nodeStructure.getName());
        map.put("region", list);
        return map;
    }

    public List<AgentGroup> getDirectAndParentGroupList(Long userId) {
        List<AgentGroup> resultList = new ArrayList<>();
        List<AgentGroup> directGroupList = getUserGroups(userId);
        if (CollectionUtils.isNotEmpty(directGroupList)) {
            resultList.addAll(directGroupList);
            for (AgentGroup group : directGroupList) {
                AgentGroup currentGroup = group;
                while (true) {
                    AgentGroup parentGroup = getParentGroup(currentGroup.getParentId());
                    if (parentGroup == null) {
                        break;
                    }
                    resultList.add(parentGroup);
                    currentGroup = parentGroup;
                }
            }
        }
        return resultList;
    }

    // 获取当前部门的所有上级部门
    public Set<AgentGroup> getAllParentGroupByGroupId(Long groupId) {
        Set<AgentGroup> groupList = new HashSet<>();
        boolean flag = true;
        while (flag) {
            AgentGroup agentGroup = getParentGroup(groupId);
            if (agentGroup != null) {
                groupList.add(agentGroup);
                groupId = agentGroup.getParentId();
            } else {
                flag = false;
            }
        }
        return groupList;
    }

    /**
     * 获取当前部门所有父级部门到，直到市场部结束
     *
     * @param groupId
     * @return
     */
    public Set<AgentGroup> getAllParentGroup(final Long groupId) {
        Objects.requireNonNull(groupId);
        Set<AgentGroup> allGroup = new HashSet<>();
        Long tempGroupId = groupId;
        while (true) {
            AgentGroup agentGroup = getGroupById(tempGroupId);
            if (null != agentGroup) {
                allGroup.add(agentGroup);
                if (Objects.equals(AgentGroupRoleType.Country, agentGroup.fetchGroupRoleType()) || null == agentGroup.getParentId() || agentGroup.getParentId() <= 0) {
                    break;
                }
                tempGroupId = agentGroup.getParentId();
            } else {
                break;
            }
        }
        return allGroup;
    }

    /**
     * 获取dimension列表
     *
     * @param id
     * @param idType
     * @return
     */
    public List<Map<String, Object>> fetchDimensionList(Long id, Integer idType) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)) {
            Map<String, Object> defaultItem = new HashMap<>();
            defaultItem.put("code", 1);
            defaultItem.put("desc", "默认");
            resultList.add(defaultItem);

            List<AgentGroup> groupList = new ArrayList<>();
            AgentGroup group = getGroupById(id);
            if (group == null) {
                return resultList;
            }
            groupList.add(group);
            groupList.addAll(getSubGroupList(id));
            Set<AgentGroupRoleType> groupRoleTypes = groupList.stream().filter(Objects::nonNull).map(AgentGroup::fetchGroupRoleType)
                    .filter(p -> p != null && (p == AgentGroupRoleType.Region || p == AgentGroupRoleType.Area || p == AgentGroupRoleType.City))
                    .collect(Collectors.toSet());

            if (groupRoleTypes.contains(AgentGroupRoleType.Region)) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("code", 2);
                itemMap.put("desc", "大区");
                resultList.add(itemMap);
            }
            if (groupRoleTypes.contains(AgentGroupRoleType.Area)) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("code", 3);
                itemMap.put("desc", "区域");
                resultList.add(itemMap);
            }
            if (groupRoleTypes.contains(AgentGroupRoleType.City)) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("code", 4);
                itemMap.put("desc", "分区");
                resultList.add(itemMap);
            }

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("code", 5);
            itemMap.put("desc", "专员");
            resultList.add(itemMap);
        } else if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("code", 5);
            itemMap.put("desc", "专员");
            resultList.add(itemMap);
        }

        return resultList;
    }
}
