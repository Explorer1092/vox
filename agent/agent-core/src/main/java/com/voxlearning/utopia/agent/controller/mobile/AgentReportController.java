package com.voxlearning.utopia.agent.controller.mobile;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentMonthly;
import com.voxlearning.utopia.agent.persist.entity.AgentWeekly;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.AgentMonthlyService;
import com.voxlearning.utopia.agent.service.mobile.AgentWeeklyService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AgentReportController
 *
 * @author song.wang
 * @date 2016/8/15
 */

@Controller
@RequestMapping("/mobile/report")
public class AgentReportController extends AbstractAgentController {

    @Inject
    private AgentWeeklyService agentWeeklyService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private WorkRecordService workRecordService;
    @Inject
    private AgentMonthlyService agentMonthlyService;

    @RequestMapping("/weekly_detail.vpage")
    public String weeklyReport(Model model){
        AuthCurrentUser user = getCurrentUser();
        Long userId = user.getUserId();
        Integer day = requestInteger("day");
        String returnPage = "";

        AgentRoleType agentRoleType = getUserRole(userId);
        if(agentRoleType != null){
            if(AgentRoleType.Region == agentRoleType){
                List<AgentUser> agentUserList = baseOrgService.getManagedGroupUsers(userId, false);
                List<AgentWeekly> weeklyList = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(agentUserList)){
                    weeklyList = agentUserList.stream().map(p -> agentWeeklyService.findByUserAndDay(p.getId(), day)).filter(p -> p != null).collect(Collectors.toList());
                }
                model.addAttribute("weeklyReportList", weeklyList);
                returnPage =  "rebuildViewDir/mobile/notice/regionManReport";
            }else if(AgentRoleType.CityManager == agentRoleType) {
                AgentWeekly agentWeekly = agentWeeklyService.findByUserAndDay(userId, day);
                model.addAttribute("weeklyReport", agentWeekly);
                returnPage =  "rebuildViewDir/mobile/notice/cityManReport";
            }else if(AgentRoleType.BusinessDeveloper == agentRoleType){
                // 设置周报数据
                AgentWeekly agentWeekly = agentWeeklyService.findByUserAndDay(userId, day);
                model.addAttribute("weeklyReport", agentWeekly);

                model.addAttribute("visitSuggest", convertWorkList(new ArrayList<>()));
                returnPage =  "rebuildViewDir/mobile/notice/agentReport";
            }
        }
        return returnPage;
    }

    private AgentRoleType getUserRole(Long userId){
        List<AgentRoleType> roleTypeList = baseOrgService.getUserRoleList(userId);
        if(CollectionUtils.isEmpty(roleTypeList)){
            return null;
        }
        return roleTypeList.get(0);
    }

    private List<Map<String, Object>> convertWorkList(List<CrmWorkRecord> workRecordList){
        if(CollectionUtils.isEmpty(workRecordList)){
            return Collections.emptyList();
        }
        List<Map<String, Object>> retList = new ArrayList<>();
        Map<String, Object> itemMap = null;
        for(CrmWorkRecord workRecord : workRecordList){
            itemMap = new HashMap<>();
            itemMap.put("schoolId", workRecord.getSchoolId());
            itemMap.put("schoolName", workRecord.getSchoolName());
            itemMap.put("workId", workRecord.getWorkerId());
            itemMap.put("workName", workRecord.getWorkerName());
            itemMap.put("partnerSuggest", workRecord.getPartnerSuggest());
            itemMap.put("date", DateUtils.dateToString(workRecord.getCreateTime(), "MM-dd"));
            retList.add(itemMap);
        }
        return retList;
    }


    @RequestMapping("/monthly_detail.vpage")
    public String monthlyReport(Model model){
        Integer month = requestInteger("month");
        Long userId = getCurrentUserId();
        AgentMonthly agentMonthly = agentMonthlyService.findByUserAndMonth(userId, month);
        model.addAttribute("monthlyReport", agentMonthly);
        return "rebuildViewDir/mobile/notice/monthlyReport";
    }

}
