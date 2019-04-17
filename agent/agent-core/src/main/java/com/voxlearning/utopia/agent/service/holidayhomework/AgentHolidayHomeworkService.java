package com.voxlearning.utopia.agent.service.holidayhomework;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumVacationHwIndicator;
import com.voxlearning.utopia.agent.bean.vacationhomework.AgentVacationHwSchoolView;
import com.voxlearning.utopia.agent.bean.vacationhomework.AgentVacationHwSumRankingView;
import com.voxlearning.utopia.agent.bean.vacationhomework.AgentVacationHwSumView;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.indicator.IndicatorService;
import com.voxlearning.utopia.agent.service.indicator.VacationHwIndicatorService;
import com.voxlearning.utopia.agent.service.indicator.support.online.MarketOnlineIndicator;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.support.AgentSchoolLevelSupport;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import org.apache.commons.fileupload.util.LimitedInputStream;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 假期作业service
 * @author deliang.che
 * @since 20108/12/29
 */
@Named
public class AgentHolidayHomeworkService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private PerformanceService performanceService;
    @Inject
    private AgentGroupUserLoaderClient agentGroupUserLoaderClient;
    @Inject
    private VacationHwIndicatorService vacationHwIndicatorService;
    @Inject
    private IndicatorService indicatorService;
    @Inject
    private MarketOnlineIndicator marketOnlineIndicator;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private AgentSchoolLevelSupport agentSchoolLevelSupport;

    public static final Integer PRIMARY_SCHOOL = 1; //小学
    public static final Integer MIDDLE_SCHOOL = 2;  //中学

    /**
     * 统计概览
     * @param userId
     * @param serviceType
     * @param subjectCode
     * @return
     */
    public MapMessage statisticsOverview(Long userId, Integer serviceType, Integer subjectCode){
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        AgentGroupUser currentGroupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
        AgentGroup currentGroup = baseOrgService.getGroupById(currentGroupUser.getGroupId());
        if (currentGroup == null){
            return MapMessage.errorMessage("所属部门不存在！");
        }
        List<Long> ids = new ArrayList<>();
        Integer idType = AgentConstants.INDICATOR_TYPE_GROUP;//1 部门，2 人员
        //全国总监
        if (userRole == AgentRoleType.Country){
            AgentGroup group = null;
            List<AgentGroup> marketingGroupList = baseOrgService.findAllGroups().stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing).collect(Collectors.toList());
            //小学
            if (Objects.equals(serviceType, PRIMARY_SCHOOL)){
                group = marketingGroupList.stream().filter(p -> p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).findFirst().orElse(null);
            //中学
            }else {
                group = marketingGroupList.stream().filter(p -> p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)).findFirst().orElse(null);
            }
            if (group != null){
                ids.add(group.getId());
            }
        }else {
            //市场专员
            if (userRole == AgentRoleType.BusinessDeveloper){
                idType = AgentConstants.INDICATOR_TYPE_USER;
                ids.add(userId);
            }else {
                AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
                ids.add(groupUser.getGroupId());
            }

            if (currentGroup.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
                serviceType = PRIMARY_SCHOOL;
            }else {
                serviceType = MIDDLE_SCHOOL;
            }
        }
        Integer day = performanceService.lastSuccessDataDay();
        Integer schoolLevelFlag = 1;
        if (Objects.equals(serviceType, MIDDLE_SCHOOL)){
            schoolLevelFlag = 24;
        }
        List<Integer> schoolLevels = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(ids.get(0), idType, schoolLevelFlag);
        Map<Long, SumVacationHwIndicator> sumVacationHwIndicatorMap = new HashMap<>();
        if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
            sumVacationHwIndicatorMap.putAll(vacationHwIndicatorService.loadVacationHwGroupSumData(ids,day,schoolLevels,subjectCode));
        }else {
            sumVacationHwIndicatorMap.putAll(vacationHwIndicatorService.loadVacationHwUserSumData(ids,day,schoolLevels,subjectCode));
        }

        Map<String,Object> dataMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(ids)){
            SumVacationHwIndicator sumVacationHwIndicator = sumVacationHwIndicatorMap.get(ids.get(0));
            if (sumVacationHwIndicator != null){
                dataMap.put("assignTeaNum",sumVacationHwIndicator.getVacationHwTeaNum() != null ? sumVacationHwIndicator.getVacationHwTeaNum() : 0);
                dataMap.put("teaCardinality",sumVacationHwIndicator.getTeaScale() != null ? sumVacationHwIndicator.getTeaScale() : 0);
                dataMap.put("assignRate",sumVacationHwIndicator.getVacationHwRate() != null ? sumVacationHwIndicator.getVacationHwRate() : 0);
                dataMap.put("settlementStuNum",sumVacationHwIndicator.getSettleStuNum() != null ? sumVacationHwIndicator.getSettleStuNum() : 0);
            }
        }
        dataMap.put("id",ids.get(0));
        dataMap.put("idType",idType);
        dataMap.put("userRoleType",userRole);
        dataMap.put("serviceType",serviceType);
        return MapMessage.successMessage().add("dataMap",dataMap);
    }

    /**
     * 专员排行
     * @param serviceType
     * @param subjectCode
     * @param currentUser
     * @return
     */
    public MapMessage userRankingList(Integer serviceType,Integer subjectCode,AuthCurrentUser currentUser){
        AgentGroupUser currentGroupUser = baseOrgService.getGroupUserByUser(currentUser.getUserId()).stream().findFirst().orElse(null);
        AgentGroup currentGroup = baseOrgService.getGroupById(currentGroupUser.getGroupId());
        if (currentGroup == null){
            return MapMessage.errorMessage("所属部门不存在！");
        }
        AgentRoleType userRole = baseOrgService.getUserRole(currentUser.getUserId());
        if (userRole != AgentRoleType.Country){
            if (currentGroup.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
                serviceType = PRIMARY_SCHOOL;
            }else {
                serviceType = MIDDLE_SCHOOL;
            }
        }
        //获取所有专员排行信息
        List<AgentVacationHwSumRankingView> rankingVOList = getAllUserRankingList(serviceType,subjectCode,currentUser);
        if (!currentUser.isCountryManager() && !currentUser.isBuManager()) {
            //小学TOP50专员/中学TOP20专员
            final int topCount;
            if (Objects.equals(serviceType, PRIMARY_SCHOOL)) {
                topCount = 50;
            } else {
                topCount = 20;
            }
            //小学TOP50专员/中学TOP20专员+自己负责范围内的所有下属专员或者自己
            rankingVOList = rankingVOList.stream().filter(item -> item.getRanking() <= topCount || item.isBelongToOwnGroup()).collect(Collectors.toList());
        }
        MapMessage mapMessage = MapMessage.successMessage();
        Map<Long, AgentVacationHwSumRankingView> rankingVOMap = rankingVOList.stream().collect(Collectors.toMap(AgentVacationHwSumRankingView::getUserId, Function.identity()));
        AgentVacationHwSumRankingView dataOwnVO = rankingVOMap.get(currentUser.getUserId());
        if (null != dataOwnVO) {
            mapMessage.put("dataOwn", dataOwnVO);
        }
        mapMessage.add("dataList", rankingVOList);
        mapMessage.add("showOnGroupBtn", showJustLookAtOwnGroup(currentUser, 3));
        mapMessage.add("showGroupRanking", !currentUser.isBusinessDeveloper());
        return mapMessage;
    }

    /**
     * 分区排行
     * @param serviceType
     * @param subjectCode
     * @param currentUser
     * @return
     */
    public MapMessage groupRankingList(Integer serviceType,Integer subjectCode,AuthCurrentUser currentUser){
        MapMessage mapMessage = MapMessage.successMessage();
        AgentGroupUser currentGroupUser = baseOrgService.getGroupUserByUser(currentUser.getUserId()).stream().findFirst().orElse(null);
        AgentGroup currentGroup = baseOrgService.getGroupById(currentGroupUser.getGroupId());
        if (currentGroup == null){
            return MapMessage.errorMessage("所属部门不存在！");
        }
        AgentRoleType userRole = baseOrgService.getUserRole(currentUser.getUserId());
        if (userRole != AgentRoleType.Country){
            if (currentGroup.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
                serviceType = PRIMARY_SCHOOL;
            }else {
                serviceType = MIDDLE_SCHOOL;
            }
        }
        //获取所有部门排行信息
        List<AgentVacationHwSumRankingView> rankingVOList = getAllGroupRankingList(serviceType,subjectCode,currentUser);
        if (!currentUser.isCountryManager() && !currentUser.isBuManager()) {
            //小学TOP20分区/中学TOP10分区
            final int topCount;
            if (Objects.equals(serviceType, PRIMARY_SCHOOL)) {
                topCount = 20;
            } else {
                topCount = 10;
            }
            //小学TOP20分区/中学TOP10分区+自己负责的所有下属分区或者自己所属分区
            rankingVOList = rankingVOList.stream().filter(item -> item.getRanking() <= topCount || item.isBelongToOwnGroup()).collect(Collectors.toList());

        }
        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(currentUser.getUserId());
        if (CollectionUtils.isNotEmpty(groupUserList)) {
            Map<Long, AgentVacationHwSumRankingView> rankingVOMap = rankingVOList.stream().collect(Collectors.toMap(AgentVacationHwSumRankingView::getGroupId, Function.identity()));
            AgentVacationHwSumRankingView dataOwnVO = rankingVOMap.get(groupUserList.get(0).getGroupId());
            if (null != dataOwnVO) {
                mapMessage.put("dataOwn", dataOwnVO);
            }
        }
        mapMessage.add("dataList", rankingVOList);
        mapMessage.add("showOnGroupBtn", showJustLookAtOwnGroup(currentUser, 1));
        return mapMessage;
    }

    /**
     * 是否显示“仅看本区”
     * @param authCurrentUser
     * @param rankingType     1：部门排行榜，3：专员排行榜
     * @return
     */
    public boolean showJustLookAtOwnGroup(AuthCurrentUser authCurrentUser, int rankingType) {
        if (null != authCurrentUser) {
            return authCurrentUser.isRegionManager() || authCurrentUser.isAreaManager() || (authCurrentUser.isCityManager() && rankingType != 1);
        }
        return false;
    }

    /**
     * 获取所有专员排行
     * @param serviceType
     * @param subjectCode
     * @param authCurrentUser
     * @return
     */
    public List<AgentVacationHwSumRankingView> getAllUserRankingList(Integer serviceType, Integer subjectCode, AuthCurrentUser authCurrentUser){
        Integer day = performanceService.lastSuccessDataDay();
        //获取与昨日对比数据
        List<AgentVacationHwSumRankingView> agentVacationHwSumRankingViewList = getUserRankingWithPreviousDay(day, serviceType, subjectCode);
        //填充数据属性，是否属于本区
        fillBelongToOwnGroup(agentVacationHwSumRankingViewList,authCurrentUser);
        return agentVacationHwSumRankingViewList;
    }

    /**
     * 获取所有部门排行
     * @param serviceType
     * @param subjectCode
     * @param authCurrentUser
     * @return
     */
    public List<AgentVacationHwSumRankingView> getAllGroupRankingList(Integer serviceType, Integer subjectCode, AuthCurrentUser authCurrentUser){
        Integer day = performanceService.lastSuccessDataDay();
        //获取与昨日对比数据
        List<AgentVacationHwSumRankingView> agentVacationHwSumRankingViewList = getCityRankingWithPreviousDay(day, serviceType, subjectCode);
        //填充数据属性，是否属于本区
        fillBelongToOwnGroup(agentVacationHwSumRankingViewList,authCurrentUser);
        return agentVacationHwSumRankingViewList;
    }


    /**
     * 专员排行，与昨日数据对比
     * @param day
     * @param serviceType
     * @param subjectCode
     * @return
     */
    private List<AgentVacationHwSumRankingView> getUserRankingWithPreviousDay(Integer day, Integer serviceType, Integer subjectCode) {
        //获取最近更新当天指标数据
        List<AgentVacationHwSumRankingView> lastDayList = getAllUserRanking(day,serviceType,subjectCode);
        List<AgentGroupUser> groupUsersList = agentGroupUserLoaderClient.findAll();
        Map<Long, AgentGroupUser> agentGroupUserMap = groupUsersList.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, Function.identity(), (o1, o2) -> o1));
        Map<Long, AgentGroup> agentGroupMap = baseOrgService.findAllGroups().stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity()));
        Integer firstDayOfMonth = Integer.valueOf(DateUtils.dateToString(MonthRange.current().getStartDate(), "yyyyMMdd"));
        //获取昨日指标数据
        List<AgentVacationHwSumRankingView> yesterdayDayList = new ArrayList<>();
        if (!firstDayOfMonth.equals(day)) {
            Date dayDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
            Date yesterdayDate = DateUtils.addDays(dayDate, -1);
            Integer yesterday = Integer.valueOf(DateUtils.dateToString(yesterdayDate, "yyyyMMdd"));
            yesterdayDayList.addAll(getAllUserRanking(yesterday,serviceType,subjectCode));
        }
        Map<Long, AgentVacationHwSumRankingView> yesterdayDataMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(yesterdayDayList)) {
            yesterdayDataMap.putAll(yesterdayDayList.stream().collect(Collectors.toMap(AgentVacationHwSumRankingView::getUserId, Function.identity(), (o1, o2) -> o1)));
        }
        lastDayList.forEach(item -> {
            Long itemUserId = item.getUserId();
            AgentGroupUser agentGroupUser = agentGroupUserMap.get(itemUserId);
            if (null != agentGroupUser) {
                AgentGroup agentGroup = agentGroupMap.get(agentGroupUser.getGroupId());
                if (null != agentGroup) {
                    item.setGroupName(agentGroup.getGroupName());
                    item.setGroupId(agentGroup.getId());
                }
            }
            //与昨日数据对比
            if (yesterdayDataMap.containsKey(item.getUserId())) {
                AgentVacationHwSumRankingView yesterdayDataView = yesterdayDataMap.get(item.getUserId());
                if (item.getRanking() < yesterdayDataView.getRanking()) {
                    item.setGrowthSituation(1);
                } else if (item.getRanking() == yesterdayDataView.getRanking()) {
                    item.setGrowthSituation(0);
                } else {
                    item.setGrowthSituation(-1);
                }
                item.setRankingFloat(yesterdayDataView.getRanking() - item.getRanking());
            }
        });
        return lastDayList;
    }


    /**
     * 分区排行，与昨日数据对比
     * @param day
     * @param serviceType
     * @param subjectCode
     * @return
     */
    private List<AgentVacationHwSumRankingView> getCityRankingWithPreviousDay(Integer day, Integer serviceType, Integer subjectCode) {
        //获取最近更新当天指标数据
        List<AgentVacationHwSumRankingView> lastDayMap = getAllCityRanking(day,serviceType,subjectCode);
        Integer firstDayOfMonth = Integer.valueOf(DateUtils.dateToString(MonthRange.current().getStartDate(), "yyyyMMdd"));
        //获取昨日指标数据
        List<AgentVacationHwSumRankingView> yesterdayDayList = new ArrayList<>();
        if (!firstDayOfMonth.equals(day)) {
            Date dayDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
            Date yesterdayDate = DateUtils.addDays(dayDate, -1);
            Integer yesterday = Integer.valueOf(DateUtils.dateToString(yesterdayDate, "yyyyMMdd"));
            yesterdayDayList.addAll(getAllCityRanking(yesterday,serviceType,subjectCode));
        }

        Map<Long, AgentVacationHwSumRankingView> yesterdayDataMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(yesterdayDayList)) {
            yesterdayDataMap.putAll(yesterdayDayList.stream().collect(Collectors.toMap(AgentVacationHwSumRankingView::getGroupId, Function.identity(), (o1, o2) -> o1)));
        }
        lastDayMap.forEach(item -> {
            if (yesterdayDataMap.containsKey(item.getGroupId())) {
                AgentVacationHwSumRankingView yesterdayView = yesterdayDataMap.get(item.getGroupId());
                if (item.getRanking() < yesterdayView.getRanking()) {
                    item.setGrowthSituation(1);
                } else if (item.getRanking() == yesterdayView.getRanking()) {
                    item.setGrowthSituation(0);
                } else {
                    item.setGrowthSituation(-1);
                }
                item.setRankingFloat(yesterdayView.getRanking() - item.getRanking());
            }
        });
        return lastDayMap;
    }


    /**
     * 获取所有专员排行
     *
     * @param day
     * @return
     */
    private List<AgentVacationHwSumRankingView> getAllUserRanking(Integer day,Integer serviceType,Integer subjectCode) {
        if (null == day) {
            day = performanceService.lastSuccessDataDay();
        }
        //获取所有部门
        List<AgentGroup> allGroups = baseOrgService.findAllGroups();
        List<Long> cityGroupIds = new ArrayList<>();
        List<Integer> schoolLevels = new ArrayList<>();
        if (Objects.equals(serviceType, PRIMARY_SCHOOL)){
            //级别为分区、业务类型为小学的部门
            cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).map(AgentGroup::getId).collect(Collectors.toList()));

            schoolLevels.add(1);
        }else {
            //级别为分区、业务类型为初中和高中的部门
            cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).map(AgentGroup::getId).collect(Collectors.toList()));

            schoolLevels.add(2);
            schoolLevels.add(4);
        }

        //获取对应部门下的专员
        List<Long> businessDeveloperIds = baseOrgService.getUserByGroupIdsAndRole(cityGroupIds, AgentRoleType.BusinessDeveloper);
        List<AgentUser> businessDeveloperList = baseOrgService.getUsers(businessDeveloperIds);

        //假期作业统计接口-user
        Map<Long, SumVacationHwIndicator> sumVacationHwIndicatorMap = vacationHwIndicatorService.loadVacationHwUserSumData(businessDeveloperIds, day, schoolLevels,subjectCode);
        //指标数据
        List<AgentVacationHwSumRankingView> dataList = new ArrayList<>();
        businessDeveloperList.forEach(item -> {
            SumVacationHwIndicator sumVacationHwIndicator = sumVacationHwIndicatorMap.get(item.getId());
            if (sumVacationHwIndicator != null) {
                AgentVacationHwSumRankingView dataVo = new AgentVacationHwSumRankingView();
                dataVo.setUserName(item.getRealName());
                dataVo.setUserAvatar(item.getAvatar());
                dataVo.setUserId(item.getId());
                dataVo.setIndicatorValue(sumVacationHwIndicator.getVacationHwRate() != null ? sumVacationHwIndicator.getVacationHwRate() : 0);
                dataList.add(dataVo);
            }
        });
        dataList.sort((o1, o2) -> {
            return -Double.compare(o1.getIndicatorValue(), o2.getIndicatorValue());
        });

        for (int i = 0; i < dataList.size(); i++) {
            if (i > 0 && dataList.get(i).getIndicatorValue() == dataList.get(i - 1).getIndicatorValue()) {
                dataList.get(i).setRanking(dataList.get(i - 1).getRanking());
            } else {
                dataList.get(i).setRanking(i + 1);
            }
        }

        return dataList;
    }


    /**
     * 获取所有分区的排行
     * @param day
     * @param serviceType
     * @param subjectCode
     * @return
     */
    private List<AgentVacationHwSumRankingView> getAllCityRanking(Integer day,Integer serviceType,Integer subjectCode) {
        if (null == day) {
            day = performanceService.lastSuccessDataDay();
        }
        //获取所有部门
        List<AgentGroup> allGroups = baseOrgService.findAllGroups();
        Map<Long,AgentGroup> cityGroupMap = new HashMap<>();
        List<Integer> schoolLevels = new ArrayList<>();
        if (Objects.equals(serviceType, PRIMARY_SCHOOL)){
            //级别为分区、业务类型为小学的部门
            cityGroupMap = allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).collect(Collectors.toMap(AgentGroup::getId, Function.identity()));

            schoolLevels.add(1);
        }else {
            //级别为分区、业务类型为初中和高中的部门
            cityGroupMap = allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).collect(Collectors.toMap(AgentGroup::getId, Function.identity()));

            schoolLevels.add(2);
            schoolLevels.add(4);
        }

        if (MapUtils.isEmpty(cityGroupMap)) {
            return Collections.emptyList();
        }

        Map<Long, SumVacationHwIndicator> sumVacationHwIndicatorMap = vacationHwIndicatorService.loadVacationHwGroupSumData(cityGroupMap.keySet(), day, schoolLevels,subjectCode);
        //指标数据
        List<AgentVacationHwSumRankingView> dataList = new ArrayList<>();
        cityGroupMap.forEach((key, group) -> {
            SumVacationHwIndicator sumVacationHwIndicator = sumVacationHwIndicatorMap.get(key);
            if (sumVacationHwIndicator != null) {
                AgentVacationHwSumRankingView dataVo = new AgentVacationHwSumRankingView();
                dataVo.setGroupId(key);
                dataVo.setGroupName(group.getGroupName());
                //拼装指标数据
                dataVo.setIndicatorValue(sumVacationHwIndicator.getVacationHwRate() != null ? sumVacationHwIndicator.getVacationHwRate() : 0);
                dataList.add(dataVo);
            }
        });
        dataList.sort((o1, o2) -> {
            return -Double.compare(o1.getIndicatorValue(), o2.getIndicatorValue());
        });
        for (int i = 0; i < dataList.size(); i++) {
            if (i > 0 && dataList.get(i).getIndicatorValue() == dataList.get(i - 1).getIndicatorValue()) {
                dataList.get(i).setRanking(dataList.get(i - 1).getRanking());
            } else {
                dataList.get(i).setRanking(i + 1);
            }
        }
        return dataList;
    }


    /**
     * 填充数据属性，是否属于本区
     * @param rankingVOList
     * @param authCurrentUser
     */
    private void fillBelongToOwnGroup(List<AgentVacationHwSumRankingView> rankingVOList, AuthCurrentUser authCurrentUser) {
        //本部门+所有子部门ID
        Set<Long> ownGroupIdSet = new HashSet<>();
        if (authCurrentUser.isRegionManager() || authCurrentUser.isAreaManager() || authCurrentUser.isCityManager()) {
            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(authCurrentUser.getUserId());
            if (CollectionUtils.isNotEmpty(groupUserList)) {
                Long groupId = groupUserList.get(0).getGroupId();
                ownGroupIdSet.add(groupId);
                ownGroupIdSet.addAll(baseOrgService.getSubGroupList(groupId).stream().map(AgentGroup::getId).collect(Collectors.toSet()));
            }
        }
        //判断是否属于本区
        rankingVOList.forEach(item -> {
            if (authCurrentUser.isBusinessDeveloper() && item.getUserId() != null) {
                item.setBelongToOwnGroup(item.getUserId().equals(authCurrentUser.getUserId()));
            } else {
                item.setBelongToOwnGroup(ownGroupIdSet.contains(item.getGroupId()));
            }
            item.setInSameRegionGroup(item.isBelongToOwnGroup());
        });
    }

    /**
     * 统计列表
     * @param id
     * @param idType
     * @param dimension
     * @param subjectCode
     * @return
     */
    public MapMessage statisticsList(Long id,Integer idType,Integer dimension,Integer subjectCode){
        MapMessage message = MapMessage.successMessage();
        Integer day = performanceService.lastSuccessDataDay();
        List<AgentVacationHwSumView> dataList = new ArrayList<>();
        if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)) {
            AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(id);
            message.add("groupRoleType", groupRoleType);
            //检查参数组合
            boolean flag = indicatorService.judgeGroupDimension(groupRoleType, dimension);
            if(!flag){
                return MapMessage.errorMessage("参数组合有误！");
            }
            AgentGroup group = baseOrgService.getGroupById(id);
            if (group == null){
                return MapMessage.errorMessage("部门不存在！");
            }
            //获取学校阶段
            Integer schoolLevelFlag = 1;
            if (group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)){
                schoolLevelFlag = 24;
            }
            List<Integer> schoolLevels = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, AgentConstants.INDICATOR_TYPE_GROUP, schoolLevelFlag);
            // 专员列表
            if(dimension == 5 || (groupRoleType == AgentGroupRoleType.City && dimension == 1)){
                List<Long> userIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(id, AgentRoleType.BusinessDeveloper.getId());
                dataList.addAll(generateUserVacationHwViewList(userIds,day, schoolLevels, subjectCode));
                // 分区的情况下， 设置未分配数据
                if(groupRoleType == AgentGroupRoleType.City && dimension == 1){
                    //获取部门未分配学校
                    List<Long> unallocatedSchoolIds = getUnallocatedSchoolIds(id, schoolLevels);
                    if (CollectionUtils.isNotEmpty(unallocatedSchoolIds)){
                        //拼装假期作业部门未分配view数据
                        dataList.add(generateUnallocatedHwViewList(id,unallocatedSchoolIds,day,subjectCode));
                    }
                }
                // 部门列表
            }else {
                dataList.addAll(generateGroupVacationHwViewList(id,groupRoleType,day,dimension,schoolLevels,subjectCode));
            }

            for (AgentVacationHwSumView item : dataList){
                item.setServiceType(schoolLevelFlag == 24 ? MIDDLE_SCHOOL : PRIMARY_SCHOOL);
                item.setClickable(true);           // 默认可点击下钻
                item.setSelf(false);               // 是否是当前部门或用户
                // 当前用户或部门的情况下
                if (Objects.equals(item.getId(), id) && Objects.equals(item.getIdType(), String.valueOf(idType))) {
                    item.setSelf(true);
                }
                // 大区看大区， 区域看区域， 分区看分区的情况下，前端页面不能点击下钻
                if((groupRoleType == AgentGroupRoleType.Region && dimension == 2) || (groupRoleType == AgentGroupRoleType.Area && dimension == 3) || (groupRoleType == AgentGroupRoleType.City && dimension == 4)) {
                    item.setClickable(false);           // 不可点击下钻
                }
            }
            // 当前用户是专员的情况下， 返回专员所在部门的专员列表
        }else if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
            List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(id);
            if(CollectionUtils.isNotEmpty(groupIdList)){
                Long groupId = groupIdList.get(0);
                AgentGroup group = baseOrgService.getGroupById(groupId);
                if (group == null){
                    return MapMessage.errorMessage("部门不存在！");
                }
                //获取学校阶段
                Integer schoolLevelFlag = 1;
                if (group.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || group.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)){
                    schoolLevelFlag = 24;
                }
                List<Integer> schoolLevels = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, AgentConstants.INDICATOR_TYPE_USER, schoolLevelFlag);
                List<Long> userIds = baseOrgService.getGroupUsersByRole(groupId, AgentRoleType.BusinessDeveloper);
                dataList.addAll(generateUserVacationHwViewList(userIds, day, schoolLevels,subjectCode));
                for (AgentVacationHwSumView item : dataList){
                    item.setServiceType(schoolLevelFlag == 24 ? MIDDLE_SCHOOL : PRIMARY_SCHOOL);
                    item.setClickable(false);           // 不可点击下钻
                    item.setSelf(false);               // 是否是当前部门或用户
                    // 当前用户或部门的情况下
                    if(Objects.equals(item.getId(), id) && Objects.equals(String.valueOf(item.getIdType()), String.valueOf(idType))){
                        item.setClickable(true);
                        item.setSelf(true);
                    }
                }
            }
        }
        message.add("dataList", dataList);
        return message;
    }


    /**
     * 拼装假期作业部门view数据
     * @param groupId
     * @param groupRoleType
     * @param day
     * @param dimension
     * @param schoolLevels
     * @param subjectCode
     * @return
     */
    public List<AgentVacationHwSumView> generateGroupVacationHwViewList(Long groupId, AgentGroupRoleType groupRoleType, Integer day, int dimension,List<Integer> schoolLevels,Integer subjectCode){
        List<AgentVacationHwSumView> resultList = new ArrayList<>();

        Collection<Long> targetGroupList = indicatorService.fetchGroupList(groupId, groupRoleType, dimension);
        if(CollectionUtils.isEmpty(targetGroupList)){
            return resultList;
        }
        //获取假期作业部门指标数据
        Map<Long, SumVacationHwIndicator> sumVacationHwIndicatorMap = vacationHwIndicatorService.loadVacationHwGroupSumData(targetGroupList, day, schoolLevels, subjectCode);
        resultList.addAll(copyVacationHwSumProperties(sumVacationHwIndicatorMap));
        return resultList;
    }


    /**
     * 拼装假期作业人员view数据
     * @param userIds
     * @param day
     * @param schoolLevels
     * @param subjectCode
     * @return
     */
    public List<AgentVacationHwSumView>  generateUserVacationHwViewList(Collection<Long> userIds, Integer day,List<Integer> schoolLevels,Integer subjectCode) {
        List<AgentVacationHwSumView> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(userIds)) {
            return resultList;
        }
        //获取假期作业人员指标数据
        Map<Long, SumVacationHwIndicator> sumVacationHwIndicatorMap = vacationHwIndicatorService.loadVacationHwUserSumData(userIds, day, schoolLevels, subjectCode);
        resultList.addAll(copyVacationHwSumProperties(sumVacationHwIndicatorMap));
        return resultList;
    }

    /**
     * 获取部门未分配学校
     * @param groupId
     * @param schoolLevels
     * @return
     */
    public List<Long> getUnallocatedSchoolIds(Long groupId, List<Integer> schoolLevels) {
        List<Long> schoolIds = new ArrayList<>();
        List<Long> unallocatedSchoolIds = baseOrgService.getCityManageOtherSchoolByGroupId(groupId).stream().collect(Collectors.toList());
        schoolIds.addAll(baseOrgService.getSchoolListByLevels(unallocatedSchoolIds, schoolLevels.stream().map(SchoolLevel::safeParse).collect(Collectors.toList())));
        return schoolIds;
    }

    /**
     * 拼装假期作业部门未分配view数据
     * @param groupId
     * @param schoolIds
     * @param day
     * @param subjectCode
     * @return
     */
    public AgentVacationHwSumView generateUnallocatedHwViewList(Long groupId,Collection<Long> schoolIds,Integer day,Integer subjectCode){
        //获取寒假作业学校指标数据
        Map<Long, SumVacationHwIndicator> sumVacationHwIndicatorMap = vacationHwIndicatorService.loadVacationHwSchoolData(schoolIds, day, subjectCode);
        AgentVacationHwSumView vacationHwSumView = new AgentVacationHwSumView();
        double vacationHwTeaNum = 0;
        double teaScale = 0;
        double settleStuNum = 0;
        for (SumVacationHwIndicator item :sumVacationHwIndicatorMap.values()){
            settleStuNum = MathUtils.doubleAdd(settleStuNum,item.getSettleStuNum() != null? item.getSettleStuNum() : 0);
            vacationHwTeaNum = MathUtils.doubleAdd(vacationHwTeaNum,item.getVacationHwTeaNum() != null ? item.getVacationHwTeaNum() : 0);
            teaScale = MathUtils.doubleAdd(teaScale,item.getTeaScale() != null ? item.getTeaScale() : 0);
        }
        vacationHwSumView.setId(groupId);
        vacationHwSumView.setIdType(AgentConstants.INDICATOR_TYPE_UNALLOCATED);
        vacationHwSumView.setName("未分配");
        vacationHwSumView.setSettleStuNum(MathUtils.doubleToInt(settleStuNum,BigDecimal.ROUND_UP));
        vacationHwSumView.setVacationHwTeaNum(MathUtils.doubleToInt(vacationHwTeaNum,BigDecimal.ROUND_UP));
        vacationHwSumView.setTeaScale(MathUtils.doubleToInt(teaScale,BigDecimal.ROUND_UP));
        vacationHwSumView.setVacationHwRate(MathUtils.doubleDivide(vacationHwTeaNum,teaScale,4));
        return vacationHwSumView;
    }

    /**
     * 假期作业指标字段赋值
     * @param sumVacationHwIndicatorMap
     * @return
     */
    private List<AgentVacationHwSumView> copyVacationHwSumProperties(Map<Long, SumVacationHwIndicator> sumVacationHwIndicatorMap){
        List<AgentVacationHwSumView> resultList = new ArrayList<>();
        if (MapUtils.isEmpty(sumVacationHwIndicatorMap)){
            return resultList;
        }
        sumVacationHwIndicatorMap.forEach((k,v) -> {
            AgentVacationHwSumView agentVacationHwSumView = new AgentVacationHwSumView();
            try {
                BeanUtils.copyProperties(agentVacationHwSumView,v);
                agentVacationHwSumView.setVacationHwTeaNum(v.getVacationHwTeaNum() != null ? v.getVacationHwTeaNum() : 0);
                agentVacationHwSumView.setTeaScale(v.getTeaScale() != null ? v.getTeaScale() : 0);
                agentVacationHwSumView.setSettleStuNum(v.getSettleStuNum() != null ? v.getSettleStuNum() : 0);
                agentVacationHwSumView.setVacationHwRate(v.getVacationHwRate() != null ? v.getVacationHwRate() : 0);
            } catch (Exception e) {
                logger.error("AgentVacationHwSumView copy error" + e);
            }
            resultList.add(agentVacationHwSumView);
        });
        return resultList;
    }


    /**
     * 学校列表
     * @param id
     * @param idType
     * @param serviceType
     * @param subjectCode
     * @return
     */
    public MapMessage schoolList(Long id, Integer idType, Integer serviceType,Integer subjectCode){
        List<AgentVacationHwSchoolView> dataList = new ArrayList<>();
        Integer day = performanceService.lastSuccessDataDay();
        Collection<Long> schoolIds = marketOnlineIndicator.fetchSchoolList(id, idType, serviceType);
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();
        //获取寒假作业学校指标数据
        Map<Long, SumVacationHwIndicator> sumVacationHwIndicatorMap = vacationHwIndicatorService.loadVacationHwSchoolData(schoolIds, day, subjectCode);
        sumVacationHwIndicatorMap.forEach((k,v) -> {
            AgentVacationHwSchoolView agentVacationHwSchoolView = new AgentVacationHwSchoolView();
            try {
                agentVacationHwSchoolView.setId(k);
                School school = schoolMap.get(k);
                if (school != null){
                    agentVacationHwSchoolView.setName(school.getCname());
                }
                agentVacationHwSchoolView.setVacationHwTeaNum(v.getVacationHwTeaNum() != null ? v.getVacationHwTeaNum() : 0);
                agentVacationHwSchoolView.setTeaScale(v.getTeaScale() != null ? v.getTeaScale() : 0);
                agentVacationHwSchoolView.setSettleStuNum(v.getSettleStuNum() != null ? v.getSettleStuNum() : 0);
                agentVacationHwSchoolView.setAuthUnAssignNum(v.getAuthUnAssignNum() != null ? v.getAuthUnAssignNum() : 0);
                dataList.add(agentVacationHwSchoolView);
            } catch (Exception e) {
                logger.error("AgentVacationHwSchoolView copy error" + e);
            }
        });
        return MapMessage.successMessage().add("dataList",dataList.stream().sorted((o1, o2) -> Integer.compare(o2.getAuthUnAssignNum(),o1.getAuthUnAssignNum())).collect(Collectors.toList()));
    }


    /**
     * 如果是市场总监，获取相应的市场部
     * @param serviceType
     */
    public Long getMarketingGroupId(Integer serviceType){
        Long id = 0L;
        AgentGroup group = null;
        List<AgentGroup> marketingGroupList = baseOrgService.findAllGroups().stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing).collect(Collectors.toList());
        //小学
        if (serviceType == 1){
            group = marketingGroupList.stream().filter(p -> p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).findFirst().orElse(null);
            //中学
        }else {
            group = marketingGroupList.stream().filter(p -> p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL)).findFirst().orElse(null);
        }
        if (group != null){
            id = group.getId();
        }
        return id;
    }

}
