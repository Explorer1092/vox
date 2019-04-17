package com.voxlearning.utopia.agent.controller.mobile.workrecord;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.listener.handler.AgentRegisterTeacherStatisticsHandler;
import com.voxlearning.utopia.agent.listener.handler.AgentWorkRecordStatisticsHandler;
import com.voxlearning.utopia.agent.persist.entity.AgentWorkRecordStatistics;
import com.voxlearning.utopia.agent.persist.entity.AgentWorkRecordStatisticsRoleData;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.AgentRegisterTeacherStatisticsService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.AgentWorkRecordStatisticsService;
import com.voxlearning.utopia.agent.service.taskmanage.AgentTaskCenterService;
import com.voxlearning.utopia.agent.view.workrecord.WrStatisticsGroupData;
import com.voxlearning.utopia.agent.view.workrecord.WrStatisticsOverview;
import com.voxlearning.utopia.agent.view.workrecord.WrStatisticsOverviewRoleData;
import com.voxlearning.utopia.agent.view.workrecord.WrStatisticsUserData;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作量统计
 *
 * @author chunlin.yu
 * @create 2018-01-23 15:29
 **/

@Controller
@RequestMapping("/mobile/work_record/statistics")
public class WorkRecordStatisticsController extends AbstractAgentController {

    @Inject
    private AgentWorkRecordStatisticsService agentWorkRecordStatisticsService;
    @Inject
    BaseOrgService baseOrgService;

    @Inject
    WorkRecordService workRecordService;
    @Inject
    private AgentTaskCenterService agentTaskCenterService;
    @Inject
    private AgentWorkRecordStatisticsHandler agentWorkRecordStatisticsHandler;

    @Inject
    private AgentRegisterTeacherStatisticsService agentRegisterTeacherStatisticsService;

    @Inject private AgentRegisterTeacherStatisticsHandler agentRegisterTeacherStatisticsHandler;
//    /**
//     * 总计信息Controller
//     */
//    @RequestMapping(value = "work_record_summary.vpage", method = RequestMethod.GET)
//    public String visit(Model model) {
//        return "rebuildViewDir/mobile/workRecord/work_record_summary";
//    }
//
//    /**
//     * 总计信息
//     */
//    @RequestMapping(value = "summary.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage getSummary() {
//        // 日期类型  1： 日  2：周  3：月
//        int dateType = getRequestInt("dateType", 1);
//        int date = getRequestInt("date");
//        date = formatDate(date, dateType);
//        if (date <= 0) {
//            return MapMessage.errorMessage("传入的日期不正确或者与日期类型不匹配");
//        }
//        AuthCurrentUser currentUser = getCurrentUser();
//        MapMessage result = MapMessage.successMessage();
//        //当是全国总监或者大区经理或者区域经理或者市经理身份时，获取所在部门统计
//        if (currentUser.isCountryManager() || currentUser.isRegionManager() || currentUser.isCityManager()|| currentUser.isAreaManager()){
//            List<AgentGroupUser> groupUserByUser = baseOrgService.getGroupUserByUser(currentUser.getUserId());
//            if (CollectionUtils.isEmpty(groupUserByUser)) {
//                return MapMessage.errorMessage();
//            }
//            Long groupId = groupUserByUser.get(0).getGroupId();
//            Map<Long, AgentWorkRecordStatistics> groupHistoryStatisticsMap = agentWorkRecordStatisticsService.getGroupStatistics(Collections.singleton(groupId), date, dateType);
//
//            WorkRecordStatisticsSummary workRecordStatisticsSummary = new WorkRecordStatisticsSummary();
//            if (MapUtils.isNotEmpty(groupHistoryStatisticsMap) && groupHistoryStatisticsMap.containsKey(groupId)) {
//                AgentWorkRecordStatistics agentWorkRecordStatistics = groupHistoryStatisticsMap.get(groupId);
//                workRecordStatisticsSummary = toWorkRecordStatisticsSummary(agentWorkRecordStatistics);
//            }
//            result.add("workRecordStatisticsSummary", workRecordStatisticsSummary);
//        }
//
//        //个人统计
//        Map<Long, AgentWorkRecordStatistics> userHistoryStatisticsMap = agentWorkRecordStatisticsService.getUserStatistics(Collections.singleton(currentUser.getUserId()), date, dateType);
//
//        AgentWorkRecordStatistics statistics = userHistoryStatisticsMap.get(currentUser.getUserId());
//        if (null != statistics) {
//            Double userVisitEngTeaPercent = 0d;
//            Double userVisitMathTeaPercent = 0d;
//            if (null != statistics.getUserVisitTeaCount() && statistics.getUserVisitTeaCount() > 0) {
//                if (null != statistics.getUserVisitEngTeaCount()) {
//                    userVisitEngTeaPercent = MathUtils.doubleDivide(statistics.getUserVisitEngTeaCount(), statistics.getUserVisitTeaCount());
//                }
//                if (null != statistics.getUserVisitMathTeaCount()) {
//                    userVisitMathTeaPercent = MathUtils.doubleDivide(statistics.getUserVisitMathTeaCount(), statistics.getUserVisitTeaCount());
//                }
//            }
//            IntoSchoolStatisticsItem personIntoSchoolStatisticsItem = new IntoSchoolStatisticsItem(currentUser.getUserId(), 2, currentUser.getRealName(), statistics.getUserAvgDayIntoSchool(), statistics.getUserVisitSchoolAvgTeaCount(), userVisitEngTeaPercent, userVisitMathTeaPercent,statistics.getUserVisitAndAssignHwTeaPct(),statistics.getUserWorkload());
//            result.add("personIntoSchoolStatisticsItem", personIntoSchoolStatisticsItem);
//        }else {
//            IntoSchoolStatisticsItem personIntoSchoolStatisticsItem = new IntoSchoolStatisticsItem(currentUser.getUserId(), 2, currentUser.getRealName(), 0d, 0d, 0d, 0d,0d,0d);
//            result.add("personIntoSchoolStatisticsItem", personIntoSchoolStatisticsItem);
//
//        }
//
//        result.add("dateShowStr", dateShowStr(date, dateType));
//        return result;
//    }
//
//    /**
//     * 下属团队工作量信息Controller
//     */
//    @RequestMapping(value = "team_workload_list.vpage", method = RequestMethod.GET)
//    public String team(Model model) {
//        return "rebuildViewDir/mobile/workRecord/team_workload_list";
//    }
//
//    /**
//     * 下属团队工作量信息
//     */
//    @RequestMapping(value = "team_workload.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage getTeamWorkload() {
//        // 日期类型  1： 日  2：周  3：月
//        int dateType = getRequestInt("dateType", 1);
//        int date = getRequestInt("date");
//        date = formatDate(date, dateType);
//        if (date <= 0) {
//            return MapMessage.errorMessage("传入的日期不正确或者与日期类型不匹配");
//        }
//        Long groupId = getRequestLong("groupId");
//        AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
//        if (null == groupRoleType) {
//            return MapMessage.errorMessage("传入的groupId不正确");
//        }
//        List<WorkloadStatisticsItem> workloadList = new ArrayList<>();
//        if (AgentGroupRoleType.Country == groupRoleType || AgentGroupRoleType.Region == groupRoleType || AgentGroupRoleType.Area == groupRoleType) {
//            // 获取该部门下所有的子部门
//            List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(groupId);
//            if (AgentGroupRoleType.Country == groupRoleType) {
//                //取分区数据
//                subGroupList = subGroupList.stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.Region).collect(Collectors.toList());
//            }
//            if (AgentGroupRoleType.Region == groupRoleType || AgentGroupRoleType.Area == groupRoleType) {
//                //取City数据
//                subGroupList = subGroupList.stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
//            }
//
//            List<Long> subGroupIds = subGroupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
//            Map<Long, AgentWorkRecordStatistics> groupStatistics = agentWorkRecordStatisticsService.getGroupStatistics(subGroupIds, date, dateType);
//
//            subGroupList.forEach(item -> {
//                AgentWorkRecordStatistics statistics = groupStatistics.get(item.getId());
//                Double workLoad = 0d;
//                if (null != statistics) {
//                    workLoad = statistics.getBdPerCapitaWorkload();
//                }
//                WorkloadStatisticsItem workloadStatisticsItem = new WorkloadStatisticsItem(item.getId(), 1, item.getGroupName(), workLoad);
//                workloadList.add(workloadStatisticsItem);
//            });
//        } else if (AgentGroupRoleType.City == groupRoleType) {
//            List<AgentUser> groupBusinessDevelopers = baseOrgService.getGroupBusinessDevelopers(groupId);
//            List<Long> businessDeveloperIds = groupBusinessDevelopers.stream().map(AgentUser::getId).collect(Collectors.toList());
//            Map<Long, AgentWorkRecordStatistics> userStatistics = agentWorkRecordStatisticsService.getUserStatistics(businessDeveloperIds, date, dateType);
//
//            groupBusinessDevelopers.forEach(item -> {
//                AgentWorkRecordStatistics statistics = userStatistics.get(item.getId());
//                Double workLoad = 0d;
//                if (null != statistics) {
//                    workLoad = statistics.getUserWorkload();
//                }
//                WorkloadStatisticsItem workloadStatisticsItem = new WorkloadStatisticsItem(item.getId(), 2, item.getRealName(), workLoad);
//                workloadList.add(workloadStatisticsItem);
//            });
//        }
//
//        MapMessage result = MapMessage.successMessage();
//        result.add("groupRoleType", groupRoleType);
//        result.add("workloadList", workloadList);
//        result.add("dateShowStr", dateShowStr(date, dateType));
//        return result;
//    }
//
//    /**
//     * 下属团队工作量信息Controller
//     */
//    @RequestMapping(value = "personal_workload_list.vpage", method = RequestMethod.GET)
//    public String personal_workload(Model model) {
//        return "rebuildViewDir/mobile/workRecord/personal_workload_list";
//    }
//
//    /**
//     * 下属个人工作量信息
//     */
//    @RequestMapping(value = "personal_workload.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage getPersonalWorkload() {
//        // 日期类型  1： 日  2：周  3：月
//        int dateType = getRequestInt("dateType", 1);
//        int date = getRequestInt("date");
//        date = formatDate(date, dateType);
//        if (date <= 0) {
//            return MapMessage.errorMessage("传入的日期不正确或者与日期类型不匹配");
//        }
//        AuthCurrentUser currentUser = getCurrentUser();
//        List<AgentGroupUser> groupUserByUser = baseOrgService.getGroupUserByUser(currentUser.getUserId());
//        if (CollectionUtils.isEmpty(groupUserByUser)) {
//            return MapMessage.errorMessage();
//        }
//        Long groupId = groupUserByUser.get(0).getGroupId();
//        AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
//        MapMessage result = MapMessage.successMessage();
//        if (groupRoleType == AgentGroupRoleType.Country) {
//            result = getCountyPersonalWorkload(groupId, date, dateType);
//        } else if (groupRoleType == AgentGroupRoleType.Region) {
//            result = getRegionPersonalWorkload(groupId, date, dateType);
//        } else if (groupRoleType == AgentGroupRoleType.Area){
//            result = getAreaPersonalWorkload(groupId,date,dateType);
//        } else if (groupRoleType == AgentGroupRoleType.City) {
//            result = getCityPersonalWorkload(groupId, date, dateType);
//        }
//        result.add("dateShowStr", dateShowStr(date, dateType));
//        return result;
//    }
//
//    /**
//     * 获取专员进校情况Controller
//     */
//    @RequestMapping(value = "into_school_list.vpage", method = RequestMethod.GET)
//    public String into_school_list(Model model) {
//        return "rebuildViewDir/mobile/workRecord/into_school_list";
//    }
//
//    /**
//     * 获取专员进校情况
//     */
//    @RequestMapping(value = "into_school.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage getIntoSchool() {
//        // 日期类型  1： 日  2：周  3：月
//        int dateType = getRequestInt("dateType", 1);
//        int date = getRequestInt("date");
//        date = formatDate(date, dateType);
//        if (date <= 0) {
//            return MapMessage.errorMessage("传入的日期不正确或者与日期类型不匹配");
//        }
//        Long groupId = getRequestLong("groupId");
//        AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
//        if (null == groupRoleType) {
//            return MapMessage.errorMessage("传入的groupId不正确");
//        }
//
//        MapMessage resultMessage = MapMessage.successMessage();
//        if (AgentGroupRoleType.Country == groupRoleType || AgentGroupRoleType.Region == groupRoleType || AgentGroupRoleType.Area == groupRoleType || AgentGroupRoleType.City == groupRoleType) {
//            try {
//                List<Long> groupBusinessDeveloperIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId());;
//                Future<Map<Long, AgentWorkRecordStatistics>> businessStatisticsMapFulture = getUserStatisticsMapFulture(groupBusinessDeveloperIds, date, dateType);
//
//                // 获取该部门下所有的子部门
//                List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(groupId);
//
//                if (AgentGroupRoleType.Country == groupRoleType) {
//
//                    //取分区数据
//                    List<AgentGroup> regionGroupList = subGroupList.stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.Region).collect(Collectors.toList());
//                    List<Long> regionGroupIdList = regionGroupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
//                    Future<Map<Long, AgentWorkRecordStatistics>> regionGroupStatisticsMapFulture = getGroupStatisticsMapFulture(regionGroupIdList, date, dateType);
//                    //取City数据
//                    List<AgentGroup> cityGroupList = subGroupList.stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
//                    List<Long> cityGroupIdList = subGroupList.stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.City).map(AgentGroup::getId).collect(Collectors.toList());
//                    Future<Map<Long, AgentWorkRecordStatistics>> cityGroupStatisticsMapFulture = getGroupStatisticsMapFulture(cityGroupIdList, date, dateType);
//
//                    Map<Long, AgentWorkRecordStatistics> regionGroupWorkRecordStatisticsMap = regionGroupStatisticsMapFulture.get();
//                    List<IntoSchoolStatisticsItem> regionIntoSchoolList = generateGroupIntoSchoolStatistics(regionGroupList, regionGroupWorkRecordStatisticsMap);
//                    Map<Long, AgentWorkRecordStatistics> cityGroupStatisticsMap = cityGroupStatisticsMapFulture.get();
//                    List<IntoSchoolStatisticsItem> cityIntoSchoolList = generateGroupIntoSchoolStatistics(cityGroupList, cityGroupStatisticsMap);
//                    resultMessage.add("regionIntoSchoolList", regionIntoSchoolList);
//                    resultMessage.add("cityIntoSchoolList", cityIntoSchoolList);
//                }
//                if (AgentGroupRoleType.Region == groupRoleType || AgentGroupRoleType.Area == groupRoleType) {
//                    //取City数据
//                    List<AgentGroup> cityGroupList = subGroupList.stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
//                    List<Long> cityGroupIdList = cityGroupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
//                    Future<Map<Long, AgentWorkRecordStatistics>> cityGroupStatisticsMapFulture = getGroupStatisticsMapFulture(cityGroupIdList, date, dateType);
//                    Map<Long, AgentWorkRecordStatistics> cityGroupStatisticsMap = cityGroupStatisticsMapFulture.get();
//                    List<IntoSchoolStatisticsItem> cityIntoSchoolList = generateGroupIntoSchoolStatistics(cityGroupList, cityGroupStatisticsMap);
//                    resultMessage.add("cityIntoSchoolList", cityIntoSchoolList);
//
//                }
//                Map<Long, AgentWorkRecordStatistics> businessStatisticsMap = businessStatisticsMapFulture.get();
//                List<IntoSchoolStatisticsItem> bdIntoSchoolList = generateUserIntoSchoolStatistics(groupBusinessDeveloperIds,businessStatisticsMap);
//                resultMessage.add("bdIntoSchoolList", bdIntoSchoolList);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//        }
//        resultMessage.add("groupRoleType", groupRoleType);
//        resultMessage.add("dateShowStr", dateShowStr(date, dateType));
//        return resultMessage;
//    }

    /**
     * 工作记录详情页面
     *
     * @return
     */
    @RequestMapping(value = "statistics_detai_page.vpage", method = RequestMethod.GET)
    public String statisticsDetai() {
        return "rebuildViewDir/mobile/workRecord/statistics_detai_page";
    }


//    private WorkRecordStatisticsSummary toWorkRecordStatisticsSummary(AgentWorkRecordStatistics statistics) {
//        if (null == statistics) {
//            return null;
//        }
//        WorkRecordStatisticsSummary statisticsSummary = new WorkRecordStatisticsSummary();
//        statisticsSummary.setGroupId(statistics.getGroupId());                            // 部门ID
//        statisticsSummary.setGroupName(statistics.getGroupName());                        // 部门名称
//        statisticsSummary.setGroupUserCount(statistics.getGroupUserCount());                  // 部门下所有人员数量
//        statisticsSummary.setFillInWorkRecordUserCount(statistics.getFillInWorkRecordUserCount());       // 填写工作记录的人员数量
//
//        // 下属工作情况
//        //---------专员
//        statisticsSummary.setBdUserCount(statistics.getBdUserCount());                     // 专员数量
//        statisticsSummary.setBdFillInWorkRecordUserCount(statistics.getBdFillInWorkRecordUserCount());     // 填写工作记录的专员数量
//        statisticsSummary.setBdPerCapitaWorkload(statistics.getBdPerCapitaWorkload());             // 专员人均日均工作量
//
//        //---------市经理
//        statisticsSummary.setCmUserCount(statistics.getCmUserCount());                     // 市经理数量
//        statisticsSummary.setCmFillInWorkRecordUserCount(statistics.getCmFillInWorkRecordUserCount());     // 填写工作记录的市经理数量
//        statisticsSummary.setCmPerCapitaWorkload(statistics.getCmPerCapitaWorkload());             // 市经理人均日均工作量
//
//        //---------区域经理
//        statisticsSummary.setAmUserCount(statistics.getAmUserCount());                     // 区域经理数量
//        statisticsSummary.setAmFillInWorkRecordUserCount(statistics.getAmFillInWorkRecordUserCount());     // 填写工作记录的区域经理数量
//        statisticsSummary.setAmPerCapitaWorkload(statistics.getAmPerCapitaWorkload());             // 区域经理人均日均工作量
//
//        //---------大区经理
//        statisticsSummary.setRmUserCount(statistics.getRmUserCount());                     // 大区经理数量
//        statisticsSummary.setRmFillInWorkRecordUserCount(statistics.getRmFillInWorkRecordUserCount());     // 填写工作记录的大区经理数量
//        statisticsSummary.setRmPerCapitaWorkload(statistics.getRmPerCapitaWorkload());             // 大区经理人均日均工作量
//
//
//        // 专员进校情况
//        statisticsSummary.setBdPerCapitaIntoSchool(statistics.getBdPerCapitaIntoSchool());             // 专员人均日均进校次数
//        statisticsSummary.setBdVisitSchoolAvgTeaCount(statistics.getBdVisitSchoolAvgTeaCount());           // 校均拜访老师数
//
//        if (null != statistics.getBdVisitTeaCount() && statistics.getBdVisitTeaCount() > 0) {
//            if (null != statistics.getBdVisitEngTeaCount()) {
//                statisticsSummary.setBdVisitEngTeaPercent(MathUtils.doubleDivide(statistics.getBdVisitEngTeaCount(), statistics.getBdVisitTeaCount()));
//            }
//            if (null != statistics.getBdVisitMathTeaCount()) {
//                statisticsSummary.setBdVisitMathTeaPercent(MathUtils.doubleDivide(statistics.getBdVisitMathTeaCount(), statistics.getBdVisitTeaCount()));
//            }
//        }
//        return statisticsSummary;
//    }
//
//    /**
//     * 时间转换为业务时间
//     *
//     * @param date
//     * @param dateType 日期类型  1： 日  2：周  3：月
//     * @return
//     */
//    private int formatDate(int date, int dateType) {
//        if ((dateType != 1 && dateType != 2 && dateType != 3) || date <= 0) {
//            return 0;
//        }
//        try {
//            Date dateTime = DateUtils.stringToDate(String.valueOf(date), "yyyyMMdd");
//            if (dateType == 1) {
//                String dateStr = DateUtils.dateToString(dateTime, "yyyyMMdd");
//                return Integer.parseInt(dateStr);
//            } else if (dateType == 2) {
//                WeekRange weekRange = WeekRange.newInstance(dateTime.getTime());
//                //取得是周的第一天
//                Date firstDate = weekRange.getStartDate();
//                String firstDateStr = DateUtils.dateToString(firstDate, "yyyyMMdd");
//                return Integer.parseInt(firstDateStr);
//            } else if (dateType == 3) {
//                MonthRange monthRange = MonthRange.newInstance(dateTime.getTime());
//                //每个月第一天
//                Date firstDate = monthRange.getStartDate();
//                String firstDateStr = DateUtils.dateToString(firstDate, "yyyyMMdd");
//                return Integer.parseInt(firstDateStr);
//            }
//            return 0;
//        } catch (Exception e) {
//            return -1;
//        }
//    }

    private String dateShowStr(Date date, int dateType) {
        if ((dateType != 1 && dateType != 2 && dateType != 3) || date == null) {
            return null;
        }
        try {
            if (dateType == 1) {
                return DateUtils.dateToString(date, "M.d");
            } else if (dateType == 2) {
                WeekRange weekRange = WeekRange.newInstance(date.getTime());
                Date beginDate = weekRange.getStartDate();
                Date lastDate = weekRange.getEndDate();
                return DateUtils.dateToString(beginDate, "M.d") + " - " + DateUtils.dateToString(lastDate, "M.d");
            } else {
                return DateUtils.dateToString(date, "yyyy.M");
            }
        } catch (Exception e) {
            return null;
        }
    }


//    private Future<Map<Long, AgentWorkRecordStatistics>> getUserStatisticsMapFulture(List<Long> userIds, Integer date, Integer dateType) {
//        Future<Map<Long, AgentWorkRecordStatistics>> userStatisticsMapFulture = AlpsThreadPool.getInstance().submit(() -> {
//            return agentWorkRecordStatisticsService.getUserStatistics(userIds, date, dateType);
//        });
//        return userStatisticsMapFulture;
//    }
//
//
//    private Future<Map<Long, AgentWorkRecordStatistics>> getGroupStatisticsMapFulture(List<Long> groupIds, Integer date, Integer dateType) {
//        Future<Map<Long, AgentWorkRecordStatistics>> userStatisticsMapFulture = AlpsThreadPool.getInstance().submit(() -> {
//            return agentWorkRecordStatisticsService.getGroupStatistics(groupIds, date, dateType);
//        });
//        return userStatisticsMapFulture;
//    }
//
//    private MapMessage getCountyPersonalWorkload(Long groupId, Integer date, Integer dateType) {
//        MapMessage resultMessage = MapMessage.successMessage();
//        List<Long> businessDeveloperUserIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId());
//        List<Long> cityManagerUserIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.CityManager.getId());
//        List<Long> areaManagerUserIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.AreaManager.getId());
//        List<Long> reginManagerUserIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.Region.getId());
//        Future<Map<Long, AgentWorkRecordStatistics>> businessStatisticsMapFulture = getUserStatisticsMapFulture(businessDeveloperUserIds, date, dateType);
//        Future<Map<Long, AgentWorkRecordStatistics>> cityManagerStatisticsMapFulture = getUserStatisticsMapFulture(cityManagerUserIds, date, dateType);
//        Future<Map<Long, AgentWorkRecordStatistics>> areaManagerStatisticsMapFulture = getUserStatisticsMapFulture(areaManagerUserIds, date, dateType);
//        Future<Map<Long, AgentWorkRecordStatistics>> reginManagerStatisticsMapFulture = getUserStatisticsMapFulture(reginManagerUserIds, date, dateType);
//
//        try {
//            //专员的工作量
//            Map<Long, AgentWorkRecordStatistics> businessStatisticsMap = businessStatisticsMapFulture.get();
//            List<PersonalWorkloadStatisticsItem> bdWorkloadList = generateUserWorkload(businessDeveloperUserIds, businessStatisticsMap, AgentRoleType.BusinessDeveloper);
//            //市经理的工作量
//            Map<Long, AgentWorkRecordStatistics> cityManagerStatisticsMap = cityManagerStatisticsMapFulture.get();
//            List<PersonalWorkloadStatisticsItem> cmWorkloadList = generateUserWorkload(cityManagerUserIds, cityManagerStatisticsMap, AgentRoleType.CityManager);
//
//
//
//            //区域经理的工作量
//            Map<Long, AgentWorkRecordStatistics> areaManagerStatisticsMap = areaManagerStatisticsMapFulture.get();
//            List<PersonalWorkloadStatisticsItem> amWorkloadList = generateUserWorkload(areaManagerUserIds, areaManagerStatisticsMap, AgentRoleType.AreaManager);
//
//
//            //大区经理的工作量
//            Map<Long, AgentWorkRecordStatistics> reginManagerStatisticsMap = reginManagerStatisticsMapFulture.get();
//            List<PersonalWorkloadStatisticsItem> rmWorkloadList = generateUserWorkload(reginManagerUserIds, reginManagerStatisticsMap, AgentRoleType.Region);
//
//            resultMessage.add("bdWorkloadList", bdWorkloadList);
//            resultMessage.add("cmWorkloadList", cmWorkloadList);
//            resultMessage.add("amWorkloadList",amWorkloadList);
//            resultMessage.add("rmWorkloadList", rmWorkloadList);
//        } catch (InterruptedException e) {
//        } catch (ExecutionException e) {
//        }
//        return resultMessage;
//    }
//
//    private MapMessage getRegionPersonalWorkload(Long groupId, Integer date, Integer dateType) {
//        MapMessage resultMessage = MapMessage.successMessage();
//        List<Long> businessDeveloperUserIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId());
//        List<Long> cityManagerUserIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.CityManager.getId());
//        List<Long> areaManagerUserIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.AreaManager.getId());
//        Future<Map<Long, AgentWorkRecordStatistics>> businessStatisticsMapFulture = getUserStatisticsMapFulture(businessDeveloperUserIds, date, dateType);
//        Future<Map<Long, AgentWorkRecordStatistics>> cityManagerStatisticsMapFulture = getUserStatisticsMapFulture(cityManagerUserIds, date, dateType);
//        Future<Map<Long, AgentWorkRecordStatistics>> areaManagerStatisticsMapFulture = getUserStatisticsMapFulture(areaManagerUserIds, date, dateType);
//        try {
//            //专员的工作量
//            Map<Long, AgentWorkRecordStatistics> businessStatisticsMap = businessStatisticsMapFulture.get();
//            List<PersonalWorkloadStatisticsItem> bdWorkloadList = generateUserWorkload(businessDeveloperUserIds, businessStatisticsMap, AgentRoleType.BusinessDeveloper);
//
//            //市经理的工作量
//            Map<Long, AgentWorkRecordStatistics> cityManagerStatisticsMap = cityManagerStatisticsMapFulture.get();
//            List<PersonalWorkloadStatisticsItem> cmWorkloadList = generateUserWorkload(cityManagerUserIds, cityManagerStatisticsMap, AgentRoleType.CityManager);
//
//
//            //区域经理的工作量
//            Map<Long, AgentWorkRecordStatistics> areaManagerStatisticsMap = areaManagerStatisticsMapFulture.get();
//            List<PersonalWorkloadStatisticsItem> amWorkloadList = generateUserWorkload(areaManagerUserIds, areaManagerStatisticsMap, AgentRoleType.AreaManager);
//
//
//            resultMessage.add("bdWorkloadList", bdWorkloadList);
//            resultMessage.add("cmWorkloadList", cmWorkloadList);
//            resultMessage.add("amWorkloadList",amWorkloadList);
//        } catch (InterruptedException e) {
//        } catch (ExecutionException e) {
//        }
//        return resultMessage;
//    }
//
//    private MapMessage getAreaPersonalWorkload(Long groupId, Integer date, Integer dateType) {
//        MapMessage resultMessage = MapMessage.successMessage();
//        List<Long> businessDeveloperUserIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId());
//        List<Long> cityManagerUserIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.CityManager.getId());
//        Future<Map<Long, AgentWorkRecordStatistics>> businessStatisticsMapFulture = getUserStatisticsMapFulture(businessDeveloperUserIds, date, dateType);
//        Future<Map<Long, AgentWorkRecordStatistics>> cityManagerStatisticsMapFulture = getUserStatisticsMapFulture(cityManagerUserIds, date, dateType);
//        try {
//            //专员的工作量
//            Map<Long, AgentWorkRecordStatistics> businessStatisticsMap = businessStatisticsMapFulture.get();
//            List<PersonalWorkloadStatisticsItem> bdWorkloadList = generateUserWorkload(businessDeveloperUserIds, businessStatisticsMap, AgentRoleType.BusinessDeveloper);
//
//            //市经理的工作量
//            Map<Long, AgentWorkRecordStatistics> cityManagerStatisticsMap = cityManagerStatisticsMapFulture.get();
//            List<PersonalWorkloadStatisticsItem> cmWorkloadList = generateUserWorkload(cityManagerUserIds, cityManagerStatisticsMap, AgentRoleType.CityManager);
//
//            resultMessage.add("bdWorkloadList", bdWorkloadList);
//            resultMessage.add("cmWorkloadList", cmWorkloadList);
//        } catch (InterruptedException e) {
//        } catch (ExecutionException e) {
//        }
//        return resultMessage;
//    }
//
//
//    private MapMessage getCityPersonalWorkload(Long groupId, Integer date, Integer dateType) {
//        MapMessage resultMessage = MapMessage.successMessage();
//        List<Long> businessDeveloperUserIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId());
//        Future<Map<Long, AgentWorkRecordStatistics>> businessStatisticsMapFulture = getUserStatisticsMapFulture(businessDeveloperUserIds, date, dateType);
//        try {
//            //专员的工作量
//            Map<Long, AgentWorkRecordStatistics> businessStatisticsMap = businessStatisticsMapFulture.get();
//            List<PersonalWorkloadStatisticsItem> bdWorkloadList = generateUserWorkload(businessDeveloperUserIds, businessStatisticsMap, AgentRoleType.BusinessDeveloper);
//
//            resultMessage.add("bdWorkloadList", bdWorkloadList);
//        } catch (InterruptedException e) {
//        } catch (ExecutionException e) {
//        }
//        return resultMessage;
//    }
//
//    private List<PersonalWorkloadStatisticsItem> generateUserWorkload(List<Long> userIds, Map<Long, AgentWorkRecordStatistics> userStatisticsMap, AgentRoleType agentRoleType) {
//        List<PersonalWorkloadStatisticsItem> userWorkloadList = new ArrayList<>();
//        List<AgentUser> users = baseOrgService.getUsers(userIds);
//        users.forEach(item -> {
//            AgentWorkRecordStatistics statistics = userStatisticsMap.get(item.getId());
//            if (null != statistics) {
//                PersonalWorkloadStatisticsItem personalWorkloadStatisticsItem = new PersonalWorkloadStatisticsItem(item.getId(), 2, item.getRealName(), statistics.getUserWorkload(), statistics.getGroupName(), agentRoleType);
//                userWorkloadList.add(personalWorkloadStatisticsItem);
//            }else {
//                List<AgentGroup> userGroups = baseOrgService.getUserGroups(item.getId());
//                AgentGroup agentGroup = userGroups.get(0);
//                PersonalWorkloadStatisticsItem personalWorkloadStatisticsItem = new PersonalWorkloadStatisticsItem(item.getId(), 2, item.getRealName(), 0d, agentGroup.getGroupName(), agentRoleType);
//                userWorkloadList.add(personalWorkloadStatisticsItem);
//            }
//        });
//        return userWorkloadList;
//    }
//
//    private List<IntoSchoolStatisticsItem> generateGroupIntoSchoolStatistics(List<AgentGroup> agentGroups, Map<Long, AgentWorkRecordStatistics> groupStatisticsMap) {
//        List<IntoSchoolStatisticsItem> list = new ArrayList<>();
//        agentGroups.forEach(item -> {
//            AgentWorkRecordStatistics statistics = groupStatisticsMap.get(item.getId());
//            if (null != statistics) {
//                Double bdVisitEngTeaPercent = 0d;
//                Double bdVisitMathTeaPercent = 0d;
//                if (null != statistics.getBdVisitTeaCount() && statistics.getBdVisitTeaCount() > 0) {
//                    if (null != statistics.getBdVisitEngTeaCount()) {
//                        bdVisitEngTeaPercent = MathUtils.doubleDivide(statistics.getBdVisitEngTeaCount(), statistics.getBdVisitTeaCount());
//                    }
//                    if (null != statistics.getBdVisitMathTeaCount()) {
//                        bdVisitMathTeaPercent = MathUtils.doubleDivide(statistics.getBdVisitMathTeaCount(), statistics.getBdVisitTeaCount());
//                    }
//                }
//                IntoSchoolStatisticsItem intoSchoolStatisticsItem = new IntoSchoolStatisticsItem(item.getId(), 1, item.getGroupName(), statistics.getBdPerCapitaIntoSchool(), statistics.getBdVisitSchoolAvgTeaCount(), bdVisitEngTeaPercent, bdVisitMathTeaPercent);
//                list.add(intoSchoolStatisticsItem);
//            }else {
//
//                IntoSchoolStatisticsItem intoSchoolStatisticsItem = new IntoSchoolStatisticsItem(item.getId(), 1, item.getGroupName(), 0d, 0d, 0d, 0d);
//                list.add(intoSchoolStatisticsItem);
//            }
//        });
//        return list;
//    }
//
//    private List<IntoSchoolStatisticsItem> generateUserIntoSchoolStatistics(List<Long> userIds, Map<Long, AgentWorkRecordStatistics> userStatisticsMap) {
//        List<IntoSchoolStatisticsItem> list = new ArrayList<>();
//        List<AgentUser> users = baseOrgService.getUsers(userIds);
//        Map<Long, AgentUser> userMap = users.stream().collect(Collectors.toMap(AgentUser::getId, Function.identity(), (o1, o2) -> o2));
//        userMap.forEach((key,item) -> {
//            AgentWorkRecordStatistics statistics = userStatisticsMap.get(item.getId());
//            if (null != statistics) {
//                Double userVisitEngTeaPercent = 0d;
//                Double userVisitMathTeaPercent = 0d;
//                if (null != statistics.getUserVisitTeaCount() && statistics.getUserVisitTeaCount() > 0) {
//                    if (null != statistics.getUserVisitEngTeaCount()) {
//                        userVisitEngTeaPercent = MathUtils.doubleDivide(statistics.getUserVisitEngTeaCount(), statistics.getUserVisitTeaCount());
//                    }
//                    if (null != statistics.getUserVisitMathTeaCount()) {
//                        userVisitMathTeaPercent = MathUtils.doubleDivide(statistics.getUserVisitMathTeaCount(), statistics.getUserVisitTeaCount());
//                    }
//                }
//                IntoSchoolStatisticsItem intoSchoolStatisticsItem = new IntoSchoolStatisticsItem(item.getId(), 2, item.getRealName(), statistics.getUserAvgDayIntoSchool(), statistics.getUserVisitSchoolAvgTeaCount(), userVisitEngTeaPercent, userVisitMathTeaPercent);
//                list.add(intoSchoolStatisticsItem);
//            }else {
//                IntoSchoolStatisticsItem intoSchoolStatisticsItem = new IntoSchoolStatisticsItem(item.getId(), 2, item.getRealName(), 0d, 0d,0d, 0d);
//                list.add(intoSchoolStatisticsItem);
//            }
//        });
//        return list;
//    }



    /**
     * 总计信息
     */
    @RequestMapping(value = "overview.vpage", method = RequestMethod.GET)
    @ResponseBody
    @OperationCode("93125d2b8e5a4987")
    public MapMessage overview() {
        // 日期类型  1： 日  2：周  3：月
        int dateType = getRequestInt("dateType", 1);
        Date date = getRequestDate("date", "yyyyMMdd", new Date());

        AuthCurrentUser currentUser = getCurrentUser();
        MapMessage result = MapMessage.successMessage();
        //当是全国总监或者大区经理或者区域经理或者市经理身份时，获取所在部门统计
        if (currentUser.isCountryManager() || currentUser.isRegionManager() || currentUser.isCityManager()|| currentUser.isAreaManager()){
            List<AgentGroupUser> groupUserByUser = baseOrgService.getGroupUserByUser(currentUser.getUserId());
            if (CollectionUtils.isEmpty(groupUserByUser)) {
                return MapMessage.errorMessage();
            }
            List<Long> groupIds = new ArrayList<>();
            Long groupId = groupUserByUser.get(0).getGroupId();
            if(currentUser.isCountryManager()){
                List<AgentGroup> groupList = baseOrgService.getSubGroupList(groupId);
                groupIds.addAll(groupList.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing).map(AgentGroup::getId).collect(Collectors.toList()));
            }else {
                groupIds.add(groupId);
            }
            Map<Long, AgentWorkRecordStatistics> statisticsMap = agentWorkRecordStatisticsService.getGroupStatistics(groupIds, date, dateType);

            List<WrStatisticsOverview> overviewDataList = new ArrayList<>();
            List<AgentRoleType> roleTypeList = new ArrayList<>();
            roleTypeList.add(AgentRoleType.BusinessDeveloper);
            roleTypeList.add(AgentRoleType.CityManager);
            roleTypeList.add(AgentRoleType.AreaManager);
            roleTypeList.add(AgentRoleType.Region);

            statisticsMap.values().forEach(p -> {
                WrStatisticsOverview item = new WrStatisticsOverview();
                item.setGroupId(p.getGroupId());
                item.setGroupName(p.getGroupName());
                AgentGroup businessUnit = baseOrgService.getParentGroupByRole(p.getGroupId(), AgentGroupRoleType.Marketing);
                if(businessUnit != null){
                    if(StringUtils.contains(businessUnit.getGroupName(), "小学")) {
                        item.setSchoolLevelName("小学");
                    }else {
                        item.setSchoolLevelName("中学");
                    }
                }

                if(MapUtils.isNotEmpty(p.getRoleDataMap())){
                    roleTypeList.forEach(r -> {
                        AgentWorkRecordStatisticsRoleData roleData = p.getRoleDataMap().get(r.getId());
                        if(roleData != null){
                            WrStatisticsOverviewRoleData overviewRoleData = new WrStatisticsOverviewRoleData();
                            overviewRoleData.setRoleId(r.getId());
                            overviewRoleData.setRoleName(r.getRoleName());
                            overviewRoleData.setUserCount(roleData.getUserCount());
                            overviewRoleData.setFillRecordUserCount(roleData.getFillRecordUserCount());
                            overviewRoleData.setRecordUnreachedUserCount(roleData.getRecordUnreachedUserCount());
                            overviewRoleData.setPerCapitaWorkload(roleData.getPerCapitaWorkload());
                            item.getRoleDataList().add(overviewRoleData);
                        }
                    });
                }
                overviewDataList.add(item);

            });
            result.add("dataList", overviewDataList);
        }
        result.add("dateShowStr", dateShowStr(date, dateType));
        return result;
    }




    /**
     * 总计信息
     */
    @RequestMapping(value = "data_list.vpage")
    @ResponseBody
    public MapMessage dataList() {
        // 日期类型  1： 日  2：周  3：月
        Long groupId = requestLong("groupId");
        int dimension = getRequestInt("dimension", 1);   // 维度 1:默认，  2：专员   3： 市经理   4： 区域经理， 5：大区经理
        int dateType = getRequestInt("dateType", 1);     // 日期类型  1： 日  2：周  3：月
        Date date = getRequestDate("date", "yyyyMMdd", new Date());

        if(groupId == null || dimension < 1 || dimension > 5){
            return MapMessage.errorMessage("请求参数数据有误");
        }

        MapMessage message = MapMessage.successMessage();
        message.add("dimensionList", getDimensionList(groupId));



        List<AgentWorkRecordStatistics> statisticsList = new ArrayList<>();

        Integer groupOrUser = 2;      //  1: 部门   2： 用户
        AgentRoleType targetRoleType = AgentRoleType.BusinessDeveloper;
        if(dimension == 1){
            AgentGroup group = baseOrgService.getGroupById(groupId);
            // 分区默认情况下查看专员， 其他情况默认查看子部门
            if(group != null && group.fetchGroupRoleType() != AgentGroupRoleType.City){
                groupOrUser = 1;
                // 获取子部门的统计数据
                List<AgentGroup> groupList = baseOrgService.getGroupListByParentId(groupId);
                List<Long> groupIds = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
                Map<Long, AgentWorkRecordStatistics> statisticsMap = agentWorkRecordStatisticsService.getGroupStatistics(groupIds, date, dateType);
                if(MapUtils.isNotEmpty(statisticsMap)){
                    statisticsList.addAll(statisticsMap.values());
                }
                // 获取本部门的统计数据作为合计, 合计不能点击下钻
                Map<Long, AgentWorkRecordStatistics> sumMap = agentWorkRecordStatisticsService.getGroupStatistics(Collections.singleton(groupId), date, dateType);
                AgentWorkRecordStatistics sumData = sumMap.get(groupId);
                if(sumData != null){
                    sumData.setGroupId(null);
                    sumData.setGroupName("合计");
                    statisticsList.add(sumData);
                }
            }
        }else {
            if(dimension == 2){
                targetRoleType = AgentRoleType.BusinessDeveloper;
            }else if(dimension == 3){
                targetRoleType = AgentRoleType.CityManager;
            }else if(dimension == 4){
                targetRoleType = AgentRoleType.AreaManager;
            }else if(dimension == 5){
                targetRoleType = AgentRoleType.Region;
            }
        }

        // user的情况
        if(groupOrUser == 2){
            message.add("userRole", targetRoleType);
            List<Long> userIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, targetRoleType.getId());
            Map<Long, AgentWorkRecordStatistics> statisticsMap = agentWorkRecordStatisticsService.getUserStatistics(userIds, date, dateType);
            if(MapUtils.isNotEmpty(statisticsMap)){
                statisticsList.addAll(statisticsMap.values());
            }
        }

        List<Object> dataList = convertToDataList(statisticsList, groupOrUser);
        message.add("dataList", dataList);
        message.add("groupOrUser", groupOrUser);
        message.add("dateShowStr", dateShowStr(date, dateType));
        return message;
    }

    // 获取数据维度列表   维度 1:默认，  2：专员   3： 市经理   4： 区域经理， 5：大区经理
    private List<Map<String, Object>> getDimensionList(Long groupId){
        List<Map<String, Object>> dimensionList = new ArrayList<>();

        dimensionList.add(createDimensionItem(1, "组织结构"));

        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group != null && group.fetchGroupRoleType() != null){
            AgentGroupRoleType groupRoleType = group.fetchGroupRoleType();
            if(groupRoleType == AgentGroupRoleType.Marketing){
                dimensionList.add(createDimensionItem(2, "专员"));
                dimensionList.add(createDimensionItem(3, "市经理"));
                if(StringUtils.contains(group.getGroupName(), "小学")){
                    dimensionList.add(createDimensionItem(4, "区域经理"));
                }
                dimensionList.add(createDimensionItem(5, "大区经理"));
            }else if(groupRoleType == AgentGroupRoleType.Region){
                dimensionList.add(createDimensionItem(2, "专员"));
                dimensionList.add(createDimensionItem(3, "市经理"));
                AgentGroup businessUnit = baseOrgService.getParentGroupByRole(group.getId(), AgentGroupRoleType.Marketing);
                if(businessUnit != null && StringUtils.contains(businessUnit.getGroupName(), "小学")){
                    dimensionList.add(createDimensionItem(4, "区域经理"));
                }
            }else if(groupRoleType == AgentGroupRoleType.Area){
                dimensionList.add(createDimensionItem(2, "专员"));
                dimensionList.add(createDimensionItem(3, "市经理"));
            }
        }
        return dimensionList;
    }

    private Map<String, Object> createDimensionItem(Integer code, String desc){
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("desc", desc);
        return map;
    }


    private List<Object> convertToDataList(List<AgentWorkRecordStatistics> statisticsList, Integer groupOrUser){
        List<Object> result = new ArrayList<>();
        if(CollectionUtils.isEmpty(statisticsList)){
            return result;
        }
        statisticsList.forEach(p -> {
            if(groupOrUser == 1){ // 部门
                WrStatisticsGroupData groupData = new WrStatisticsGroupData();
                groupData.setGroupId(p.getGroupId());
                groupData.setGroupName(p.getGroupName());
                if(MapUtils.isNotEmpty(p.getRoleDataMap())){
                    AgentWorkRecordStatisticsRoleData roleData = p.getRoleDataMap().get(AgentRoleType.BusinessDeveloper.getId());
                    if(roleData != null){
                        groupData.setUserCount(roleData.getUserCount());
                        groupData.setRecordUnreachedUserCount(roleData.getRecordUnreachedUserCount());
                        groupData.setPerCapitaWorkload(roleData.getPerCapitaWorkload());
                    }
                }
                if(p.getGroupId() == null){
                    groupData.setClickable(false);
                }else {
                    groupData.setClickable(true);
                }
                result.add(groupData);
            }else if(groupOrUser == 2){  // user
                WrStatisticsUserData userData = new WrStatisticsUserData();
                userData.setUserId(p.getUserId());
                userData.setUserName(p.getUserName());
                userData.setUserIntoSchoolWorkload(p.getUserIntoSchoolWorkload());
                userData.setUserVisitWorkload(p.getUserVisitWorkload());
                userData.setUserWorkload(p.getUserWorkload());
                userData.setUserWorkDays(p.getUserWorkDays());
                userData.setUserNeedWordDays(p.getUserNeedWordDays());
                result.add(userData);
            }
        });
        return result;
    }


    /**
     * 首页-我的进校统计
     * @return
     */
    @RequestMapping(value = "my_into_school_statistics.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMyIntoSchoolStatistics() {
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        return MapMessage.successMessage().add("dataMap",workRecordService.getMyIntoSchoolStatistics(getCurrentUserId(),date));
    }

    /**
     * 首页-统计过程月榜
     * @return
     */
    @RequestMapping(value = "process_monthly_ranking_statistics.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage processMonthlyRankingStatistics() {
        Integer searchType = getRequestInt("searchType",1);//搜索类型 1：小学专员 2：中学专员
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        AuthCurrentUser user = getCurrentUser();
        return workRecordService.processMonthlyRankingStatistics(user,searchType,date);
    }

    /**
     * 过程月榜明细
     * @return
     */
    @RequestMapping(value = "process_monthly_ranking_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage processMonthlyRankingDetail() {
        Integer searchType = getRequestInt("searchType",1);     //搜索类型 1：小学专员 2：中学专员
//        Integer rankingType = getRequestInt("rankingType",1);   //1：工作量之星 2：进校之星 3：见师之星
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        AuthCurrentUser user = getCurrentUser();
        return workRecordService.processMonthlyRankingDetail(user,searchType,date);
    }

    /**
     * 部门及用户角色列表
     * @return
     */
    @RequestMapping(value = "group_user_role_type_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage groupUserRoleTypeList() {
        AuthCurrentUser currentUser = getCurrentUser();
        MapMessage mapMessage = MapMessage.successMessage();
        AgentRoleType userRole = baseOrgService.getUserRole(currentUser.getUserId());
        mapMessage.put("userRoleType",userRole);
        mapMessage.put("dataList",workRecordService.groupUserRoleTypeList(currentUser));
        return mapMessage;
    }

    /**
     * 首页-团队工作量统计
     * @return
     */
    @RequestMapping(value = "team_workload_statistics.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage teamWorkloadStatistics() {
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Long groupId = getRequestLong("groupId");
        String userRoleType = getRequestString("userRoleType");
        AuthCurrentUser currentUser = getCurrentUser();
        return MapMessage.successMessage().add("dataMap",workRecordService.teamWorkloadStatistics(currentUser,date,groupId,userRoleType));
    }


    /**
     * 首页-团队专员进校
     * @return
     */
    @RequestMapping(value = "team_bd_into_school_statistics.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage teamBdIntoSchoolStatistics() {
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Integer searchType = getRequestInt("searchType",1); //查询类型 1：小学专员 2：中学专员
        AuthCurrentUser currentUser = getCurrentUser();
        return MapMessage.successMessage().add("dataMap",workRecordService.teamBdIntoSchoolStatistics(currentUser,date,searchType));
    }

    /**
     * 团队专员进校明细
     * @return
     */
    @RequestMapping(value = "team_bd_into_school_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage teamBdIntoSchoolDetail() {
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Integer dateType = getRequestInt("dateType", 1);     // 日期类型  1： 日  2：周  3：月
        Long groupId = getRequestLong("groupId");
        Integer dimension = getRequestInt("dimension", 1);  // 1:默认   2：大区   3：区域   4：分区   5：专员

        Integer groupOrUser = 1; //1: 部门   2： 用户
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if (dimension == 1){
            if(group != null && group.fetchGroupRoleType() == AgentGroupRoleType.City){
                groupOrUser = 2;
            }
        }
        if (dimension == 5){
            groupOrUser = 2;
        }


        AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
        boolean flag = agentWorkRecordStatisticsService.judgeGroupDimension(groupRoleType, dimension);
        if(!flag){
            return MapMessage.errorMessage("参数组合有误！");
        }

        MapMessage mapMessage = MapMessage.successMessage();
        Map<Long, AgentWorkRecordStatistics> intoSchoolWorkRecordStatisticsMap = workRecordService.teamBdIntoSchoolDetail(date, groupId,groupRoleType, dimension, dateType);
        List<AgentWorkRecordStatistics> statisticsList = new ArrayList<>();
        statisticsList.addAll(intoSchoolWorkRecordStatisticsMap.values());

        List<Map<String,Object>> dataList = agentWorkRecordStatisticsService.intoSchoolStatisticsConvertToMapList(statisticsList, groupOrUser);
        mapMessage.add("dataList", dataList);
        mapMessage.add("groupOrUser", groupOrUser);
        mapMessage.add("dateShowStr", dateShowStr(date, dateType));
        mapMessage.add("dimensionList",agentTaskCenterService.fetchDimensionList(groupId));
        return mapMessage;
    }

//    @RequestMapping(value = "generateData.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage generateData() {
//        Date startDate = getRequestDate("startDate");
//        Date endDate = getRequestDate("endDate");
//        Integer type = getRequestInt("type");
//        agentWorkRecordStatisticsHandler.generateData(startDate,endDate,type);
//        return MapMessage.successMessage();
//    }

    /**
     * 今日工作概览（我的）
     * @return
     */
    @RequestMapping(value = "today_work_overview_mine.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage todayWorkOverviewMine() {
        Long userId = getRequestLong("userId");
        if (userId == 0L){
            userId = getCurrentUserId();
        }
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        return MapMessage.successMessage().add("dataMap",workRecordService.userWorkStatistics(userId,date,1));
    }


    /**
     * 今日工作概览（团队）
     * @return
     */
    @RequestMapping(value = "today_work_overview_team.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage todayWorkOverviewTeam() {
        Long userId = getRequestLong("userId");
        if (userId == 0L){
            userId = getCurrentUserId();
        }
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
        if (groupUser == null){
            return MapMessage.errorMessage("所在部门获取错误！");
        }
        return MapMessage.successMessage().add("dataMap",workRecordService.groupWorkStatistics(groupUser.getGroupId(),date,1,AgentRoleType.BusinessDeveloper));
    }



    /**
     * 工作统计趋势图
     * @return
     */
    @RequestMapping(value = "work_statistics_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage workStatisticsData() {
        Long id = getRequestLong("id");
        Integer groupOrUser = getRequestInt("groupOrUser", 1);//1：group  2：user
        if (id == 0L){
            if(groupOrUser == 1){
                AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(getCurrentUserId()).stream().findFirst().orElse(null);
                id = groupUser.getGroupId();
            }else {
                id = getCurrentUserId();
            }
        }
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Integer dateType = getRequestInt("dateType",1); // 1： 日  2：周  3：月
        String userRoleType = requestString("userRoleType","BusinessDeveloper");
        return workRecordService.workStatisticsView(id,groupOrUser,date,dateType,userRoleType);
    }

    //部门数据筛选
    @RequestMapping(value = "range_organization_role.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rangeOrganizationRole(){
        Long groupId = getRequestLong("groupId");
        String userRoleType = getRequestString("userRoleType");
        String groupRoleType = getRequestString("groupRoleType");
        return MapMessage.successMessage().add("dataMap",agentRegisterTeacherStatisticsService.rangeOrganizationRole(groupId,userRoleType,groupRoleType));
    }

    //过程排名数据列表
    @RequestMapping(value = "team_statistics_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage teamStatisticsList(){
        Long groupId = getRequestLong("groupId");
        String groupRoleType = getRequestString("groupRoleType");
        if(StringUtils.isBlank(groupRoleType)){
            return MapMessage.errorMessage("请选择部门角色");
        }
        String userRoleType = getRequestString("userRoleType");
        if(StringUtils.isBlank(userRoleType)){
            return MapMessage.errorMessage("请选择用户角色");
        }
        Integer dateType = getRequestInt("dateType",1); // 1： 日  2：周  3：月
        Date date = getRequestDate("date","yyyyMMdd",new Date());
        return agentRegisterTeacherStatisticsService.teamStatisticsData(groupId,userRoleType,groupRoleType,dateType,date);
    }

    /**
     * 工作记录列表接口
     *
     * @return
     */
    @RequestMapping(value = "work_record_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage workRecordList() {
        Long userId = getRequestLong("userId");
        if (0L == userId){
            userId = getCurrentUserId();
        }
        Date date = getRequestDate("date", "yyyyMMdd");
        if (date == null) {
            return MapMessage.errorMessage("传入的日期不正确或者与日期类型不匹配");
        }
        Integer dateType = getRequestInt("dateType",1); // 1： 日  2：周  3：月

        Date startDate = agentWorkRecordStatisticsService.getStartDatePub(date, dateType);
        Date endDate = agentWorkRecordStatisticsService.getEndDatePub(date, dateType);
        return MapMessage.successMessage().add("dataList",workRecordService.workRecordList(userId, startDate, endDate));
    }

    /**
     * 从summary数据初始化每天的新注册老师数据
     *
     * @return
     */
    @RequestMapping(value = "test_init_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage testInitData() {
        String dateStr = requestString("dateStr");
        Integer dayNum = getRequestInt("dayNum",1);
        AlpsThreadPool.getInstance().submit(() -> agentRegisterTeacherStatisticsHandler.handle(dateStr,dayNum));
        return MapMessage.successMessage();
    }

//    /**
//     * 测试新注册老师 消息过来时模拟数据
//     *
//     * @return
//     */
//    @RequestMapping(value = "test_teacher_register.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage testTeacherRegister() {
//        Long schoolId = getRequestLong("schoolId");
//        String subject = getRequestString("subject");
//        agentRegisterTeacherStatisticsService.generateRegisterTeacherData(subject,schoolId);
//        return MapMessage.successMessage();
//    }
    /**
     *  工具方法
     *  从summary查专员某天注册老师数
     *
     * @return
     */
    @RequestMapping(value = "user_register_teacher_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage userRegisterTeacherList() {
        MapMessage mapMessage = MapMessage.successMessage();
        Date date = requestDate("date",DateUtils.FORMAT_SQL_DATE);
        Long id = requestLong("id");
        Integer groupOrUser = getRequestInt("groupOrUser",1);
        return mapMessage.add("dataList", agentRegisterTeacherStatisticsService.getUserOrGroupDayRegisterTeachcer(id,groupOrUser,date));
    }
}
