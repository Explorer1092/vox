/**
 * Author:   xianlong.zhang
 * Date:     2018/11/13 17:22
 * Description: 新任务中心接口
 * History:
 */
package com.voxlearning.utopia.agent.controller.mobile.taskcenter;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.taskmanage.AgentTaskManageService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping(value = "/mobile/agenttask")
public class AgentTaskMobileController extends AbstractAgentController {

    @Inject
    private AgentTaskManageService agentTaskManageService;
    @RequestMapping(value = "main_task_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage maintainTeacherList(){
        return agentTaskManageService.mainTaskList(getCurrentUserId());
    }

    @RequestMapping(value = "range_organization_role.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rangeOrganizationRole(){
        Long groupId = getRequestLong("groupId");
        String groupRoleType = getRequestString("groupRoleType");
        String roleType = getRequestString("roleType");
        return MapMessage.successMessage().add("dataMap",agentTaskManageService.rangeOrganizationRole(groupId,groupRoleType,roleType,getCurrentUserId()));
    }

    @RequestMapping(value = "sub_task_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage subTaskList(){
        Long userId = getRequestLong("userId");
        Long groupId = requestLong("groupId");
        String taskId = requestString("taskId");
        Long schoolId = getRequestLong("schoolId");//学校详情，待办任务,维护老师点击跳转
//        if(StringUtils.isBlank(taskId)){
//            return MapMessage.errorMessage("主任务id不能为空");
//        }


        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        if(roleType == AgentRoleType.BusinessDeveloper){
            userId = getCurrentUserId();
        }else{
            if(userId > 0){
                roleType = baseOrgService.getUserRole(userId);
                if(roleType != AgentRoleType.BusinessDeveloper){
                    return MapMessage.errorMessage("只能查看专员下的子任务列表");
                }
            }

        }
        return agentTaskManageService.subTaskList(roleType,userId,taskId,groupId,schoolId);
    }

    @RequestMapping(value = "task_statistic.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage taskStatistic(){
        String taskId = getRequestString("taskId");
        Long groupId = getRequestLong("groupId");
        String groupRoleType = getRequestString("groupRoleType");
        String userRoleType = getRequestString("userRoleType");
        if((StringUtils.isBlank(groupRoleType) || AgentGroupRoleType.nameOf(groupRoleType) == null)&& (StringUtils.isBlank(userRoleType) || AgentRoleType.nameOf(userRoleType) == null)){
            return MapMessage.errorMessage("部门级别 人员不能同时为空！");
        }
        if (StringUtils.isBlank(taskId)){
            return MapMessage.errorMessage("任务id不能为空！");
        }
        AuthCurrentUser currentUser = getCurrentUser();
        if (currentUser.isBusinessDeveloper()){
            return MapMessage.errorMessage("该角色不支持统计功能！");
        }
//        if (groupId == 0L){
//            return MapMessage.errorMessage("部门不正确！");
//        }
//        if (StringUtils.isBlank(groupRoleType) || AgentGroupRoleType.nameOf(groupRoleType) == null){
//            return MapMessage.errorMessage("部门级别不正确！");
//        }
//        if (StringUtils.isBlank(userRoleType) || AgentRoleType.nameOf(userRoleType) == null){
//            return MapMessage.errorMessage("人员角色不正确！");
//        }
        return MapMessage.successMessage().add("dataList",agentTaskManageService.taskStatistic(taskId ,groupId,AgentGroupRoleType.nameOf(groupRoleType),AgentRoleType.nameOf(userRoleType)));
    }
}
