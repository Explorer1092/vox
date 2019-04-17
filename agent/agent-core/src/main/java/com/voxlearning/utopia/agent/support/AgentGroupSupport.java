package com.voxlearning.utopia.agent.support;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.bean.group.GroupWithParent;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupRegionLoaderClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author song.wang
 * @date 2018/9/18
 */
@Named
public class AgentGroupSupport {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AgentGroupLoaderClient agentGroupLoaderClient;
    @Inject
    private AgentGroupRegionLoaderClient agentGroupRegionLoaderClient;
    @Inject
    private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;
    @Inject
    private BaseOrgService baseOrgService;

    public Map<Long, GroupWithParent> generateGroupWithParentByIds(Collection<Long> groupIds) {
        Map<Long, AgentGroup> groupMap = agentGroupLoaderClient.loads(groupIds);
        if (MapUtils.isEmpty(groupMap)) {
            return Collections.emptyMap();
        }
        return generateGroupWithParent(groupMap.values());
    }

    public Map<Long, GroupWithParent> generateGroupWithParent(Collection<AgentGroup> groups) {
        if (CollectionUtils.isEmpty(groups)) {
            return Collections.emptyMap();
        }
        Map<Long, AgentGroup> cachedMap = new HashMap<>();
        cachedMap.putAll(groups.stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity(), (o1, o2) -> o1)));

        Map<Long, GroupWithParent> resultMap = new HashMap<>();
        groups.forEach(p -> {
            GroupWithParent groupWithParent = generateGroupWithParent(p, cachedMap);
            if (groupWithParent != null) {
                resultMap.put(groupWithParent.getId(), groupWithParent);
            }
        });
        return resultMap;
    }

    private GroupWithParent generateGroupWithParent(AgentGroup group, Map<Long, AgentGroup> cachedMap) {
        if (group == null) {
            return null;
        }
        GroupWithParent groupWithParent = GroupWithParent.Builder.build(group);
        AgentGroup parentGroup = cachedMap.get(groupWithParent.getParentId());
        if (parentGroup == null) {
            parentGroup = agentGroupLoaderClient.load(groupWithParent.getParentId());
            if (parentGroup != null) {
                cachedMap.put(parentGroup.getId(), parentGroup);
            }
        }
        GroupWithParent parent = generateGroupWithParent(parentGroup, cachedMap);
        groupWithParent.setParent(parent);
        return groupWithParent;
    }

    public GroupWithParent generateGroupWithParent(Long groupId) {
        AgentGroup group = agentGroupLoaderClient.load(groupId);
        return generateGroupWithParent(group);
    }

    private GroupWithParent generateGroupWithParent(AgentGroup group) {
        return generateGroupWithParent(group, new HashMap<>());
    }


    /**
     * 获取负责该学校的市场部中的部门
     *
     * @param schoolId 学校ID
     * @return 负责该学校的市场部下的部门ID
     */
    public List<Long> getMarketGroupIdsBySchool(Long schoolId) {
        return getGroupIdsBySchool(schoolId, AgentGroupRoleType.getMarketRoleList());
    }

    /**
     * 获取负责该学校的指定角色最小粒度的部门
     *
     * @param schoolId          学校列表
     * @param groupRoleTypeList 部门角色列表
     * @return
     */
    public List<Long> getGroupIdsBySchool(Long schoolId, Collection<AgentGroupRoleType> groupRoleTypeList) {
        if (CollectionUtils.isEmpty(groupRoleTypeList)) {
            return Collections.emptyList();
        }
        List<Long> groupIds = getAllGroupIdsBySchool(schoolId);
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        List<Long> targetIds = agentGroupLoaderClient.loads(groupIds).values().stream().filter(p -> groupRoleTypeList.contains(p.fetchGroupRoleType())).map(AgentGroup::getId).collect(Collectors.toList());
        return removeParentGroup(targetIds);
    }

    /**
     * 获取负责指定学校的最小粒度所有部门列表（只要业务范围覆盖改学校的最小粒度的部门都会返回）， 包括市场部，渠道等部门
     * 如果一个分区负责该学校， 则该分区的上级部门肯定负责该学校，但只返回最小粒度的部门--- 分区
     *
     * @param schoolId 学校ID
     * @return 负责该学校的最小粒度的部门列表
     */
    public List<Long> getGroupIdsBySchool(Long schoolId) {
        // 获取负责该学校的所有部门列表（包括子部门和父部门）
        List<Long> targetGroupIds = getAllGroupIdsBySchool(schoolId);

        // 去掉存在父子关系的上级部门，返回负责该区域的最小粒度的部门
        return removeParentGroup(targetGroupIds);
    }


    /**
     * 获取负责指定学校的所有部门列表（包括子部门和父部门）
     *
     * @param schoolId 学校ID
     * @return 部门列表
     */
    public List<Long> getAllGroupIdsBySchool(Long schoolId) {
        AgentDictSchool dictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
        if (dictSchool == null) {
            return Collections.emptyList();
        }
        Integer countyCode = dictSchool.getCountyCode();
        return getGroupIdsByRegionCodeAndSchoolLevels(countyCode, Collections.singleton(dictSchool.getSchoolLevel()));
    }

    /**
     * 获取负责指定区域，指定学校阶段，指定部门角色 的最小粒度部门列表
     *
     * @param regionCode        区域Code
     * @param schoolLevels      学校阶段列表
     * @param groupRoleTypeList 部门角色列表
     * @return 部门列表
     */
    public List<Long> getGroupIdsByRegionCodeAndSchoolLevels(Integer regionCode, Collection<Integer> schoolLevels, Collection<AgentGroupRoleType> groupRoleTypeList) {
        List<Long> groupIds = getGroupIdsByRegionCodeAndSchoolLevels(regionCode, schoolLevels);
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        List<Long> targetIds = agentGroupLoaderClient.loads(groupIds).values().stream().filter(p -> groupRoleTypeList.contains(p.fetchGroupRoleType())).map(AgentGroup::getId).collect(Collectors.toList());
        return removeParentGroup(targetIds);
    }


    /**
     * 获取负责指定区域，指定学校阶段的所有部门列表（包括子部门和父部门）
     *
     * @param regionCode   区域Code
     * @param schoolLevels 学校阶段列表
     * @return 部门列表
     */
    public List<Long> getGroupIdsByRegionCodeAndSchoolLevels(Integer regionCode, Collection<Integer> schoolLevels) {
        if (CollectionUtils.isEmpty(schoolLevels)) {
            return Collections.emptyList();
        }
        // 获取区域范围覆盖改区域的所有部门（包括子部门和父部门）
        List<Long> groupIds = getGroupIdsByRegionCode(regionCode);

        // 根据业务类型过滤出负责指定学校阶段的部门
        return agentGroupLoaderClient.loads(groupIds).values().stream().filter(p -> {
            List<Integer> schoolLevelList = p.fetchServiceTypeList().stream().map(AgentServiceType::toSchoolLevel).map(SchoolLevel::getLevel).collect(Collectors.toList());
            if (schoolLevelList.stream().anyMatch(schoolLevels::contains)) {
                return true;
            }
            return false;
        }).map(AgentGroup::getId).collect(Collectors.toList());
    }


    /**
     * 获取负责该区域的所有部门列表（包括父部门与子部门）， 仅仅是区域覆盖
     *
     * @param regionCode 区域code
     * @return 部门列表
     */
    public List<Long> getGroupIdsByRegionCode(Integer regionCode) {
        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        if (exRegion == null) {
            return Collections.emptyList();
        }
        Set<Integer> regionCodeList = new HashSet<>();
        regionCodeList.add(regionCode);
        if (exRegion.fetchRegionType() == RegionType.COUNTY) {
            regionCodeList.add(exRegion.getCityCode());
            regionCodeList.add(exRegion.getProvinceCode());
        } else if (exRegion.fetchRegionType() == RegionType.CITY) {
            regionCodeList.add(exRegion.getProvinceCode());
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

    private List<AgentGroup> getParentGroupList(Long groupId) {
        List<AgentGroup> groupList = new ArrayList<>();
        AgentGroup currentGroup = agentGroupLoaderClient.load(groupId);
        while (true) {
            AgentGroup parentGroup = agentGroupLoaderClient.load(currentGroup.getParentId());
            if (parentGroup != null) {
                groupList.add(parentGroup);
                currentGroup = parentGroup;
            } else {
                break;
            }
        }
        return groupList;
    }


    /**
     * 去掉groupIds中存在上下级关系的父部门，仅留最小粒度的子部门
     *
     * @param groupIds 部门ID列表
     * @return 最小粒度的部门ID列表
     */
    public List<Long> removeParentGroup(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        Set<Long> groupIdSet = new HashSet<>(groupIds);
        groupIds.forEach(groupId -> {
            if (groupIdSet.contains(groupId)) {
                List<AgentGroup> parentGroupList = getParentGroupList(groupId);
                if (CollectionUtils.isNotEmpty(parentGroupList)) {
                    for (AgentGroup group : parentGroupList) {
                        groupIdSet.remove(group.getId());
                    }
                }
            }
        });
        return new ArrayList<>(groupIdSet);
    }

    /**
     * 去掉groupIds中存在上下级关系的子部门，仅留最大粒度的部门
     *
     * @param groupIds 部门ID列表
     * @return 最大粒度的部门ID列表
     */
    public List<Long> removeChildGroup(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }

        Set<Long> groupIdSet = new HashSet<>(groupIds);
        groupIds.forEach(groupId -> {
            if (groupIdSet.contains(groupId)) {
                AgentGroup group = baseOrgService.getGroupById(groupId);
                if (group == null) {
                    groupIdSet.remove(groupId);
                } else {
                    List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(groupId);
                    if (CollectionUtils.isNotEmpty(subGroupList)) {
                        for (AgentGroup subGroup : subGroupList) {
                            groupIdSet.remove(subGroup.getId());
                        }
                    }
                }
            }
        });
        return new ArrayList<>(groupIdSet);
    }
}
