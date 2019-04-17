package com.voxlearning.utopia.agent.service.ranking;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumParentIndicator;
import com.voxlearning.utopia.agent.bean.ranking.AgentPerformanceRankingVO;
import com.voxlearning.utopia.agent.bean.ranking.PerformanceRankingSumDataItem;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.dao.mongo.AgentPerformanceRankingDao;
import com.voxlearning.utopia.agent.persist.entity.AgentPerformanceRanking;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.indicator.OnlineIndicatorService;
import com.voxlearning.utopia.agent.service.indicator.ParentIndicatorService;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 排行榜服务
 *
 * @author chunlin.yu
 * @create 2018-03-21 20:17
 **/
@Named
public class AgentRankingService extends AbstractAgentService {

    public static final String DAY_DATA_TYPE = "dayList";
    public static final String MONTH_DATA_TYPE = "monthList";
    public static final String DAY_OWN_DATA_TYPE = "dayOwn";
    public static final String MONTH_OWN_DATA_TYPE = "monthOwn";

    public static final String SUMMARY_DATA_TYPE = "summary";

    public static final Integer JUNIOR_SCHOOL_TYPE = 1;
    public static final Integer MIDDLE_SCHOOL_TYPE = 2;
    public static final Integer PARENT_TYPE = 3;

    @Inject
    private AgentPerformanceRankingDao agentPerformanceRankingDao;

    @Inject
    private PerformanceService performanceService;

    @Inject
    private BaseOrgService baseOrgService;

    @Inject
    private AgentGroupUserLoaderClient agentGroupUserLoaderClient;

    @Inject
    private OnlineIndicatorService onlineIndicatorService;

    @Inject
    private ParentIndicatorService parentIndicatorService;

    @Inject
    private AgentSchoolLevelSupport agentSchoolLevelSupport;

    /**
     * 每日生成排行榜数据
     */
    public void generateRanking(int date) {
        AlpsThreadPool.getInstance().submit(() -> {
            Integer lastSuccessDataDay;
            if (date == 0) {
                lastSuccessDataDay = performanceService.lastSuccessDataDay();
            } else {
                lastSuccessDataDay = date;
            }
            agentPerformanceRankingDao.deleteByDay(lastSuccessDataDay);
            Map<String, List<AgentPerformanceRankingVO>> map = dealUserRankingWithPreviousDay(IndicatorType.JUNIOR_REG, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.JUNIOR_REG.getType(), 3);
            map = dealUserRankingWithPreviousDay(IndicatorType.JUNIOR_SglSubj_INC_1, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.JUNIOR_SglSubj_INC_1.getType(), 3);
            map = dealUserRankingWithPreviousDay(IndicatorType.JUNIOR_SglSubj_TEACHER_REG, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.JUNIOR_SglSubj_TEACHER_REG.getType(), 3);
            map = dealUserRankingWithPreviousDay(IndicatorType.MIDDLE_REG, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.MIDDLE_REG.getType(), 3);
            map = dealUserRankingWithPreviousDay(IndicatorType.MIDDLE_ENGLISH_INC_1, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.MIDDLE_ENGLISH_INC_1.getType(), 3);
            map = dealUserRankingWithPreviousDay(IndicatorType.MIDDLE_MATH_INC_1, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.MIDDLE_MATH_INC_1.getType(), 3);
            map = dealUserRankingWithPreviousDay(IndicatorType.MIDDLE_ENGLISH_TEACHER_REG, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.MIDDLE_ENGLISH_TEACHER_REG.getType(), 3);
            map = dealUserRankingWithPreviousDay(IndicatorType.MIDDLE_MATH_TEACHER_REG, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.MIDDLE_MATH_TEACHER_REG.getType(), 3);
            map = dealUserRankingWithPreviousDay(IndicatorType.BIND_STU_PARENT, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.BIND_STU_PARENT.getType(), 3);
            map = dealUserRankingWithPreviousDay(IndicatorType.LOGIN_GTE1_BIND_STU_PARENT, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.LOGIN_GTE1_BIND_STU_PARENT.getType(), 3);
            map = dealUserRankingWithPreviousDay(IndicatorType.PARENT_STU_ACTIVE_SETTLEMENT, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.PARENT_STU_ACTIVE_SETTLEMENT.getType(), 3);


            map = dealCityRankingWithPreviousDay(IndicatorType.JUNIOR_REG, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.JUNIOR_REG.getType(), 1);
            map = dealCityRankingWithPreviousDay(IndicatorType.JUNIOR_SglSubj_INC_1, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.JUNIOR_SglSubj_INC_1.getType(), 1);
            map = dealCityRankingWithPreviousDay(IndicatorType.JUNIOR_SglSubj_TEACHER_REG, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.JUNIOR_SglSubj_TEACHER_REG.getType(), 1);
            map = dealCityRankingWithPreviousDay(IndicatorType.MIDDLE_REG, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.MIDDLE_REG.getType(), 1);
            map = dealCityRankingWithPreviousDay(IndicatorType.MIDDLE_ENGLISH_INC_1, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.MIDDLE_ENGLISH_INC_1.getType(), 1);
            map = dealCityRankingWithPreviousDay(IndicatorType.MIDDLE_MATH_INC_1, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.MIDDLE_MATH_INC_1.getType(), 1);
            map = dealCityRankingWithPreviousDay(IndicatorType.JUNIOR_SglSubj_TEACHER_REG, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.JUNIOR_SglSubj_TEACHER_REG.getType(), 1);
            map = dealCityRankingWithPreviousDay(IndicatorType.MIDDLE_ENGLISH_TEACHER_REG, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.MIDDLE_ENGLISH_TEACHER_REG.getType(), 1);
            map = dealCityRankingWithPreviousDay(IndicatorType.MIDDLE_MATH_TEACHER_REG, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.MIDDLE_MATH_TEACHER_REG.getType(), 1);
            map = dealCityRankingWithPreviousDay(IndicatorType.BIND_STU_PARENT, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.BIND_STU_PARENT.getType(), 1);
            map = dealCityRankingWithPreviousDay(IndicatorType.LOGIN_GTE1_BIND_STU_PARENT, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.LOGIN_GTE1_BIND_STU_PARENT.getType(), 1);
            map = dealCityRankingWithPreviousDay(IndicatorType.PARENT_STU_ACTIVE_SETTLEMENT, lastSuccessDataDay);
            addPerformanceRankingToDB(map, lastSuccessDataDay, IndicatorType.PARENT_STU_ACTIVE_SETTLEMENT.getType(), 1);
        });
    }

    private void addPerformanceRankingToDB(Map<String, List<AgentPerformanceRankingVO>> map, Integer day, Integer indicatorType, Integer rankingType) {
        List<AgentPerformanceRankingVO> dayRankingVOList = map.get(DAY_DATA_TYPE);
        List<AgentPerformanceRanking> rankingList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dayRankingVOList)) {
            dayRankingVOList.forEach(item -> {
                AgentPerformanceRanking ranking = new AgentPerformanceRanking();
                ranking.setDay(day);
                ranking.setGroupId(item.getGroupId());
                ranking.setIndicatorType(indicatorType);
                ranking.setIndicatorValue(item.getIndicatorValue());
                ranking.setGroupName(item.getGroupName());
                ranking.setRanking(item.getRanking());
                ranking.setRankingFloat(item.getRankingFloat());
                ranking.setRannkingDateType(1);
                ranking.setUserId(item.getUserId());
                ranking.setUserName(item.getUserName());
                ranking.setType(rankingType);
                ranking.setTotalCount(dayRankingVOList.size());
                rankingList.add(ranking);
            });
        }
        List<AgentPerformanceRankingVO> monthRankingVOList = map.get(MONTH_DATA_TYPE);
        if (CollectionUtils.isNotEmpty(monthRankingVOList)) {
            monthRankingVOList.forEach(item -> {
                AgentPerformanceRanking ranking = new AgentPerformanceRanking();
                ranking.setDay(day);
                ranking.setGroupId(item.getGroupId());
                ranking.setIndicatorType(indicatorType);
                ranking.setIndicatorValue(item.getIndicatorValue());
                ranking.setGroupName(item.getGroupName());
                ranking.setRanking(item.getRanking());
                ranking.setRankingFloat(item.getRankingFloat());
                ranking.setRannkingDateType(2);
                ranking.setUserId(item.getUserId());
                ranking.setUserName(item.getUserName());
                ranking.setType(rankingType);
                ranking.setTotalCount(monthRankingVOList.size());
                rankingList.add(ranking);
            });
        }
        if (CollectionUtils.isNotEmpty(rankingList)) {
            agentPerformanceRankingDao.inserts(rankingList);
        }
    }


    /**
     * 获取排行前三名摘要
     *
     * @param currentUser
     * @return
     */
    public Map<String, List<AgentPerformanceRankingVO>> getRankingSummary(AuthCurrentUser currentUser,Integer serviceType,Integer dateType,Long id,Integer idType,Integer schoolLevelFlag) {
        Map<String, List<AgentPerformanceRankingVO>> result = new LinkedHashMap<>();
        String dateTypeStr = DAY_DATA_TYPE;
        if (dateType == 2){
            dateTypeStr = MONTH_DATA_TYPE;
        }
        //获取所有部门
        List<AgentGroup> allGroups = baseOrgService.findAllGroups();
        List<AgentGroupUser> groupUsersList = agentGroupUserLoaderClient.findAll();
        Map<Long, AgentGroupUser> userGroupMap = groupUsersList.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, Function.identity(), (o1, o2) -> o1));
        Map<Long, AgentGroup> groupMap = allGroups.stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity()));
        //获取所有专员各个指标的排行信息
        Integer day = performanceService.lastSuccessDataDay();
        Map<String, Map<String, List<AgentPerformanceRankingVO>>> allUserRankingSummary = getAllUserRankingSummary(day, serviceType,allGroups,id,idType,schoolLevelFlag);
        Integer firstDayOfMonth = Integer.valueOf(DateUtils.dateToString(MonthRange.current().getStartDate(), "yyyyMMdd"));
        Map<String, Map<String, List<AgentPerformanceRankingVO>>> yesterdayAllUserRankingSummary = new HashMap<>();
        if (!firstDayOfMonth.equals(day)) {
            Date dayDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
            Date yesterdayDate = DateUtils.addDays(dayDate, -1);
            Integer yesterday = Integer.valueOf(DateUtils.dateToString(yesterdayDate, "yyyyMMdd"));
            yesterdayAllUserRankingSummary.putAll(getAllUserRankingSummary(yesterday, serviceType,allGroups,id,idType,schoolLevelFlag));
        }
        //小学作业
        if (serviceType.equals(JUNIOR_SCHOOL_TYPE)){
            Map<String, List<AgentPerformanceRankingVO>> ranking1 = getUserRankingWithOwnGroupSignSummary(currentUser,IndicatorType.JUNIOR_REG,allUserRankingSummary,yesterdayAllUserRankingSummary,userGroupMap,groupMap);
            Map<String, List<AgentPerformanceRankingVO>> ranking4 = getUserRankingWithOwnGroupSignSummary(currentUser,IndicatorType.JUNIOR_SglSubj_INC_1,allUserRankingSummary,yesterdayAllUserRankingSummary,userGroupMap,groupMap);
            Map<String, List<AgentPerformanceRankingVO>> ranking5 = getUserRankingWithOwnGroupSignSummary(currentUser,IndicatorType.JUNIOR_SglSubj_TEACHER_REG,allUserRankingSummary,yesterdayAllUserRankingSummary,userGroupMap,groupMap);

            result.put(IndicatorType.JUNIOR_REG.name(), ranking1.get(dateTypeStr).stream().filter(item -> item.getIndicatorValue() > 0).limit(3).collect(Collectors.toList()));
            result.put(IndicatorType.JUNIOR_SglSubj_INC_1.name(), ranking4.get(dateTypeStr).stream().filter(item -> item.getIndicatorValue() > 0).limit(3).collect(Collectors.toList()));
            result.put(IndicatorType.JUNIOR_SglSubj_TEACHER_REG.name(), ranking5.get(dateTypeStr).stream().filter(item -> item.getIndicatorValue() > 0).limit(3).collect(Collectors.toList()));
            //中学作业
        }else if (serviceType.equals(MIDDLE_SCHOOL_TYPE)){
            Map<String, List<AgentPerformanceRankingVO>> ranking7 = getUserRankingWithOwnGroupSignSummary(currentUser,IndicatorType.MIDDLE_REG,allUserRankingSummary,yesterdayAllUserRankingSummary,userGroupMap,groupMap);
            Map<String, List<AgentPerformanceRankingVO>> ranking8 = getUserRankingWithOwnGroupSignSummary(currentUser,IndicatorType.MIDDLE_ENGLISH_INC_1,allUserRankingSummary,yesterdayAllUserRankingSummary,userGroupMap,groupMap);
            Map<String, List<AgentPerformanceRankingVO>> ranking9 = getUserRankingWithOwnGroupSignSummary(currentUser,IndicatorType.MIDDLE_MATH_INC_1,allUserRankingSummary,yesterdayAllUserRankingSummary,userGroupMap,groupMap);
            Map<String, List<AgentPerformanceRankingVO>> ranking10 = getUserRankingWithOwnGroupSignSummary(currentUser,IndicatorType.MIDDLE_ENGLISH_TEACHER_REG,allUserRankingSummary,yesterdayAllUserRankingSummary,userGroupMap,groupMap);
            Map<String, List<AgentPerformanceRankingVO>> ranking11 = getUserRankingWithOwnGroupSignSummary(currentUser,IndicatorType.MIDDLE_MATH_TEACHER_REG,allUserRankingSummary,yesterdayAllUserRankingSummary,userGroupMap,groupMap);

            result.put(IndicatorType.MIDDLE_REG.name(), ranking7.get(dateTypeStr).stream().filter(item -> item.getIndicatorValue() > 0).limit(3).collect(Collectors.toList()));
            result.put(IndicatorType.MIDDLE_ENGLISH_INC_1.name(), ranking8.get(dateTypeStr).stream().filter(item -> item.getIndicatorValue() > 0).limit(3).collect(Collectors.toList()));
            result.put(IndicatorType.MIDDLE_MATH_INC_1.name(), ranking9.get(dateTypeStr).stream().filter(item -> item.getIndicatorValue() > 0).limit(3).collect(Collectors.toList()));
            result.put(IndicatorType.MIDDLE_ENGLISH_TEACHER_REG.name(), ranking10.get(dateTypeStr).stream().filter(item -> item.getIndicatorValue() > 0).limit(3).collect(Collectors.toList()));
            result.put(IndicatorType.MIDDLE_MATH_TEACHER_REG.name(), ranking11.get(dateTypeStr).stream().filter(item -> item.getIndicatorValue() > 0).limit(3).collect(Collectors.toList()));
            //家长
        }else if (serviceType.equals(PARENT_TYPE)){
            Map<String, List<AgentPerformanceRankingVO>> ranking15 = getUserRankingWithOwnGroupSignSummary(currentUser,IndicatorType.BIND_STU_PARENT,allUserRankingSummary,yesterdayAllUserRankingSummary,userGroupMap,groupMap);
            Map<String, List<AgentPerformanceRankingVO>> ranking16 = getUserRankingWithOwnGroupSignSummary(currentUser,IndicatorType.LOGIN_GTE1_BIND_STU_PARENT,allUserRankingSummary,yesterdayAllUserRankingSummary,userGroupMap,groupMap);
            Map<String, List<AgentPerformanceRankingVO>> ranking17 = getUserRankingWithOwnGroupSignSummary(currentUser,IndicatorType.PARENT_STU_ACTIVE_SETTLEMENT,allUserRankingSummary,yesterdayAllUserRankingSummary,userGroupMap,groupMap);

            result.put(IndicatorType.BIND_STU_PARENT.name(), ranking15.get(dateTypeStr).stream().filter(item -> item.getIndicatorValue() > 0).limit(3).collect(Collectors.toList()));
            result.put(IndicatorType.LOGIN_GTE1_BIND_STU_PARENT.name(), ranking16.get(dateTypeStr).stream().filter(item -> item.getIndicatorValue() > 0).limit(3).collect(Collectors.toList()));
            result.put(IndicatorType.PARENT_STU_ACTIVE_SETTLEMENT.name(), ranking17.get(dateTypeStr).stream().filter(item -> item.getIndicatorValue() > 0).limit(3).collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * 获取所有排名，包含是否属于自己部门的标志
     *
     * @param indicatorType
     * @param authCurrentUser
     * @return
     */
    public Map<String, List<AgentPerformanceRankingVO>> getAllUserRankingWithOwnGroupSign(IndicatorType indicatorType, AuthCurrentUser authCurrentUser) {
        Integer day = performanceService.lastSuccessDataDay();
        Map<String, List<AgentPerformanceRankingVO>> lastDayMap = dealUserRankingWithPreviousDay(indicatorType, day);
        fillBelongToOwnGroup(lastDayMap, authCurrentUser);
        return lastDayMap;
    }


    public Map<String, List<AgentPerformanceRankingVO>> getUserRankingWithOwnGroupSignSummary(AuthCurrentUser authCurrentUser,
                                                                                              IndicatorType indicatorType,
                                                                                              Map<String, Map<String, List<AgentPerformanceRankingVO>>> allUserRankingSummary,
                                                                                              Map<String, Map<String, List<AgentPerformanceRankingVO>>> yesterdayAllUserRankingSummary,
                                                                                              Map<Long, AgentGroupUser> userGroupMap,
                                                                                              Map<Long, AgentGroup> groupMap) {
        Map<String, List<AgentPerformanceRankingVO>> lastDayMap = allUserRankingSummary.get(indicatorType.name());
        Map<String, List<AgentPerformanceRankingVO>> yesterdayDayMap = yesterdayAllUserRankingSummary.get(indicatorType.name());
        Map<String, List<AgentPerformanceRankingVO>> lastDayMapResult = dealUserRankingWithPreviousDaySummary(lastDayMap,yesterdayDayMap,userGroupMap,groupMap);
        fillBelongToOwnGroup(lastDayMapResult, authCurrentUser);
        return lastDayMapResult;
    }


    /**
     * 获取所有专员排行，打入了与昨天的对比数据
     *
     * @param indicatorType
     * @param baseDay
     * @return
     */
    private Map<String, List<AgentPerformanceRankingVO>> dealUserRankingWithPreviousDay(IndicatorType indicatorType, Integer baseDay) {
        Map<String, List<AgentPerformanceRankingVO>> lastDayMap = getAllUserRanking(indicatorType, baseDay);
        List<AgentGroupUser> groupUsersList = agentGroupUserLoaderClient.findAll();
        Map<Long, AgentGroupUser> agentGroupUserMap = groupUsersList.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, Function.identity(), (o1, o2) -> o1));
        Map<Long, AgentGroup> agentGroupMap = baseOrgService.findAllGroups().stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity()));
        Integer firstDayOfMonth = Integer.valueOf(DateUtils.dateToString(MonthRange.current().getStartDate(), "yyyyMMdd"));
        Map<String, List<AgentPerformanceRankingVO>> yesterdayDayMap = new HashMap<>();
        if (!firstDayOfMonth.equals(baseDay)) {
            Date dayDate = DateUtils.stringToDate(String.valueOf(baseDay), "yyyyMMdd");
            Date yesterdayDate = DateUtils.addDays(dayDate, -1);
            Integer yesterday = Integer.valueOf(DateUtils.dateToString(yesterdayDate, "yyyyMMdd"));
            yesterdayDayMap.putAll(getAllUserRanking(indicatorType, yesterday));
        }
        lastDayMap.forEach((key, rankingVOList) -> {
            List<AgentPerformanceRankingVO> yesterdayRankingVOList = yesterdayDayMap.get(key);
            Map<Long, AgentPerformanceRankingVO> voMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(yesterdayRankingVOList)) {
                Map<Long, AgentPerformanceRankingVO> tempMap = yesterdayRankingVOList.stream().collect(Collectors.toMap(AgentPerformanceRankingVO::getUserId, Function.identity(), (o1, o2) -> o1));
                voMap.putAll(tempMap);
            }
            rankingVOList.forEach(item -> {
                Long itemUserId = item.getUserId();
                AgentGroupUser agentGroupUser = agentGroupUserMap.get(itemUserId);
                if (null != agentGroupUser) {
                    AgentGroup agentGroup = agentGroupMap.get(agentGroupUser.getGroupId());
                    if (null != agentGroup) {
                        item.setGroupName(agentGroup.getGroupName());
                        item.setGroupId(agentGroup.getId());
                    }
                }
                if (voMap.containsKey(item.getUserId())) {
                    AgentPerformanceRankingVO agentPerformanceRankingVO = voMap.get(item.getUserId());
                    if (item.getRanking() < agentPerformanceRankingVO.getRanking()) {
                        item.setGrowthSituation(1);
                    } else if (item.getRanking() == agentPerformanceRankingVO.getRanking()) {
                        item.setGrowthSituation(0);
                    } else {
                        item.setGrowthSituation(-1);
                    }
                    item.setRankingFloat(agentPerformanceRankingVO.getRanking() - item.getRanking());
                }
            });
        });
        return lastDayMap;
    }


    private Map<String, List<AgentPerformanceRankingVO>> dealUserRankingWithPreviousDaySummary(Map<String, List<AgentPerformanceRankingVO>> lastDayMap,
                                                                                               Map<String, List<AgentPerformanceRankingVO>> yesterdayDayMap,
                                                                                               Map<Long, AgentGroupUser> agentGroupUserMap,
                                                                                               Map<Long, AgentGroup> agentGroupMap) {
        Map<String, List<AgentPerformanceRankingVO>> lastDayMapResult = orderPerformanceRanking(lastDayMap);
        Map<String, List<AgentPerformanceRankingVO>> yesterdayDayMapResult = orderPerformanceRanking(yesterdayDayMap);

        lastDayMapResult.forEach((key, rankingVOList) -> {
            List<AgentPerformanceRankingVO> yesterdayRankingVOList = yesterdayDayMapResult.get(key);
            Map<Long, AgentPerformanceRankingVO> voMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(yesterdayRankingVOList)) {
                Map<Long, AgentPerformanceRankingVO> tempMap = yesterdayRankingVOList.stream().collect(Collectors.toMap(AgentPerformanceRankingVO::getUserId, Function.identity(), (o1, o2) -> o1));
                voMap.putAll(tempMap);
            }
            rankingVOList.forEach(item -> {
                Long itemUserId = item.getUserId();
                AgentGroupUser agentGroupUser = agentGroupUserMap.get(itemUserId);
                if (null != agentGroupUser) {
                    AgentGroup agentGroup = agentGroupMap.get(agentGroupUser.getGroupId());
                    if (null != agentGroup) {
                        item.setGroupName(agentGroup.getGroupName());
                        item.setGroupId(agentGroup.getId());
                    }
                }
                if (voMap.containsKey(item.getUserId())) {
                    AgentPerformanceRankingVO agentPerformanceRankingVO = voMap.get(item.getUserId());
                    if (item.getRanking() < agentPerformanceRankingVO.getRanking()) {
                        item.setGrowthSituation(1);
                    } else if (item.getRanking() == agentPerformanceRankingVO.getRanking()) {
                        item.setGrowthSituation(0);
                    } else {
                        item.setGrowthSituation(-1);
                    }
                    item.setRankingFloat(agentPerformanceRankingVO.getRanking() - item.getRanking());
                }
            });
        });
        return lastDayMapResult;
    }

   public  Map<String, List<AgentPerformanceRankingVO>> orderPerformanceRanking(Map<String, List<AgentPerformanceRankingVO>> originMap){
        Map<String,List<AgentPerformanceRankingVO>> resultMap = new HashMap<>();
       if (MapUtils.isNotEmpty(originMap)){
           List<AgentPerformanceRankingVO> dayList = originMap.get("dayList");
           List<AgentPerformanceRankingVO> monthList = originMap.get("monthList");
           dayList.sort((o1, o2) -> {
               return -Double.compare(o1.getIndicatorValue(), o2.getIndicatorValue());
           });
           monthList.sort((o1, o2) -> {
               return -Double.compare(o1.getIndicatorValue(), o2.getIndicatorValue());
           });
           for (int i = 0; i < dayList.size(); i++) {
               if (i > 0 && dayList.get(i).getIndicatorValue() == dayList.get(i - 1).getIndicatorValue()) {
                   dayList.get(i).setRanking(dayList.get(i - 1).getRanking());
               } else {
                   dayList.get(i).setRanking(i + 1);
               }
           }
           for (int i = 0; i < monthList.size(); i++) {
               if (i > 0 && monthList.get(i).getIndicatorValue() == monthList.get(i - 1).getIndicatorValue()) {
                   monthList.get(i).setRanking(monthList.get(i - 1).getRanking());
               } else {
                   monthList.get(i).setRanking(i + 1);
               }
           }
           resultMap.put("dayList",dayList);
           resultMap.put("monthList",monthList);
       }
       return resultMap;
   }

    /**
     * 获取所有分区排行榜，打入了属于个人部门的标志
     *
     * @param indicatorType
     * @param authCurrentUser
     * @return
     */
    public Map<String, List<AgentPerformanceRankingVO>> getAllCityRankingWithOwnGroupSign(IndicatorType indicatorType, AuthCurrentUser authCurrentUser) {
        Integer day = performanceService.lastSuccessDataDay();
        Map<String, List<AgentPerformanceRankingVO>> lastDayMap = dealCityRankingWithPreviousDay(indicatorType, day);
        fillBelongToOwnGroup(lastDayMap, authCurrentUser);
        return lastDayMap;
    }

    /**
     * 填充数据所属性
     *
     * @param lastDayMap
     * @param authCurrentUser
     */
    private void fillBelongToOwnGroup(Map<String, List<AgentPerformanceRankingVO>> lastDayMap, AuthCurrentUser authCurrentUser) {
        Set<Long> ownGroupIdSet = new HashSet<>();
        if (authCurrentUser.isRegionManager() || authCurrentUser.isAreaManager() || authCurrentUser.isCityManager()) {
            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(authCurrentUser.getUserId());
            if (CollectionUtils.isNotEmpty(groupUserList)) {
                Long parentGroupId = groupUserList.get(0).getGroupId();
                ownGroupIdSet.add(parentGroupId);
                ownGroupIdSet.addAll(baseOrgService.getSubGroupList(parentGroupId).stream().map(AgentGroup::getId).collect(Collectors.toSet()));
            }
        }
        lastDayMap.forEach((key, rankingVOList) -> {
            rankingVOList.forEach(item -> {
                if (authCurrentUser.isBusinessDeveloper() && item.getUserId() != null) {
                    item.setBelongToOwnGroup(item.getUserId().equals(authCurrentUser.getUserId()));
                } else {
                    item.setBelongToOwnGroup(ownGroupIdSet.contains(item.getGroupId()));
                }
                item.setInSameRegionGroup(item.isBelongToOwnGroup());
            });
        });
    }

    /**
     * 获取部门排行榜，打入了与前一天的对比数据
     *
     * @param indicatorType
     * @param day
     * @return
     */
    private Map<String, List<AgentPerformanceRankingVO>> dealCityRankingWithPreviousDay(IndicatorType indicatorType, Integer day) {
        Map<String, List<AgentPerformanceRankingVO>> lastDayMap = getAllCityRanking(indicatorType, day);
        Integer firstDayOfMonth = Integer.valueOf(DateUtils.dateToString(MonthRange.current().getStartDate(), "yyyyMMdd"));
        Map<String, List<AgentPerformanceRankingVO>> yesterdayDayMap = new HashMap<>();
        if (!firstDayOfMonth.equals(day)) {
            Date dayDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
            Date yesterdayDate = DateUtils.addDays(dayDate, -1);
            Integer yesterday = Integer.valueOf(DateUtils.dateToString(yesterdayDate, "yyyyMMdd"));
            yesterdayDayMap.putAll(getAllCityRanking(indicatorType, yesterday));
        }
        lastDayMap.forEach((key, rankingVOList) -> {
            List<AgentPerformanceRankingVO> yesterdayRankingVOList = yesterdayDayMap.get(key);
            Map<Long, AgentPerformanceRankingVO> voMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(yesterdayRankingVOList)) {
                Map<Long, AgentPerformanceRankingVO> tempMap = yesterdayRankingVOList.stream().collect(Collectors.toMap(AgentPerformanceRankingVO::getGroupId, Function.identity(), (o1, o2) -> o1));
                voMap.putAll(tempMap);
            }
            rankingVOList.forEach(item -> {
                if (voMap.containsKey(item.getGroupId())) {
                    AgentPerformanceRankingVO agentPerformanceRankingVO = voMap.get(item.getGroupId());
                    if (item.getRanking() < agentPerformanceRankingVO.getRanking()) {
                        item.setGrowthSituation(1);
                    } else if (item.getRanking() == agentPerformanceRankingVO.getRanking()) {
                        item.setGrowthSituation(0);
                    } else {
                        item.setGrowthSituation(-1);
                    }
                    item.setRankingFloat(agentPerformanceRankingVO.getRanking() - item.getRanking());
                }
            });
        });
        return lastDayMap;
    }

    /**
     * 获取所有分区的排行
     *
     * @param indicatorType
     * @param day
     * @return
     */
    private Map<String, List<AgentPerformanceRankingVO>> getAllCityRanking(IndicatorType indicatorType, Integer day) {
        if (null == day) {
            day = performanceService.lastSuccessDataDay();
        }

        Boolean isJunior = true;
        int serviceType = JUNIOR_SCHOOL_TYPE;
        if (indicatorType == IndicatorType.JUNIOR_REG || indicatorType == IndicatorType.JUNIOR_SglSubj_INC_1 || indicatorType == IndicatorType.JUNIOR_SglSubj_TEACHER_REG) {
            isJunior = true;
            serviceType = JUNIOR_SCHOOL_TYPE;
        } else if (indicatorType == IndicatorType.MIDDLE_REG || indicatorType == IndicatorType.MIDDLE_ENGLISH_INC_1 || indicatorType == IndicatorType.MIDDLE_MATH_INC_1 || indicatorType == IndicatorType.MIDDLE_ENGLISH_TEACHER_REG || indicatorType == IndicatorType.MIDDLE_MATH_TEACHER_REG) {
            isJunior = false;
            serviceType = MIDDLE_SCHOOL_TYPE;
        } else if (indicatorType == IndicatorType.BIND_STU_PARENT || indicatorType == IndicatorType.LOGIN_GTE1_BIND_STU_PARENT || indicatorType == IndicatorType.PARENT_STU_ACTIVE_SETTLEMENT){
            isJunior = true;
            serviceType = PARENT_TYPE;
        }else {
            return new HashMap<>();
        }

        //获取所有部门
        List<AgentGroup> allGroups = baseOrgService.findAllGroups();
        List<AgentGroup> cityGroupList = new ArrayList<>();
        List<Integer> schoolLevels = new ArrayList<>();
        if (isJunior){
            //级别为分区、业务类型为小学的部门
            cityGroupList.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).collect(Collectors.toList()));
            schoolLevels.add(1);
        }else {
            //级别为分区、业务类型为初中和高中的部门
            cityGroupList.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).collect(Collectors.toList()));
            schoolLevels.add(2);
            schoolLevels.add(4);
        }

        if (CollectionUtils.isEmpty(cityGroupList)) {
            return new HashMap<>();
        }

        Set<Long> cityGroupIds = cityGroupList.stream().map(AgentGroup::getId).collect(Collectors.toSet());

        Map<Long, PerformanceRankingSumDataItem> performanceRankingSumDataItemMap = new HashMap<>();

        if (serviceType == JUNIOR_SCHOOL_TYPE || serviceType == MIDDLE_SCHOOL_TYPE){
            performanceRankingSumDataItemMap.putAll(loadGroupPerformanceRankingSumDataMap(cityGroupIds, day, schoolLevels));
        }else {
            performanceRankingSumDataItemMap.putAll(loadParentGroupPerformanceRankingSumDataMap(cityGroupIds,day,schoolLevels));
        }
        return getGroupRankingVoListByIndicatorType(cityGroupList,performanceRankingSumDataItemMap,indicatorType);
    }

    private Map<String,List<AgentPerformanceRankingVO>> getGroupRankingVoListByIndicatorType(List<AgentGroup> cityGroupList,Map<Long, PerformanceRankingSumDataItem> performanceRankingSumDataItemMap,IndicatorType indicatorType){
        List<AgentPerformanceRankingVO> dayList = new ArrayList<>();
        List<AgentPerformanceRankingVO> monthList = new ArrayList<>();
        cityGroupList.forEach(item -> {
            PerformanceRankingSumDataItem performanceRankingSumDataItem = performanceRankingSumDataItemMap.get(item.getId());
            if (performanceRankingSumDataItem != null) {
                AgentPerformanceRankingVO dayVo = new AgentPerformanceRankingVO();
                dayVo.setGroupId(item.getId());
                dayVo.setGroupName(item.getGroupName());
                AgentPerformanceRankingVO monthVo = new AgentPerformanceRankingVO();
                monthVo.setGroupId(item.getId());
                monthVo.setGroupName(item.getGroupName());
                //拼装指标数据
                bindPerformanceRankingSumData(indicatorType,performanceRankingSumDataItem,dayList,monthList,dayVo,monthVo);
//                Integer actuallyCount = baseOrgService.getAllSubGroupUsersByGroupIdAndRole(item.getId(),AgentRoleType.BusinessDeveloper.getId()).size();
                int actuallyCount = performanceRankingSumDataItem.getHeadCount();
                if (actuallyCount > 0){
                    dayVo.setIndicatorValue(MathUtils.doubleDivide(dayVo.getIndicatorValue(), actuallyCount, 0));
                    monthVo.setIndicatorValue(MathUtils.doubleDivide(monthVo.getIndicatorValue(), actuallyCount, 0));
                }else {
                    dayVo.setIndicatorValue(0);
                    monthVo.setIndicatorValue(0);
                }
            }
        });
        dayList.sort((o1, o2) -> {
            return -Double.compare(o1.getIndicatorValue(), o2.getIndicatorValue());
        });
        monthList.sort((o1, o2) -> {
            return -Double.compare(o1.getIndicatorValue(), o2.getIndicatorValue());
        });
        for (int i = 0; i < dayList.size(); i++) {
            if (i > 0 && dayList.get(i).getIndicatorValue() == dayList.get(i - 1).getIndicatorValue()) {
                dayList.get(i).setRanking(dayList.get(i - 1).getRanking());
            } else {
                dayList.get(i).setRanking(i + 1);
            }
        }
        for (int i = 0; i < monthList.size(); i++) {
            if (i > 0 && monthList.get(i).getIndicatorValue() == monthList.get(i - 1).getIndicatorValue()) {
                monthList.get(i).setRanking(monthList.get(i - 1).getRanking());
            } else {
                monthList.get(i).setRanking(i + 1);
            }
        }
        Map<String, List<AgentPerformanceRankingVO>> result = new HashMap<>();
        result.put(DAY_DATA_TYPE, dayList);
        result.put(MONTH_DATA_TYPE, monthList);
        return result;
    }

    /**
     * 获取所有专员排行
     *
     * @param indicatorType
     * @param day
     * @return
     */
    private Map<String, List<AgentPerformanceRankingVO>> getAllUserRanking(IndicatorType indicatorType, Integer day) {
        if (null == day) {
            day = performanceService.lastSuccessDataDay();
        }
        int serviceType;
        if (indicatorType == IndicatorType.JUNIOR_REG || indicatorType == IndicatorType.JUNIOR_SglSubj_INC_1 || indicatorType == IndicatorType.JUNIOR_SglSubj_TEACHER_REG) {
            serviceType = JUNIOR_SCHOOL_TYPE;
        } else if (indicatorType == IndicatorType.MIDDLE_REG || indicatorType == IndicatorType.MIDDLE_ENGLISH_INC_1 || indicatorType == IndicatorType.MIDDLE_MATH_INC_1 || indicatorType == IndicatorType.MIDDLE_ENGLISH_TEACHER_REG || indicatorType == IndicatorType.MIDDLE_MATH_TEACHER_REG) {
            serviceType = MIDDLE_SCHOOL_TYPE;
        } else if (indicatorType == IndicatorType.BIND_STU_PARENT || indicatorType == IndicatorType.LOGIN_GTE1_BIND_STU_PARENT || indicatorType == IndicatorType.PARENT_STU_ACTIVE_SETTLEMENT){
            serviceType = PARENT_TYPE;
        }else {
            return new HashMap<>();
        }

        //获取所有部门
        List<AgentGroup> allGroups = baseOrgService.findAllGroups();
        List<Long> cityGroupIds = new ArrayList<>();
        if (serviceType == JUNIOR_SCHOOL_TYPE || serviceType == PARENT_TYPE){
            //级别为分区、业务类型为小学的部门
            cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).map(AgentGroup::getId).collect(Collectors.toList()));
        }else {
            //级别为分区、业务类型为初中和高中的部门
            cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).map(AgentGroup::getId).collect(Collectors.toList()));
        }

        //获取对应部门下的专员
        List<Long> businessDeveloperIds = baseOrgService.getUserByGroupIdsAndRole(cityGroupIds, AgentRoleType.BusinessDeveloper);
        List<AgentUser> businessDeveloperList = baseOrgService.getUsers(businessDeveloperIds);

        Map<Long, PerformanceRankingSumDataItem> performanceRankingSumDataItemMap = new HashMap<>();
        List<Integer> schoolLevels = new ArrayList<>();
        //小学
        if (serviceType == JUNIOR_SCHOOL_TYPE) {
            schoolLevels.add(1);
            performanceRankingSumDataItemMap.putAll(loadUserPerformanceRankingSumDataMap(businessDeveloperIds, day, schoolLevels));
            //中学
        } else if (serviceType == MIDDLE_SCHOOL_TYPE){
            schoolLevels.add(2);
            schoolLevels.add(4);
            performanceRankingSumDataItemMap.putAll(loadUserPerformanceRankingSumDataMap(businessDeveloperIds, day, schoolLevels));
        }else {
            schoolLevels.add(1);
            performanceRankingSumDataItemMap.putAll(loadParentUserPerformanceRankingSumDataMap(businessDeveloperIds, day, schoolLevels));
        }
        return getUserRankingVoListByIndicatorType(businessDeveloperList,performanceRankingSumDataItemMap,indicatorType);
    }

    private Map<String, List<AgentPerformanceRankingVO>> getUserRankingVoListByIndicatorType(List<AgentUser> businessDeveloperList, Map<Long, PerformanceRankingSumDataItem> performanceRankingSumDataItemMap, IndicatorType indicatorType){
        //昨日指标数据
        List<AgentPerformanceRankingVO> dayList = new ArrayList<>();
        //本月指标数据
        List<AgentPerformanceRankingVO> monthList = new ArrayList<>();
        businessDeveloperList.forEach(item -> {
            PerformanceRankingSumDataItem performanceRankingSumDataItem = performanceRankingSumDataItemMap.get(item.getId());
            if (performanceRankingSumDataItem != null) {
                AgentPerformanceRankingVO dayVo = new AgentPerformanceRankingVO();
                dayVo.setUserName(item.getRealName());
                dayVo.setUserAvatar(item.getAvatar());
                dayVo.setUserId(item.getId());
                AgentPerformanceRankingVO monthVo = new AgentPerformanceRankingVO();
                monthVo.setUserName(item.getRealName());
                monthVo.setUserAvatar(item.getAvatar());
                monthVo.setUserId(item.getId());
                //拼装指标数据
                bindPerformanceRankingSumData(indicatorType,performanceRankingSumDataItem,dayList,monthList,dayVo,monthVo);
            }
        });
        dayList.sort((o1, o2) -> {
            return -Double.compare(o1.getIndicatorValue(), o2.getIndicatorValue());
        });
        monthList.sort((o1, o2) -> {
            return -Double.compare(o1.getIndicatorValue(), o2.getIndicatorValue());
        });
        for (int i = 0; i < dayList.size(); i++) {
            if (i > 0 && dayList.get(i).getIndicatorValue() == dayList.get(i - 1).getIndicatorValue()) {
                dayList.get(i).setRanking(dayList.get(i - 1).getRanking());
            } else {
                dayList.get(i).setRanking(i + 1);
            }
        }
        for (int i = 0; i < monthList.size(); i++) {
            if (i > 0 && monthList.get(i).getIndicatorValue() == monthList.get(i - 1).getIndicatorValue()) {
                monthList.get(i).setRanking(monthList.get(i - 1).getRanking());
            } else {
                monthList.get(i).setRanking(i + 1);
            }
        }
        Map<String, List<AgentPerformanceRankingVO>> result = new HashMap<>();
        result.put(DAY_DATA_TYPE, dayList);
        result.put(MONTH_DATA_TYPE, monthList);
        return result;
    }


    private Map<String,Map<String,List<AgentPerformanceRankingVO>>> getAllUserRankingSummary(Integer day,Integer serviceType,List<AgentGroup> allGroups,Long id,Integer idType,Integer schoolLevelFlag) {
        if (null == day) {
            day = performanceService.lastSuccessDataDay();
        }

        List<Long> cityGroupIds = new ArrayList<>();
        if (serviceType == 1 || serviceType == 3){
            //级别为分区、业务类型为小学的部门
            cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).map(AgentGroup::getId).collect(Collectors.toList()));
        }else {
            //级别为分区、业务类型为初中和高中的部门
            cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).map(AgentGroup::getId).collect(Collectors.toList()));
        }

        //获取对应部门下的专员
        List<Long> businessDeveloperIds = baseOrgService.getUserByGroupIdsAndRole(cityGroupIds, AgentRoleType.BusinessDeveloper);
        List<AgentUser> businessDeveloperList = baseOrgService.getUsers(businessDeveloperIds);

        Map<Long, PerformanceRankingSumDataItem> performanceRankingSumDataItemMap = new HashMap<>();
        List<Integer> schoolLevels = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, idType, schoolLevelFlag);
        //小学
        if (serviceType.equals(JUNIOR_SCHOOL_TYPE)) {
            performanceRankingSumDataItemMap.putAll(loadUserPerformanceRankingSumDataMap(businessDeveloperIds, day, schoolLevels));
            //中学
        } else if (serviceType.equals(MIDDLE_SCHOOL_TYPE)){
            performanceRankingSumDataItemMap.putAll(loadUserPerformanceRankingSumDataMap(businessDeveloperIds, day, schoolLevels));
            //家长
        }else {
            performanceRankingSumDataItemMap.putAll(loadParentUserPerformanceRankingSumDataMap(businessDeveloperIds,day,schoolLevels));
        }
        //小学
        List<AgentPerformanceRankingVO> juniorRegDayList = new ArrayList<>();
        List<AgentPerformanceRankingVO> juniorRegMonthList = new ArrayList<>();
        List<AgentPerformanceRankingVO> juniorSglSubjInc1DayList = new ArrayList<>();
        List<AgentPerformanceRankingVO> juniorSglSubjInc1MonthList = new ArrayList<>();

        List<AgentPerformanceRankingVO> juniorSglSubjTeacherRegDayList = new ArrayList<>();
        List<AgentPerformanceRankingVO> juniorSglSubjTeacherRegMonthList = new ArrayList<>();
        //中学
        List<AgentPerformanceRankingVO> middleRegDayList = new ArrayList<>();
        List<AgentPerformanceRankingVO> middleRegMonthList = new ArrayList<>();
        List<AgentPerformanceRankingVO> middleEnglishInc1DayList = new ArrayList<>();
        List<AgentPerformanceRankingVO> middleEnglishInc1MonthList = new ArrayList<>();
        List<AgentPerformanceRankingVO> middleMathInc1DayList = new ArrayList<>();
        List<AgentPerformanceRankingVO> middleMathInc1MonthList = new ArrayList<>();

        List<AgentPerformanceRankingVO> middleEnglishTeacherRegDayList = new ArrayList<>();
        List<AgentPerformanceRankingVO> middleEnglishTeacherRegMonthList = new ArrayList<>();
        List<AgentPerformanceRankingVO> middleMathTeacherRegDayList = new ArrayList<>();
        List<AgentPerformanceRankingVO> middleMathTeacherRegMonthList = new ArrayList<>();

        //家长
        List<AgentPerformanceRankingVO> bindStuParentDayList = new ArrayList<>();
        List<AgentPerformanceRankingVO> bindStuParentMonthList = new ArrayList<>();

        List<AgentPerformanceRankingVO> loginGte1BindStuParentDayList = new ArrayList<>();
        List<AgentPerformanceRankingVO> loginGte1BindStuParentMonthList = new ArrayList<>();

        List<AgentPerformanceRankingVO> parentStuActiveSettlementDayList = new ArrayList<>();
        List<AgentPerformanceRankingVO> parentStuActiveSettlementMonthList = new ArrayList<>();

        businessDeveloperList.forEach(item -> {
            PerformanceRankingSumDataItem dataItem = performanceRankingSumDataItemMap.get(item.getId());
            if (dataItem != null) {
                AgentPerformanceRankingVO origRankingVo = new AgentPerformanceRankingVO();
                origRankingVo.setUserName(item.getRealName());
                origRankingVo.setUserAvatar(item.getAvatar());
                origRankingVo.setUserId(item.getId());
                if (serviceType == 1) {
                    //昨日小学学生注册
                    juniorRegDayList.add(generateRankingVo(origRankingVo,dataItem.getRegStuCountDf()));
                    //本月小学学生注册
                    juniorRegMonthList.add(generateRankingVo(origRankingVo,dataItem.getTmRegStuCount()));

                    //昨日单科学生新增
                    juniorSglSubjInc1DayList.add(generateRankingVo(origRankingVo,dataItem.getPdFinSglSubjHwGte1IncStuCount()));
                    //本月小数学生新增
                    juniorSglSubjInc1MonthList.add(generateRankingVo(origRankingVo,dataItem.getTmFinSglSubjHwGte1IncStuCount()));

                    //昨日老师注册榜
                    juniorSglSubjTeacherRegDayList.add(generateRankingVo(origRankingVo,dataItem.getPdRegSglSubjTeaCount()));
                    //本月老师注册榜
                    juniorSglSubjTeacherRegMonthList.add(generateRankingVo(origRankingVo,dataItem.getTmRegSglSubjTeaCount()));
                }else if (serviceType == 2){
                    //昨日中学学生注册
                    middleRegDayList.add(generateRankingVo(origRankingVo,dataItem.getRegStuCountDf() + dataItem.getPdPromoteRegStuCount()));
                    //本月中学学生注册
                    middleRegMonthList.add(generateRankingVo(origRankingVo,dataItem.getTmRegStuCount() + dataItem.getTmPromoteRegStuCount()));

                    //昨日中英学生新增
                    middleEnglishInc1DayList.add(generateRankingVo(origRankingVo,dataItem.getPdFinEngHwGte1IncStuCount()));
                    //本月中英学生新增
                    middleEnglishInc1MonthList.add(generateRankingVo(origRankingVo,dataItem.getTmFinEngHwGte1IncStuCount()));

                    //昨日中数学生新增
                    middleMathInc1DayList.add(generateRankingVo(origRankingVo,dataItem.getPdFinMathHwGte1IncStuCount()));
                    //本月中数学生新增
                    middleMathInc1MonthList.add(generateRankingVo(origRankingVo,dataItem.getTmFinMathHwGte1IncStuCount()));

                    //昨日中英老师注册榜
                    middleEnglishTeacherRegDayList.add(generateRankingVo(origRankingVo,dataItem.getPdRegEngTeaCount()));
                    //本月中英老师注册榜
                    middleEnglishTeacherRegMonthList.add(generateRankingVo(origRankingVo,dataItem.getPdRegEngTeaCount()));

                    //昨日中数老师注册榜
                    middleMathTeacherRegDayList.add(generateRankingVo(origRankingVo,dataItem.getPdRegMathTeaCount()));
                    //本月中数老师注册榜
                    middleMathTeacherRegMonthList.add(generateRankingVo(origRankingVo,dataItem.getPdRegMathTeaCount()));
                }else {
                    //昨日新增绑定家长榜
                    bindStuParentDayList.add(generateRankingVo(origRankingVo,dataItem.getPdBindStuParentNum()));
                    //本月新增绑定家长榜
                    bindStuParentMonthList.add(generateRankingVo(origRankingVo,dataItem.getPdBindStuParentNum()));

                    //昨日家长活跃1次榜
                    loginGte1BindStuParentDayList.add(generateRankingVo(origRankingVo,dataItem.getPdLoginGte1BindStuParentNum()));
                    //本月家长活跃1次榜
                    loginGte1BindStuParentMonthList.add(generateRankingVo(origRankingVo,dataItem.getTmLoginGte1BindStuParentNum()));

                    //昨日学生家长双活榜
                    parentStuActiveSettlementDayList.add(generateRankingVo(origRankingVo,dataItem.getPdParentStuActiveSettlementNum()));
                    //本月学生家长双活榜
                    parentStuActiveSettlementMonthList.add(generateRankingVo(origRankingVo,dataItem.getTmParentStuActiveSettlementNum()));
                }

            }
        });

        Map<String,Map<String,List<AgentPerformanceRankingVO>>> dataMap = new HashMap<>();
        if(serviceType == 1){
            Map<String,List<AgentPerformanceRankingVO>> juniorRegItemMap = new HashMap<>();
            juniorRegItemMap.put(DAY_DATA_TYPE, juniorRegDayList);
            juniorRegItemMap.put(MONTH_DATA_TYPE, juniorRegMonthList);
            dataMap.put(IndicatorType.JUNIOR_REG.name(),juniorRegItemMap);

            Map<String,List<AgentPerformanceRankingVO>> juniorSglSubjInc1ItemMap = new HashMap<>();
            juniorSglSubjInc1ItemMap.put(DAY_DATA_TYPE, juniorSglSubjInc1DayList);
            juniorSglSubjInc1ItemMap.put(MONTH_DATA_TYPE, juniorSglSubjInc1MonthList);
            dataMap.put(IndicatorType.JUNIOR_SglSubj_INC_1.name(),juniorSglSubjInc1ItemMap);


            Map<String,List<AgentPerformanceRankingVO>> juniorSglSubjTeacherRegItemMap = new HashMap<>();
            juniorSglSubjTeacherRegItemMap.put(DAY_DATA_TYPE, juniorSglSubjTeacherRegDayList);
            juniorSglSubjTeacherRegItemMap.put(MONTH_DATA_TYPE, juniorSglSubjTeacherRegMonthList);
            dataMap.put(IndicatorType.JUNIOR_SglSubj_TEACHER_REG.name(),juniorSglSubjTeacherRegItemMap);

        }else if (serviceType == 2){
            Map<String,List<AgentPerformanceRankingVO>> middleRegItemMap = new HashMap<>();
            middleRegItemMap.put(DAY_DATA_TYPE, middleRegDayList);
            middleRegItemMap.put(MONTH_DATA_TYPE, middleRegMonthList);
            dataMap.put(IndicatorType.MIDDLE_REG.name(),middleRegItemMap);

            Map<String,List<AgentPerformanceRankingVO>> middleEnglishInc1ItemMap = new HashMap<>();
            middleEnglishInc1ItemMap.put(DAY_DATA_TYPE, middleEnglishInc1DayList);
            middleEnglishInc1ItemMap.put(MONTH_DATA_TYPE, middleEnglishInc1MonthList);
            dataMap.put(IndicatorType.MIDDLE_ENGLISH_INC_1.name(),middleEnglishInc1ItemMap);

            Map<String,List<AgentPerformanceRankingVO>> middleMathInc1ItemMap = new HashMap<>();
            middleMathInc1ItemMap.put(DAY_DATA_TYPE, middleMathInc1DayList);
            middleMathInc1ItemMap.put(MONTH_DATA_TYPE, middleMathInc1MonthList);
            dataMap.put(IndicatorType.MIDDLE_MATH_INC_1.name(),middleMathInc1ItemMap);

            Map<String,List<AgentPerformanceRankingVO>> middleEnglishTeacherRegItemMap = new HashMap<>();
            middleEnglishTeacherRegItemMap.put(DAY_DATA_TYPE, middleEnglishTeacherRegDayList);
            middleEnglishTeacherRegItemMap.put(MONTH_DATA_TYPE, middleEnglishTeacherRegMonthList);
            dataMap.put(IndicatorType.MIDDLE_ENGLISH_TEACHER_REG.name(),middleEnglishTeacherRegItemMap);

            Map<String,List<AgentPerformanceRankingVO>> middleMathTeacherRegItemMap = new HashMap<>();
            middleMathTeacherRegItemMap.put(DAY_DATA_TYPE, middleMathTeacherRegDayList);
            middleMathTeacherRegItemMap.put(MONTH_DATA_TYPE, middleMathTeacherRegMonthList);
            dataMap.put(IndicatorType.MIDDLE_MATH_TEACHER_REG.name(),middleMathTeacherRegItemMap);
        }else {
            Map<String,List<AgentPerformanceRankingVO>> bindStuParentItemMap = new HashMap<>();
            bindStuParentItemMap.put(DAY_DATA_TYPE, bindStuParentDayList);
            bindStuParentItemMap.put(MONTH_DATA_TYPE, bindStuParentMonthList);
            dataMap.put(IndicatorType.BIND_STU_PARENT.name(),bindStuParentItemMap);

            Map<String,List<AgentPerformanceRankingVO>> loginGte1BindStuParentItemMap = new HashMap<>();
            loginGte1BindStuParentItemMap.put(DAY_DATA_TYPE, loginGte1BindStuParentDayList);
            loginGte1BindStuParentItemMap.put(MONTH_DATA_TYPE, loginGte1BindStuParentMonthList);
            dataMap.put(IndicatorType.LOGIN_GTE1_BIND_STU_PARENT.name(),loginGte1BindStuParentItemMap);

            Map<String,List<AgentPerformanceRankingVO>> parentStuActiveSettlementItemMap = new HashMap<>();
            parentStuActiveSettlementItemMap.put(DAY_DATA_TYPE, parentStuActiveSettlementDayList);
            parentStuActiveSettlementItemMap.put(MONTH_DATA_TYPE, parentStuActiveSettlementMonthList);
            dataMap.put(IndicatorType.PARENT_STU_ACTIVE_SETTLEMENT.name(),parentStuActiveSettlementItemMap);
        }
        return dataMap;
    }

    public AgentPerformanceRankingVO generateRankingVo(AgentPerformanceRankingVO origRankingVo,int indicatorValue){
        AgentPerformanceRankingVO rankingVO = new AgentPerformanceRankingVO();
        try {
            BeanUtils.copyProperties(rankingVO,origRankingVo);
        } catch (Exception e) {
            logger.error("bean copy error");
        }
        rankingVO.setIndicatorValue(indicatorValue);
        return rankingVO;
    }

    /**
     * 拼装指标数据
     * @param indicatorType
     * @param performanceRankingSumDataItem
     * @param dayList
     * @param monthList
     * @param dayVo
     * @param monthVo
     */
    private void bindPerformanceRankingSumData(IndicatorType indicatorType,PerformanceRankingSumDataItem performanceRankingSumDataItem,List<AgentPerformanceRankingVO> dayList,List<AgentPerformanceRankingVO> monthList,AgentPerformanceRankingVO dayVo,AgentPerformanceRankingVO monthVo){
        //小学学生注册榜
        if (indicatorType == IndicatorType.JUNIOR_REG) {
            dayVo.setIndicatorValue(performanceRankingSumDataItem.getRegStuCountDf());
            dayList.add(dayVo);
            monthVo.setIndicatorValue(performanceRankingSumDataItem.getTmRegStuCount());
            monthList.add(monthVo);
            //单科学生新增榜
        } else if (indicatorType == IndicatorType.JUNIOR_SglSubj_INC_1) {
            dayVo.setIndicatorValue(performanceRankingSumDataItem.getPdFinSglSubjHwGte1IncStuCount());
            dayList.add(dayVo);
            monthVo.setIndicatorValue(performanceRankingSumDataItem.getTmFinSglSubjHwGte1IncStuCount());
            monthList.add(monthVo);
            //老师注册榜
        } else if (indicatorType == IndicatorType.JUNIOR_SglSubj_TEACHER_REG){
            dayVo.setIndicatorValue(performanceRankingSumDataItem.getPdRegSglSubjTeaCount());
            dayList.add(dayVo);
            monthVo.setIndicatorValue(performanceRankingSumDataItem.getTmRegSglSubjTeaCount());
            monthList.add(monthVo);
            //中学学生注册榜
        } else if (indicatorType == IndicatorType.MIDDLE_REG) {
            dayVo.setIndicatorValue(performanceRankingSumDataItem.getRegStuCountDf() + performanceRankingSumDataItem.getPdPromoteRegStuCount());
            dayList.add(dayVo);
            monthVo.setIndicatorValue(performanceRankingSumDataItem.getTmRegStuCount() + performanceRankingSumDataItem.getTmPromoteRegStuCount());
            monthList.add(monthVo);
            //中英学生新增榜
        } else if (indicatorType == IndicatorType.MIDDLE_ENGLISH_INC_1) {
            dayVo.setIndicatorValue(performanceRankingSumDataItem.getPdFinEngHwGte1IncStuCount());
            dayList.add(dayVo);
            monthVo.setIndicatorValue(performanceRankingSumDataItem.getTmFinEngHwGte1IncStuCount());
            monthList.add(monthVo);
            //中数学生新增榜
        } else if (indicatorType == IndicatorType.MIDDLE_MATH_INC_1) {
            dayVo.setIndicatorValue(performanceRankingSumDataItem.getPdFinMathHwGte1IncStuCount());
            dayList.add(dayVo);
            monthVo.setIndicatorValue(performanceRankingSumDataItem.getTmFinMathHwGte1IncStuCount());
            monthList.add(monthVo);
            //中英老师注册榜
        } else if (indicatorType == IndicatorType.MIDDLE_ENGLISH_TEACHER_REG) {
            dayVo.setIndicatorValue(performanceRankingSumDataItem.getPdRegEngTeaCount());
            dayList.add(dayVo);
            monthVo.setIndicatorValue(performanceRankingSumDataItem.getTmRegEngTeaCount());
            monthList.add(monthVo);
            //中数老师注册榜
        } else if (indicatorType == IndicatorType.MIDDLE_MATH_TEACHER_REG) {
            dayVo.setIndicatorValue(performanceRankingSumDataItem.getPdRegMathTeaCount());
            dayList.add(dayVo);
            monthVo.setIndicatorValue(performanceRankingSumDataItem.getTmRegMathTeaCount());
            monthList.add(monthVo);
            //新增绑定家长榜
        }else if (indicatorType == IndicatorType.BIND_STU_PARENT){
            dayVo.setIndicatorValue(performanceRankingSumDataItem.getPdBindStuParentNum());
            dayList.add(dayVo);
            monthVo.setIndicatorValue(performanceRankingSumDataItem.getTmBindStuParentNum());
            monthList.add(monthVo);
            //家长活跃1次榜
        }else if (indicatorType == IndicatorType.LOGIN_GTE1_BIND_STU_PARENT){
            dayVo.setIndicatorValue(performanceRankingSumDataItem.getPdLoginGte1BindStuParentNum());
            dayList.add(dayVo);
            monthVo.setIndicatorValue(performanceRankingSumDataItem.getTmLoginGte1BindStuParentNum());
            monthList.add(monthVo);
            //学生家长双活榜
        }else if (indicatorType == IndicatorType.PARENT_STU_ACTIVE_SETTLEMENT){
            dayVo.setIndicatorValue(performanceRankingSumDataItem.getPdParentStuActiveSettlementNum());
            dayList.add(dayVo);
            monthVo.setIndicatorValue(performanceRankingSumDataItem.getTmParentStuActiveSettlementNum());
            monthList.add(monthVo);
        }
    }

    /**
     * @param authCurrentUser
     * @param rankingType     1：部门排行榜，3：专员排行榜
     * @return
     */
    public boolean showBelongToOwnGroup(AuthCurrentUser authCurrentUser, int rankingType) {
        if (null != authCurrentUser) {
            return authCurrentUser.isRegionManager() || authCurrentUser.isAreaManager() || (authCurrentUser.isCityManager() && rankingType != 1);
        }
        return false;
    }

    private Map<Long, PerformanceRankingSumDataItem> loadUserPerformanceRankingSumDataMap(Collection<Long> userIds, Integer day, Collection<Integer> schoolLevels) {
        //获取人员业绩数据
        Map<Long, SumOnlineIndicator> sumOnlineIndicatorMap = onlineIndicatorService.loadUserSumData(userIds, day, schoolLevels);
        return loadPerformanceRankingSumDataMap(userIds,sumOnlineIndicatorMap);
    }


    private Map<Long, PerformanceRankingSumDataItem> loadGroupPerformanceRankingSumDataMap(Collection<Long> groupIds, Integer day, Collection<Integer> schoolLevels) {
        Map<Long, SumOnlineIndicator> sumOnlineIndicatorMap = onlineIndicatorService.loadGroupSumData(groupIds, day, schoolLevels);
        return loadPerformanceRankingSumDataMap(groupIds,sumOnlineIndicatorMap);
    }

    private Map<Long, PerformanceRankingSumDataItem> loadParentUserPerformanceRankingSumDataMap(Collection<Long> userIds, Integer day, Collection<Integer> schoolLevels) {
        //获取人员业绩数据
        Map<Long, SumParentIndicator> sumParentIndicatorMap = parentIndicatorService.loadUserSumData(userIds, day, schoolLevels);
        return loadParentPerformanceRankingSumDataMap(userIds,sumParentIndicatorMap);
    }

    private Map<Long, PerformanceRankingSumDataItem> loadParentGroupPerformanceRankingSumDataMap(Collection<Long> groupIds, Integer day, Collection<Integer> schoolLevels) {
        Map<Long, SumParentIndicator> sumParentIndicatorMap = parentIndicatorService.loadGroupSumData(groupIds, day, schoolLevels);
        return loadParentPerformanceRankingSumDataMap(groupIds,sumParentIndicatorMap);
    }

    private Map<Long, PerformanceRankingSumDataItem> loadPerformanceRankingSumDataMap(Collection<Long> ids,Map<Long, SumOnlineIndicator> sumOnlineIndicatorMap) {
        Map<Long, PerformanceRankingSumDataItem> result = new HashMap<>();
        if (MapUtils.isNotEmpty(sumOnlineIndicatorMap)){
            ids.forEach(item -> {
                SumOnlineIndicator sumOnlineIndicator = sumOnlineIndicatorMap.get(item);
                if (sumOnlineIndicator != null){
                    //昨日数据
                    PerformanceRankingSumDataItem dataItem = new PerformanceRankingSumDataItem();
                    dataItem.setRegStuCountDf(SafeConverter.toInt(sumOnlineIndicator.fetchDayData().getRegStuCount()));
                    dataItem.setPdFinChnHwGte1IncStuCount(SafeConverter.toInt(sumOnlineIndicator.fetchDayData().getFinChnHwGte1UnSettleStuCount()));
                    dataItem.setPdFinMathHwGte1IncStuCount(SafeConverter.toInt(sumOnlineIndicator.fetchDayData().getFinMathHwGte1UnSettleStuCount()));
                    dataItem.setPdFinEngHwGte1IncStuCount(SafeConverter.toInt(sumOnlineIndicator.fetchDayData().getFinEngHwGte1UnSettleStuCount()));
                    dataItem.setPdFinSglSubjHwGte1IncStuCount(SafeConverter.toInt(sumOnlineIndicator.fetchDayData().getFinSglSubjHwGte1UnSettleStuCount()));
                    dataItem.setPdRegEngTeaCount(SafeConverter.toInt(sumOnlineIndicator.fetchDayData().getRegEngTeaCount()));
                    dataItem.setPdRegMathTeaCount(SafeConverter.toInt(sumOnlineIndicator.fetchDayData().getRegMathTeaCount()));
                    dataItem.setPdRegSglSubjTeaCount(SafeConverter.toInt(sumOnlineIndicator.fetchDayData().getRegSglSubjTeaCount()));
                    dataItem.setPdPromoteRegStuCount(SafeConverter.toInt(sumOnlineIndicator.fetchDayData().getPromoteRegStuCount()));//学生注册数（升学）

                    //本月数据
                    dataItem.setTmRegStuCount(SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getRegStuCount()));
                    dataItem.setTmFinChnHwGte1IncStuCount(SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getFinChnHwGte1UnSettleStuCount()));
                    dataItem.setTmFinMathHwGte1IncStuCount(SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getFinMathHwGte1UnSettleStuCount()));
                    dataItem.setTmFinEngHwGte1IncStuCount(SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getFinEngHwGte1UnSettleStuCount()));
                    dataItem.setTmFinSglSubjHwGte1IncStuCount(SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getFinSglSubjHwGte1UnSettleStuCount()));
                    dataItem.setTmRegEngTeaCount(SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getRegEngTeaCount()));
                    dataItem.setTmRegMathTeaCount(SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getRegMathTeaCount()));
                    dataItem.setTmRegSglSubjTeaCount(SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getRegSglSubjTeaCount()));
                    dataItem.setTmPromoteRegStuCount(SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getPromoteRegStuCount()));//学生注册数（升学）

                    dataItem.setHeadCount(sumOnlineIndicator.getHeadCount());
                    result.put(item,dataItem);
                }
            });
        }
        return result;
    }

    private Map<Long, PerformanceRankingSumDataItem> loadParentPerformanceRankingSumDataMap(Collection<Long> ids,Map<Long, SumParentIndicator> sumParentIndicatorMap) {
        Map<Long, PerformanceRankingSumDataItem> result = new HashMap<>();
        if (MapUtils.isNotEmpty(sumParentIndicatorMap)){
            ids.forEach(item -> {
                SumParentIndicator sumParentIndicator = sumParentIndicatorMap.get(item);
                if (sumParentIndicator != null){
                    PerformanceRankingSumDataItem dataItem = new PerformanceRankingSumDataItem();
                    dataItem.setPdBindStuParentNum(SafeConverter.toInt(sumParentIndicator.fetchDayData().getBindStuParentNum()));
                    dataItem.setPdLoginGte1BindStuParentNum(SafeConverter.toInt(sumParentIndicator.fetchDayData().getTmLoginGte1BindStuParentNum()));
                    dataItem.setPdParentStuActiveSettlementNum(SafeConverter.toInt(sumParentIndicator.fetchDayData().getParentStuActiveSettlementNum()));

                    dataItem.setTmBindStuParentNum(SafeConverter.toInt(sumParentIndicator.fetchMonthData().getBindStuParentNum()));
                    dataItem.setTmLoginGte1BindStuParentNum(SafeConverter.toInt(sumParentIndicator.fetchMonthData().getTmLoginGte1BindStuParentNum()));
                    dataItem.setTmParentStuActiveSettlementNum(SafeConverter.toInt(sumParentIndicator.fetchMonthData().getParentStuActiveSettlementNum()));

                    dataItem.setHeadCount(SafeConverter.toInt(sumParentIndicator.getHeadCount()));
                    result.put(item,dataItem);
                }
            });
        }
        return result;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum IndicatorType {
        @Deprecated
        JUNIOR_INC(2,"小学新增榜"),//小学新增
        @Deprecated
        MIDDLE_ENGLISH_REG(3,"中英注册榜"),  //中英注册
        @Deprecated
        JUNIOR_CHINESE_INC_1(5,"小语新增1套榜"),//小语新增1套
        @Deprecated
        MIDDLE_SCAN(6,"中学扫描榜"),//中学扫描

        JUNIOR_REG(1,"小学学生注册榜"),            //小学注册榜
        JUNIOR_SglSubj_INC_1(15,"单科学生新增榜"),     //单科新增1套
        JUNIOR_SglSubj_TEACHER_REG(16,"老师注册榜"),      //老师注册榜

        MIDDLE_REG(7,"中学学生注册榜"),            //中学学生注册榜
        MIDDLE_ENGLISH_INC_1(8,"中英学生新增榜"),  //中英学生新增榜
        MIDDLE_MATH_INC_1(9,"中数学生新增榜"),     //中数学生新增榜
        MIDDLE_ENGLISH_TEACHER_REG(10,"中英老师注册榜"),   //中英老师注册榜
        MIDDLE_MATH_TEACHER_REG(11,"中数老师注册榜"),      //中数老师注册榜

        BIND_STU_PARENT(12,"新增绑定家长榜"),
        LOGIN_GTE1_BIND_STU_PARENT(13,"家长活跃1次榜"),
        PARENT_STU_ACTIVE_SETTLEMENT(14,"学生家长双活榜");

        @Getter
        private final int type;
        @Getter
        private final String description;

        private final static Map<Integer, IndicatorType> TEMP_MAP = new LinkedHashMap<>();

        static {
            for (IndicatorType indicatorType : IndicatorType.values()) {
                TEMP_MAP.put(indicatorType.getType(), indicatorType);
            }
        }

        public static IndicatorType valueOf(int value) {
            try {
                return TEMP_MAP.get(value);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public Map<String,Object> ownRanking(Long id,Integer idType,Integer serviceType,Integer schoolLevelFlag){
        Map<String,Object> dataMap = new HashMap<>();
        Integer day = performanceService.lastSuccessDataDay();
        //获取所有部门
        List<AgentGroup> allGroups = baseOrgService.findAllGroups();
        //级别为分区、业务类型为小学的部门
        List<AgentGroup> cityGroupList = allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).collect(Collectors.toList());
        Set<Long> cityGroupIds = cityGroupList.stream().map(AgentGroup::getId).collect(Collectors.toSet());

        //获取对应部门下的专员
        List<Long> businessDeveloperIds = baseOrgService.getUserByGroupIdsAndRole(cityGroupIds, AgentRoleType.BusinessDeveloper);
        List<AgentUser> businessDeveloperList = baseOrgService.getUsers(businessDeveloperIds);

        Map<Long, PerformanceRankingSumDataItem> performanceRankingSumDataItemMap = new HashMap<>();
        List<Integer> schoolLevels = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id,idType,schoolLevelFlag);

        //小学作业
        if (Objects.equals(serviceType, JUNIOR_SCHOOL_TYPE)){
            AgentPerformanceRankingVO dayRankingVo= null;
            if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
                performanceRankingSumDataItemMap.putAll(loadUserPerformanceRankingSumDataMap(businessDeveloperIds, day, schoolLevels));
                Map<String, List<AgentPerformanceRankingVO>> rankingVoMap = getUserRankingVoListByIndicatorType(businessDeveloperList, performanceRankingSumDataItemMap, IndicatorType.JUNIOR_REG);
                List<AgentPerformanceRankingVO> dayRankingVOList = rankingVoMap.get(DAY_DATA_TYPE);
                dayRankingVo = dayRankingVOList.stream().filter(p -> Objects.equals(p.getUserId(), id)).findFirst().orElse(null);
            }else if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
                performanceRankingSumDataItemMap.putAll(loadGroupPerformanceRankingSumDataMap(cityGroupIds,day,schoolLevels));
                Map<String, List<AgentPerformanceRankingVO>> rankingVoMap = getGroupRankingVoListByIndicatorType(cityGroupList, performanceRankingSumDataItemMap, IndicatorType.JUNIOR_REG);
                List<AgentPerformanceRankingVO> dayRankingVOList = rankingVoMap.get(DAY_DATA_TYPE);
                dayRankingVo = dayRankingVOList.stream().filter(p -> Objects.equals(p.getGroupId(), id)).findFirst().orElse(null);
            }
            if (dayRankingVo != null){
                dataMap.put("regStuRanking",dayRankingVo.getRanking());
            }
            //家长
        }else if (Objects.equals(serviceType, PARENT_TYPE)){
            Map<String, List<AgentPerformanceRankingVO>> rankingVoMap1 = new HashMap<>();
            Map<String, List<AgentPerformanceRankingVO>> rankingVoMap2 = new HashMap<>();
            Map<String, List<AgentPerformanceRankingVO>> rankingVoMap3 = new HashMap<>();
            if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
                performanceRankingSumDataItemMap.putAll(loadParentUserPerformanceRankingSumDataMap(businessDeveloperIds, day, schoolLevels));
                rankingVoMap1.putAll(getUserRankingVoListByIndicatorType(businessDeveloperList, performanceRankingSumDataItemMap, IndicatorType.BIND_STU_PARENT));
                rankingVoMap2.putAll(getUserRankingVoListByIndicatorType(businessDeveloperList, performanceRankingSumDataItemMap, IndicatorType.LOGIN_GTE1_BIND_STU_PARENT));
                rankingVoMap3.putAll(getUserRankingVoListByIndicatorType(businessDeveloperList, performanceRankingSumDataItemMap, IndicatorType.PARENT_STU_ACTIVE_SETTLEMENT));
            }else if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
                performanceRankingSumDataItemMap.putAll(loadParentGroupPerformanceRankingSumDataMap(cityGroupIds,day,schoolLevels));
                rankingVoMap1.putAll(getGroupRankingVoListByIndicatorType(cityGroupList, performanceRankingSumDataItemMap, IndicatorType.BIND_STU_PARENT));
                rankingVoMap2.putAll(getGroupRankingVoListByIndicatorType(cityGroupList, performanceRankingSumDataItemMap, IndicatorType.LOGIN_GTE1_BIND_STU_PARENT));
                rankingVoMap3.putAll(getGroupRankingVoListByIndicatorType(cityGroupList, performanceRankingSumDataItemMap, IndicatorType.PARENT_STU_ACTIVE_SETTLEMENT));
            }
            dataMap.putAll(getRanking(id,idType,rankingVoMap1,"bindStuParentRanking"));
            dataMap.putAll(getRanking(id,idType,rankingVoMap2,"loginGte1BindStuParentRanking"));
            dataMap.putAll(getRanking(id,idType,rankingVoMap3,"parentStuActiveSettlementRanking"));
        }

        return dataMap;
    }

    private Map<String,Object> getRanking(Long id,Integer idType,Map<String, List<AgentPerformanceRankingVO>> rankingVoMap,String rankingKey){
        Map<String,Object> dataMap = new HashMap<>();
        List<AgentPerformanceRankingVO> dayRankingVOList = rankingVoMap.get(DAY_DATA_TYPE);
        AgentPerformanceRankingVO dayRankingVo = null;
        if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
            dayRankingVo = dayRankingVOList.stream().filter(p -> Objects.equals(p.getUserId(), id)).findFirst().orElse(null);
        }else if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
            dayRankingVo = dayRankingVOList.stream().filter(p -> Objects.equals(p.getGroupId(), id)).findFirst().orElse(null);
        }
        if (dayRankingVo != null){
            dataMap.put(rankingKey,dayRankingVo.getRanking());
        }
        return dataMap;
    }

}
