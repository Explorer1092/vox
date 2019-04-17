package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.bean.AgentGroupHeadCountInfo;
import com.voxlearning.utopia.agent.bean.AgentGroupRegionInfo;
import com.voxlearning.utopia.agent.bean.honeycomb.HoneycombUserData;
import com.voxlearning.utopia.agent.constants.AgentCityLevelType;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialBudget;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialCost;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.honeycomb.HoneycombUserService;
import com.voxlearning.utopia.agent.service.material.AgentMaterialBudgetService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentCityLevelService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 我的-个人信息service
 *
 * @author deliang.che
 * @date 2018-04-26
 **/
@Named
public class AgentPersonalInfoService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AgentMaterialBudgetService agentMaterialBudgetService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private BaseUserService baseUserService;
    @Inject
    private AgentCityLevelService agentCityLevelService;
    @Inject
    private HoneycombUserService honeycombUserService;

    /**
     * 获取个人信息详情
     *
     * @param userId
     * @return
     */
    public Map<String, Object> userDetail(Long userId) {
        Map<String, Object> dataMap = new HashMap<>();
        AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
        if (null != groupUser) {
            Long groupId = groupUser.getGroupId();
            AgentRoleType userRoleType = groupUser.getUserRoleType();
            AgentGroup group = baseOrgService.getGroupById(groupId);
            if (null != group) {
                String groupName = group.getGroupName();
                AgentUser agentUser = baseUserService.getById(userId);
                if (null == agentUser) {
                    return MapMessage.errorMessage("用户信息不存在,请刷新页面");
                }
                dataMap.put("realName", agentUser.getRealName());
                dataMap.put("accountName", agentUser.getAccountName());
                dataMap.put("tel", agentUser.getTel());
                dataMap.put("workingDayNum", agentUser.getContractStartDate() != null ? DateUtils.dayDiff(new Date(), agentUser.getContractStartDate()) : 0); //在职天数=当前日期-合同开始日期
                AgentMaterialCost userMaterialCost = agentMaterialBudgetService.getUserMaterialCostByUserId(userId);
                if (null != userMaterialCost) {
                    dataMap.put("materialCostId", userMaterialCost.getId());//物料ID
                    dataMap.put("userMaterialBalance", userMaterialCost.getBalance());   //可用余额
                } else {
                    dataMap.put("userMaterialBalance", 0);   //可用余额
                }
                dataMap.put("isBusinessDeveloper", userRoleType == AgentRoleType.BusinessDeveloper);
                dataMap.put("groupId", groupId);
                dataMap.put("groupName", groupName);

                List<Long> honeycombUserIds = honeycombUserService.getHoneycombUserIds(agentUser.getId());
                if (CollectionUtils.isNotEmpty(honeycombUserIds)) {
                    Long honeycombId = honeycombUserIds.get(0);
                    dataMap.put("honeycombId", honeycombId);
                    List<HoneycombUserData> honeycombUserList = honeycombUserService.getHoneycombUserData(Collections.singleton(honeycombId));
                    if (CollectionUtils.isNotEmpty(honeycombUserList)) {
                        dataMap.put("honeycombMobile", honeycombUserList.get(0).getMobile());
                    }
                }
            }
        }
        return dataMap;
    }

    /**
     * 部门详情
     *
     * @param groupId
     * @return
     */
    public Map<String, Object> departmentDetail(Long groupId) {
        Map<String, Object> dataMap = new HashMap<>();
        //获取部门详情
        AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
        if (agentGroup == null) {
            return dataMap;
        }
        //应招
        Integer headCount = getGroupHeadCount(agentGroup);
        dataMap.put("headCount", headCount);
        //实际在岗
        Integer actualCount = baseOrgService.getAllSubGroupUsersByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId()).size();
        dataMap.put("actualCount", actualCount);
        //满编率
        AgentGroupHeadCountInfo agentGroupHeadCountInfo = new AgentGroupHeadCountInfo();
        agentGroupHeadCountInfo.setHeadCount(headCount);
        agentGroupHeadCountInfo.setActuallyCount(actualCount);
        dataMap.put("fullRate", agentGroupHeadCountInfo.getActuallyRate());

        dataMap.put("isSubregion", agentGroup.fetchGroupRoleType() == AgentGroupRoleType.City);

        dataMap.put("groupName", agentGroup.getGroupName());
        List<AgentGroupRegion> agentGroupRegionList = baseOrgService.getGroupRegionByGroup(groupId);
        List<AgentGroupRegionInfo> agentGroupRegionInfoList = createAgentGroupRegionInfo(agentGroupRegionList, groupId);
        //如果部门级别是分区
        if (agentGroup.fetchGroupRoleType() == AgentGroupRoleType.City) {
            dataMap.put("groupRegionInfoList", agentGroupRegionInfoList);
        } else {
            dataMap.put("cityNum", agentGroupRegionInfoList.size());
        }
        AgentGroupRoleType groupType = AgentGroupRoleType.of(agentGroup.getRoleId());
        dataMap.put("groupType", groupType == null ? "" : groupType.getRoleName());
        dataMap.put("latest6MonthCityBudgetData", getLatest6MonthCityBudgetData(agentGroup));

        //部门物料费用
        AgentMaterialCost groupMaterialCost = agentMaterialBudgetService.getGroupMaterialCostByGroupId(groupId);
        if (null != groupMaterialCost) {
            Double userMaterialBalance = agentMaterialBudgetService.getUserMaterialBalanceByGroupIdAndAchoolTerm(groupMaterialCost.getGroupId(), groupMaterialCost.getSchoolTerm());
            dataMap.put("materialCostId", groupMaterialCost.getId());
            //部门未分配余额
            Double undistributedCost = groupMaterialCost.getUndistributedCost() == null ? 0 : groupMaterialCost.getUndistributedCost();
            //部门物料余额=人员余额+部门未分配余额
            dataMap.put("groupBalance", MathUtils.doubleAdd(userMaterialBalance, undistributedCost));
        } else {
            dataMap.put("materialCostId", "");
            dataMap.put("groupBalance", 0);
        }

        return dataMap;
    }

    /**
     * 部门负责的区域列表
     *
     * @param agentGroupRegionList
     * @return
     */
    public List<AgentGroupRegionInfo> createAgentGroupRegionInfo(List<AgentGroupRegion> agentGroupRegionList, Long groupId) {
        if (CollectionUtils.isEmpty(agentGroupRegionList)) {
            return Collections.emptyList();
        }

        List<Integer> regionCodeList = agentGroupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toList());
        //获取所有区域信息
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionCodeList);
        Set<Integer> cityCodes = new HashSet<>();
        exRegionMap.values().forEach(p -> {
            if (p.fetchRegionType() == RegionType.PROVINCE) {
                cityCodes.addAll(p.getChildren().stream().filter(t -> t.fetchRegionType() == RegionType.CITY).map(ExRegion::getId).collect(Collectors.toList()));
            } else if (p.fetchRegionType() == RegionType.CITY) {
                cityCodes.add(p.getId());
            } else if (p.fetchRegionType() == RegionType.CITY) {
                cityCodes.add(p.getCityCode());
            }
        });

        //过滤出区域等级为“市”的区域
        Map<Integer, AgentCityLevelType> cityLevelTypeMap = agentCityLevelService.loadCityLevelTypeMap(cityCodes);


        List<AgentGroupRegionInfo> agentGroupRegionInfoList = new ArrayList<>();
        //获取部门详情
        AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
        if (null != agentGroup) {
            cityCodes.forEach(item -> {
                AgentGroupRegionInfo agentGroupRegionInfo = new AgentGroupRegionInfo();

                ExRegion exRegion = exRegionMap.get(item);
                if (exRegion == null) {
                    exRegion = raikouSystem.loadRegion(item);
                }

                if (exRegion != null) {
                    agentGroupRegionInfo.setProvinceName(exRegion.getProvinceName());
                    agentGroupRegionInfo.setCityName(exRegion.getCityName());
                    if (cityLevelTypeMap.containsKey(item)) {
                        agentGroupRegionInfo.setCityLevel(cityLevelTypeMap.get(item).getValue());
                    } else {
                        agentGroupRegionInfo.setCityLevel("");
                    }

                    List<ExRegion> countyList = exRegion.getChildren().stream().filter(r -> regionCodeList.contains(r.getId())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(countyList)) {
                        String countyName = countyList.stream().map(ExRegion::getName).reduce((x, y) -> StringUtils.join(x, "、", y)).get();
                        agentGroupRegionInfo.setCountyName(countyName);
                    }

                    agentGroupRegionInfoList.add(agentGroupRegionInfo);
                }
            });
        }
        return agentGroupRegionInfoList;
    }

    /**
     * 近6月城市支持费用余额
     *
     * @param agentGroup
     * @return
     */
    private Double getLatest6MonthCityBudgetData(AgentGroup agentGroup) {
        Set<Long> groupIds = new HashSet<>();
        AgentGroupRoleType groupType = AgentGroupRoleType.of(agentGroup.getRoleId());
        if (AgentGroupRoleType.City.equals(groupType)) {
            groupIds.add(agentGroup.getId());
        } else {
            Set<Long> groupIdSet = baseOrgService.getSubGroupList(agentGroup.getId()).stream().filter(item -> AgentGroupRoleType.City.getId().equals(item.getRoleId())).map(AgentGroup::getId).collect(Collectors.toSet());
            groupIds.addAll(groupIdSet);
        }
        List<AgentMaterialBudget> latest6MonthCityBudgetList = agentMaterialBudgetService.getLatest6MonthCityBudget(groupIds);
        double balance = 0d;
        for (int i = 0; i < latest6MonthCityBudgetList.size(); i++) {
            balance = MathUtils.doubleAdd(balance, latest6MonthCityBudgetList.get(i).getBalance());
        }
        return balance;
    }

    /**
     * 获取专员情况列表
     *
     * @param groupId
     * @return
     */
    public List<AgentGroupHeadCountInfo> businessDeveloperList(Long groupId) {
        List<AgentGroupHeadCountInfo> hcInfoList = new ArrayList<>();
        List<AgentGroup> agentGroupList = new ArrayList<>();
        AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
        //如果所属部门级别是“分区”
        if (null != agentGroup && agentGroup.fetchGroupRoleType() == AgentGroupRoleType.City) {
            agentGroupList.add(agentGroup);
        } else {
            //获取该部门所有级别是“分区”的子部门
            List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(groupId).stream().filter(item -> null != item && item.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
            agentGroupList.addAll(subGroupList);
        }
        agentGroupList.forEach(item -> {
            AgentGroupHeadCountInfo hcInfo = new AgentGroupHeadCountInfo();
            hcInfo.setGroupName(item.getGroupName());
            hcInfo.setHeadCount(item.getHeadCount() != null ? item.getHeadCount() : 0);
            Integer actuallyCount = baseOrgService.getAllSubGroupUsersByGroupIdAndRole(item.getId(), AgentRoleType.BusinessDeveloper.getId()).size();
            hcInfo.setActuallyCount(actuallyCount);
            hcInfoList.add(hcInfo);
        });
        //按照待招专员数由高到低排序
        List<AgentGroupHeadCountInfo> dataList = hcInfoList.stream().sorted(Comparator.comparing(AgentGroupHeadCountInfo::getWaitingCount).reversed()).collect(Collectors.toList());
        return dataList;
    }

    /**
     * 获取部门应招专员（HeadCount）
     *
     * @param agentGroup
     * @return
     */
    private Integer getGroupHeadCount(AgentGroup agentGroup) {
        Integer headCount = 0;
        if (null != agentGroup) {
            if (agentGroup.fetchGroupRoleType() == AgentGroupRoleType.City) {
                if (null != agentGroup.getHeadCount()) {
                    headCount = agentGroup.getHeadCount();
                }
            } else {
                List<AgentGroup> agentSubGroupList = baseOrgService.getSubGroupList(agentGroup.getId()).stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
                for (int i = 0; i < agentSubGroupList.size(); i++) {
                    AgentGroup groupTemp = agentSubGroupList.get(i);
                    if (groupTemp.getHeadCount() != null) {
                        headCount += groupTemp.getHeadCount();
                    }
                }
            }
        }
        return headCount;
    }
}
