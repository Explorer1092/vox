package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * BuildPerformanceCacheHandler
 *
 * @author song.wang
 * @date 2017/11/20
 */
@Named
public class BuildPerformanceCacheHandler extends SpringContainerSupport {

    @Inject private BaseOrgService baseOrgService;

    public void handle() {
        AgentGroup group = baseOrgService.getGroupByName("市场部");
//        Integer day = performanceService.lastSuccessDataDay();
//
//        // 生成部门数据
//        basePerformanceService.loadGroupData(group.getId(), day);
//
//        // 生成市场部下城市数据
//        performanceService.generateGroupCityPerformanceDataList(group.getId(), group.fetchGroupRoleType(), day);
//
//        List<AgentGroup> cityGroupList = baseOrgService.getSubGroupList(group.getId()).stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
//        cityGroupList.forEach(p -> {
//            // 生成分区下专员及未分配数据
//            performanceService.generateGroupUserPerformanceDataList(p.getId(), p.fetchGroupRoleType(), AgentRoleType.BusinessDeveloper, day);
//
//            // 生成分区的城市数据
//            performanceService.generateGroupCityPerformanceDataList(p.getId(), p.fetchGroupRoleType(), day);
//        });
//        initSchoolMauIncreaseStatistics(group.getId());
    }

//    /**
//     * 初始化学校增长情况缓存数据
//     * @param baseGroupId
//     */
//    private void initSchoolMauIncreaseStatistics(Long baseGroupId){
//        schoolMauIncreaseStatisticsService.generateGroupCategoryMap(baseGroupId);
//        schoolMauIncreaseStatisticsService.generateGroupCityCategoryMap(baseGroupId);
//        schoolMauIncreaseStatisticsService.generateGroupUserCategoryMap(baseGroupId, AgentRoleType.BusinessDeveloper);
//    }
}
