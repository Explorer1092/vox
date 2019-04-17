package com.voxlearning.utopia.agent.controller.mobile.storagebox;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.storagebox.TeacherAuthInfo;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.storagebox.TeacherAuthScheduleService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 新老师认证进度
 * Created by yaguang.wang on 2016/12/7.
 */
@RequestMapping("/mobile/resource/teacherauth")
@Controller
@Slf4j
public class TeacherAuthScheduleController extends AbstractAgentController {
    @Inject private BaseOrgService baseOrgService;
    @Inject private TeacherAuthScheduleService teacherAuthScheduleService;

    /**
     * 根据当前人选择专员
     * @return
     */
    @RequestMapping(value = "new_teacher_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getNewTeacherList(){
        Long userId = getRequestLong("userId");
        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        if(roleType == AgentRoleType.BusinessDeveloper){
            userId = getCurrentUserId();
        }
        List<TeacherAuthInfo> dataList = teacherAuthScheduleService.loadSchoolTeacherAuthInfoByUserId(userId);
        return MapMessage.successMessage().add("dataList",dataList);
    }


    /**
     * 市经理及以上角色查询下边专员列表
     * @return
     */
    @RequestMapping(value = "developers.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getDevelopers(){
        AuthCurrentUser currentUser = getCurrentUser();
        if (currentUser.isBusinessDeveloper()){
            return MapMessage.errorMessage("您无权限查询");
        }
        List<Map<String,Object>> resutList = new ArrayList<>();
        List<AgentGroup> userGroups = baseOrgService.getUserGroups(currentUser.getUserId());
        if (CollectionUtils.isNotEmpty(userGroups)) {
            AgentGroup agentGroup = userGroups.get(0);
            List<AgentGroup> subGroupList = new ArrayList<>();
            if (agentGroup.fetchGroupRoleType() == AgentGroupRoleType.City){
                subGroupList.add(agentGroup);
            }else {
                List<AgentGroup> tempSubGroupList = baseOrgService.getSubGroupList(agentGroup.getId()).stream().filter(item -> item.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
                subGroupList.addAll(tempSubGroupList);
            }
            subGroupList.forEach(item -> {
                List<Long> userIds = baseOrgService.getGroupUsersByRole(item.getId(), AgentRoleType.BusinessDeveloper);
                List<AgentUser> users = baseOrgService.getUsers(userIds);
                users.forEach(user -> {
                    Map<String,Object> map = new HashMap<>();
                    map.put("userId",user.getId());
                    map.put("realName",user.getRealName());
                    map.put("groupName",item.getGroupName());
                    resutList.add(map);
                });
            });
            resutList.sort(Comparator.comparing(p->SafeConverter.toString(p.get("realName")), Collator.getInstance(java.util.Locale.CHINA)));
        }
        return MapMessage.successMessage().add("dataList",resutList);
    }
}
