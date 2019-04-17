package com.voxlearning.utopia.agent.controller.mobile.holidayhomework;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.holidayhomework.AgentHolidayHomeworkService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Objects;

/**
 * 假期作业
 * @author deliang.che
 * @since 2018/12/29
 */
@Controller
@RequestMapping(value = "/mobile/holiday/homework")
public class AgentHolidayHomeworkController extends AbstractAgentController {

    @Inject
    private AgentHolidayHomeworkService agentHolidayHomeworkService;

    /**
     * 统计概览
     * @return
     */
    @RequestMapping(value = "statistics_overview.vpage")
    @ResponseBody
    public MapMessage statisticsOverview(){
        int serviceType = getRequestInt("serviceType", 1);  //业务类型：1 小学，2 中学
        int subject = getRequestInt("subject", 1);          //学科：1 英语，2 数学，3 语文
        return agentHolidayHomeworkService.statisticsOverview(getCurrentUserId(),serviceType,subject);
    }

    /**
     * 获取专员排行
     * @return
     */
    @RequestMapping(value = "/user_ranking_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage userRankingList() {
        int serviceType = getRequestInt("serviceType", 1);  //业务类型：1 小学，2 中学
        int subject = getRequestInt("subject", 1);          //学科：1 英语，2 数学，3 语文
        AuthCurrentUser currentUser = getCurrentUser();
        return agentHolidayHomeworkService.userRankingList(serviceType,subject,currentUser);
    }

    /**
     * 获取分区排行
     * @return
     */
    @RequestMapping(value = "/group_ranking_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage groupRankingList() {
        int serviceType = getRequestInt("serviceType", 1);  //业务类型：1 小学，2 中学
        int subject = getRequestInt("subject", 1);          //学科：1 英语，2 数学，3 语文
        AuthCurrentUser currentUser = getCurrentUser();
        return agentHolidayHomeworkService.groupRankingList(serviceType,subject,currentUser);
    }

    /**
     * 统计列表
     * @return
     */
    @RequestMapping(value = "/statistics_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage statisticsList() {
        Long id = getRequestLong("id");
        Integer idType = getRequestInt("idType", AgentConstants.INDICATOR_TYPE_GROUP);
        int serviceType = getRequestInt("serviceType");  //业务类型：1 小学，2 中学
        int dimension = getRequestInt("dimension", 1);  // 1:默认   2：大区   3：区域   4：分区   5：专员
        int subject = getRequestInt("subject", 1);      //学科：1 英语，2 数学，3 语文
        //如果是市场总监，获取相应的市场部
        Long userId = getCurrentUserId();
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        if (userRole == AgentRoleType.Country && serviceType > 0){
            id = agentHolidayHomeworkService.getMarketingGroupId(serviceType);
        }
        return agentHolidayHomeworkService.statisticsList(id,idType,dimension,subject);
    }

    /**
     * 组织列表
     * @return
     */
    @RequestMapping(value = "/dimension_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dimensionList() {
        Long id = getRequestLong("id");
        Integer idType = getRequestInt("idType", AgentConstants.INDICATOR_TYPE_GROUP);
        int serviceType = getRequestInt("serviceType");  //业务类型：1 小学，2 中学
        //如果是市场总监，获取相应的市场部
        Long userId = getCurrentUserId();
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        if (userRole == AgentRoleType.Country && serviceType > 0){
            id = agentHolidayHomeworkService.getMarketingGroupId(serviceType);
        }
        return MapMessage.successMessage().add("dimensionList", baseOrgService.fetchDimensionList(id, idType));
    }

    /**
     * 学校列表
     * @return
     */
    @RequestMapping(value = "/school_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolList() {
        Long id = getRequestLong("id");
        Integer idType = getRequestInt("idType", AgentConstants.INDICATOR_TYPE_USER);
        int serviceType = getRequestInt("serviceType", 1);  //业务类型：1 小学，2 中学
        int subject = getRequestInt("subject", 1);          //学科：1 英语，2 数学，3 语文

        if(id <= 0L ||  (!Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER) && !Objects.equals(idType, AgentConstants.INDICATOR_TYPE_UNALLOCATED))){
            return MapMessage.errorMessage("参数错误");
        }
        return agentHolidayHomeworkService.schoolList(id,idType,serviceType,subject);
    }

}
