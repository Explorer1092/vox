package com.voxlearning.utopia.agent.controller.mobile.ranking;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.ranking.AgentPerformanceRankingVO;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.data.AgentDataParentService;
import com.voxlearning.utopia.agent.service.ranking.AgentRankingService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.agent.service.ranking.AgentRankingService.DAY_OWN_DATA_TYPE;
import static com.voxlearning.utopia.agent.service.ranking.AgentRankingService.MONTH_OWN_DATA_TYPE;

/**
 * @author chunlin.yu
 * @create 2018-03-21 21:20
 **/
@Controller
@RequestMapping("/mobile/ranking")
public class AgentRankingController extends AbstractAgentController {


    @Inject
    private AgentRankingService agentRankingService;
    @Inject
    private AgentDataParentService agentDataParentService;


    /**
     * 获取专员排行汇总
     * @return
     */
    @RequestMapping(value = "/ranking_summary.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rankingSummary() {
        Integer serviceType = getRequestInt("serviceType",1);   //业务类型	1：小学作业  2：中学作业  3：家长
        Integer dateType = getRequestInt("dateType",1);         //日期类型	1：昨日  2：本月

        AuthCurrentUser currentUser = getCurrentUser();
        int schoolLevelFlag = 1;
        if (serviceType == 2){
            schoolLevelFlag = 24;
        }
        Map<String, Object> parameterMap = agentDataParentService.getTargetParameterMap(currentUser.getUserId(),schoolLevelFlag);
        Long id = (Long)parameterMap.getOrDefault("id", 0);
        Integer idType = (Integer)parameterMap.getOrDefault("idType", AgentConstants.INDICATOR_TYPE_USER);
        schoolLevelFlag = (Integer)parameterMap.getOrDefault("schoolLevelFlag", 1); // 学校阶段   1:小学   24：初高中
        if(id <= 0 || (!Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP) && !Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER))){
            return MapMessage.errorMessage("参数错误！");
        }
        Map<String, List<AgentPerformanceRankingVO>> result = agentRankingService.getRankingSummary(currentUser,serviceType,dateType,id,idType,schoolLevelFlag);
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add(AgentRankingService.SUMMARY_DATA_TYPE, result);
        return mapMessage;
    }


    /**
     * 获取专员排行
     * @return
     */
    @RequestMapping(value = "/user_ranking.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUserRanking() {
        String indicatorType = getRequestString("indicatorType");
        if (StringUtils.isBlank(indicatorType)){
            return MapMessage.errorMessage("指标类型不能为空");
        }
        AgentRankingService.IndicatorType indicatorTypeEnum = AgentRankingService.IndicatorType.valueOf(indicatorType);
        AuthCurrentUser currentUser = getCurrentUser();
        Map<String, List<AgentPerformanceRankingVO>> rankingMap = agentRankingService.getAllUserRankingWithOwnGroupSign(indicatorTypeEnum, currentUser);

        List<AgentPerformanceRankingVO> dayRankingVOList = rankingMap.get(AgentRankingService.DAY_DATA_TYPE);
        List<AgentPerformanceRankingVO> monthRankingVOList = rankingMap.get(AgentRankingService.MONTH_DATA_TYPE);
        if (!currentUser.isCountryManager() && !currentUser.isBuManager()) {
            //小学TOP50专员/中学TOP20专员
            final int topCount;
            if (indicatorTypeEnum == AgentRankingService.IndicatorType.MIDDLE_REG || indicatorTypeEnum == AgentRankingService.IndicatorType.MIDDLE_ENGLISH_INC_1 || indicatorTypeEnum == AgentRankingService.IndicatorType.MIDDLE_MATH_INC_1
                    || indicatorTypeEnum == AgentRankingService.IndicatorType.MIDDLE_ENGLISH_TEACHER_REG || indicatorTypeEnum == AgentRankingService.IndicatorType.MIDDLE_MATH_TEACHER_REG) {
                topCount = 20;
            } else {
                topCount = 50;
            }
            //小学TOP50专员/中学TOP20专员+自己负责范围内的所有下属专员或者自己
            dayRankingVOList = dayRankingVOList.stream().filter(item -> item.getRanking() <= topCount || item.isBelongToOwnGroup()).collect(Collectors.toList());
            //小学TOP50专员/中学TOP20专员+自己负责范围内的所有下属专员或者自己
            monthRankingVOList = monthRankingVOList.stream().filter(item -> item.getRanking() <= topCount || item.isBelongToOwnGroup()).collect(Collectors.toList());

        }

        MapMessage mapMessage = MapMessage.successMessage();

        Map<Long, AgentPerformanceRankingVO> dayRankingVOMap = dayRankingVOList.stream().collect(Collectors.toMap(AgentPerformanceRankingVO::getUserId, Function.identity()));
        AgentPerformanceRankingVO dayOwnVO = dayRankingVOMap.get(getCurrentUserId());
        if (null != dayOwnVO) {
            mapMessage.put(DAY_OWN_DATA_TYPE, dayOwnVO);
        }
        Map<Long, AgentPerformanceRankingVO> monthRankingVOMap = monthRankingVOList.stream().collect(Collectors.toMap(AgentPerformanceRankingVO::getUserId, Function.identity()));
        AgentPerformanceRankingVO monthOwnVO = monthRankingVOMap.get(getCurrentUserId());
        if (null != monthOwnVO) {
            mapMessage.put(MONTH_OWN_DATA_TYPE, monthOwnVO);
        }
        mapMessage.add(AgentRankingService.DAY_DATA_TYPE, dayRankingVOList);
        mapMessage.add(AgentRankingService.MONTH_DATA_TYPE, monthRankingVOList);
        mapMessage.add("showOnGroupBtn", agentRankingService.showBelongToOwnGroup(currentUser, 3));
        mapMessage.add("showGroupRanking", !currentUser.isBusinessDeveloper());
        return mapMessage;
    }

    /**
     * 获取分区排行
     * @return
     */
    @RequestMapping(value = "/group_ranking.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getGroupRanking() {
        String indicatorType = getRequestString("indicatorType");
        if (StringUtils.isBlank(indicatorType)){
            return MapMessage.errorMessage("指标类型不能为空");
        }
        AgentRankingService.IndicatorType indicatorTypeEnum = AgentRankingService.IndicatorType.valueOf(indicatorType);
        AuthCurrentUser currentUser = getCurrentUser();
        Map<String, List<AgentPerformanceRankingVO>> rankingMap = agentRankingService.getAllCityRankingWithOwnGroupSign(indicatorTypeEnum, currentUser);
        MapMessage mapMessage = MapMessage.successMessage();

        List<AgentPerformanceRankingVO> dayRankingVOList = rankingMap.get(AgentRankingService.DAY_DATA_TYPE);
        List<AgentPerformanceRankingVO> monthRankingVOList = rankingMap.get(AgentRankingService.MONTH_DATA_TYPE);
        if (!currentUser.isCountryManager() && !currentUser.isBuManager()) {
            //小学TOP20分区/中学TOP10分区
            final int topCount;
            if (indicatorTypeEnum == AgentRankingService.IndicatorType.MIDDLE_REG || indicatorTypeEnum == AgentRankingService.IndicatorType.MIDDLE_ENGLISH_INC_1 || indicatorTypeEnum == AgentRankingService.IndicatorType.MIDDLE_MATH_INC_1
                    || indicatorTypeEnum == AgentRankingService.IndicatorType.MIDDLE_ENGLISH_TEACHER_REG || indicatorTypeEnum == AgentRankingService.IndicatorType.MIDDLE_MATH_TEACHER_REG) {
                topCount = 10;
            } else {
                topCount = 20;
            }
            //小学TOP20分区/中学TOP10分区+自己负责的所有下属分区或者自己所属分区
            dayRankingVOList = dayRankingVOList.stream().filter(item -> item.getRanking() <= topCount || item.isBelongToOwnGroup()).collect(Collectors.toList());
            //小学TOP20分区/中学TOP10分区+自己负责的所有下属分区或者自己所属分区
            monthRankingVOList = monthRankingVOList.stream().filter(item -> item.getRanking() <= topCount || item.isBelongToOwnGroup()).collect(Collectors.toList());

        }
        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(getCurrentUserId());
        if (CollectionUtils.isNotEmpty(groupUserList)) {
            Map<Long, AgentPerformanceRankingVO> dayRankingVOMap = dayRankingVOList.stream().collect(Collectors.toMap(AgentPerformanceRankingVO::getGroupId, Function.identity()));
            AgentPerformanceRankingVO dayOwnVO = dayRankingVOMap.get(groupUserList.get(0).getGroupId());
            if (null != dayOwnVO) {
                mapMessage.put(DAY_OWN_DATA_TYPE, dayOwnVO);
            }
            Map<Long, AgentPerformanceRankingVO> monthRankingVOMap = monthRankingVOList.stream().collect(Collectors.toMap(AgentPerformanceRankingVO::getGroupId, Function.identity()));
            AgentPerformanceRankingVO monthOwnVO = monthRankingVOMap.get(groupUserList.get(0).getGroupId());
            if (null != monthOwnVO) {
                mapMessage.put(MONTH_OWN_DATA_TYPE, monthOwnVO);
            }
        }
        mapMessage.add(AgentRankingService.DAY_DATA_TYPE, dayRankingVOList);
        mapMessage.add(AgentRankingService.MONTH_DATA_TYPE, monthRankingVOList);
        mapMessage.add("showOnGroupBtn", agentRankingService.showBelongToOwnGroup(currentUser, 1));
        return mapMessage;
    }

    /**
     * 获取个人排名
     * @return
     */
    @RequestMapping(value = "/own_ranking.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage ownRanking() {
        Integer serviceType = getRequestInt("serviceType",1);   //业务类型	1：小学作业   3：家长
        int schoolLevelFlag = getRequestInt("schoolLevelFlag",1);
        Long userId = getCurrentUserId();
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        Long id = 0L;
        Integer idType = AgentConstants.INDICATOR_TYPE_USER;
        if (userRole == AgentRoleType.BusinessDeveloper){
            id = userId;
            idType = AgentConstants.INDICATOR_TYPE_USER;
        }else if (userRole == AgentRoleType.CityManager){
            AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
            if (groupUser != null){
                id = groupUser.getGroupId();
                idType = AgentConstants.INDICATOR_TYPE_GROUP;
            }
        }else {
            return MapMessage.errorMessage("该角色不支持查看排名！");
        }
        return MapMessage.successMessage().add("dataMap",agentRankingService.ownRanking(id,idType,serviceType,schoolLevelFlag));
    }
}
