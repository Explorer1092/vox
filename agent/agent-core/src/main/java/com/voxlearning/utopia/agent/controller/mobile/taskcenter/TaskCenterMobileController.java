package com.voxlearning.utopia.agent.controller.mobile.taskcenter;


import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentTaskFeedbackType;
import com.voxlearning.utopia.agent.constants.AgentTaskType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.task.AgentTaskMain;
import com.voxlearning.utopia.agent.persist.entity.task.AgentTaskMainVO;
import com.voxlearning.utopia.agent.persist.entity.task.AgentTaskSubOnline;
import com.voxlearning.utopia.agent.service.taskmanage.AgentTaskCenterService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author deliang.che
 * @since 2018-05-25
 */
@Controller
@RequestMapping(value = "/mobile/taskcenter")
public class TaskCenterMobileController extends AbstractAgentController {
    @Inject
    private AgentTaskCenterService agentTaskCenterService;

    /**
     * 维护老师列表（主任务列表）
     * @return
     */
    @RequestMapping(value = "maintain_teacher_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage maintainTeacherList(){
        Integer type = requestInteger("type"); //1:个人  2：团队
        AuthCurrentUser currentUser = getCurrentUser();
        Map<String ,Object> dataMap = new HashMap<>();
        AgentRoleType userRole = baseOrgService.getUserRole(currentUser.getUserId());
        Long groupId = baseOrgService.getGroupUserByUser(currentUser.getUserId()).stream().map(AgentGroupUser::getGroupId).findFirst().orElse(null);
        List<AgentTaskMainVO> dataList = new ArrayList<>();
        //默认情况
        if (null == type){
            //专员
            if (userRole == AgentRoleType.BusinessDeveloper){
                //获取个人任务列表
                dataList = agentTaskCenterService.getMainTaskList(currentUser.getUserId(),AgentTaskMain.PERSONAL);
                //其他角色
            }else {
                //获取团队任务列表
                dataList = agentTaskCenterService.getMainTaskList(currentUser.getUserId(),AgentTaskMain.TEAM);
            }
            dataMap.put("roleName",userRole);
        //点击团队完成情况、我的任务的情况
        }else {
            dataList = agentTaskCenterService.getMainTaskList(currentUser.getUserId(),type);
        }

        //未完成的任务按照任务的截止时间由远到近排序
        List<AgentTaskMainVO> unfinishedList = dataList.stream().filter(item -> null != item && item.getStatus().equals("unfinished")).sorted(Comparator.comparing(AgentTaskMainVO::getEndTime)).collect(Collectors.toList());
        //已完成的任务按照任务的截止时间由近到远排序
        List<AgentTaskMainVO> finishedList = dataList.stream().filter(item -> null != item && item.getStatus().equals("finished")).sorted(Comparator.comparing(AgentTaskMainVO::getEndTime).reversed()).collect(Collectors.toList());
        dataMap.put("unfinishedList",unfinishedList);
        dataMap.put("finishedList",finishedList);
        dataMap.put("groupId",groupId);
        return MapMessage.successMessage().add("dataMap",dataMap);
    }

    /**
     * 线上维护老师，完成情况反馈
     * @return
     */
    @RequestMapping(value = "sub_task_online_feedback.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage subTaskOnlineFeedback(){
        String id = requestString("id");
        String feedbackType = requestString("feedbackType");
        String feedbackResult = requestString("feedbackResult");
        AgentTaskFeedbackType taskFeedbackType = AgentTaskFeedbackType.nameOf(feedbackType);
        if (StringUtils.isBlank(id)){
            return MapMessage.errorMessage("ID不正确");
        }
        AgentTaskSubOnline taskSubOnline = agentTaskCenterService.getSubTaskOnlineById(id);
        if (null == taskSubOnline){
            return MapMessage.errorMessage("该任务不存在");
        }
        taskSubOnline.setFeedbackType(taskFeedbackType);
        taskSubOnline.setFeedbackResult(feedbackResult);
        taskSubOnline.setIsFeedback(true);
        taskSubOnline.setFeedbackTime(new Date());
        agentTaskCenterService.updateSubTaskOnline(taskSubOnline);
        return MapMessage.successMessage();
    }

    /**
     * 个人子任务完成情况列表
     * @return
     */
    @RequestMapping(value = "sub_task_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage subTaskList(){
        AuthCurrentUser currentUser = getCurrentUser();
        String mainTaskId = requestString("mainTaskId");
        if (StringUtils.isBlank(mainTaskId)){
            return MapMessage.errorMessage("任务ID不正确");
        }
        String taskType = requestString("taskType");
        Map<String, Object> dataMap = new HashMap<>();
        //线上维护老师
        if (AgentTaskType.ONLINE_SERVICE_TEACHER == AgentTaskType.nameOf(taskType)){
            dataMap = agentTaskCenterService.subTaskOnlineList(mainTaskId, currentUser.getUserId());
            //进校维护老师
        }else if (AgentTaskType.INSCHOOL_SERVICE_TEACHER == AgentTaskType.nameOf(taskType)){
            dataMap = agentTaskCenterService.subTaskIntoSchoolList(mainTaskId, currentUser.getUserId());
        }
        return MapMessage.successMessage().add("dataMap",dataMap);
    }


    /**
     * 团队子任务完成情况列表
     * @return
     */
    @RequestMapping(value = "team_task_list.vpage")
    @ResponseBody
    public MapMessage teamTaskList(){
        MapMessage message = MapMessage.successMessage();
        String mainTaskId = requestString("mainTaskId");
        Long groupId = getRequestLong("groupId");
        int dimension = getRequestInt("dimension", 1);  // 1:默认   2：大区   3：区域   4：分区   5：专员

        if (StringUtils.isBlank(mainTaskId)){
            return MapMessage.errorMessage("任务ID不正确");
        }
        AgentTaskMain taskMain = agentTaskCenterService.getMainTaskById(mainTaskId);
        if (null == taskMain){
            return MapMessage.errorMessage("任务不存在");
        }
        //部门级别
        AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
        boolean flag = agentTaskCenterService.judgeGroupDimension(groupRoleType, dimension);
        if(!flag){
            return MapMessage.errorMessage("参数组合有误！");
        }

        List<Map<String, Object>> dataList = new ArrayList<>();
        // 专员统计列表
        if(dimension == 5 || (groupRoleType == AgentGroupRoleType.City && dimension == 1)){
            dataList.addAll(agentTaskCenterService.subTaskDataUserList(mainTaskId, groupId, taskMain.getTaskType()));
            //部门统计列表
        }else {
            dataList.addAll(agentTaskCenterService.subTaskDataGroupList(mainTaskId, groupId, taskMain.getTaskType(), dimension));
        }
        message.add("dataList",dataList);
        message.add("dimensions",agentTaskCenterService.fetchDimensionList(groupId));
        message.add("groupRoleType", groupRoleType);
        return message;
    }

}