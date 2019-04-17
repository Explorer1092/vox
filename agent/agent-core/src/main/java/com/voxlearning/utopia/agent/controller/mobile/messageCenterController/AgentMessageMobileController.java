package com.voxlearning.utopia.agent.controller.mobile.messageCenterController;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.JPushCrmType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.messagecenter.AgentMessageService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

@Controller
@RequestMapping("/mobile/messageCenter/")
@Slf4j
public class AgentMessageMobileController extends AbstractAgentController {

    @Inject private AgentMessageService agentMessageService;

    @Inject private BaseOrgService baseOrgService;

    @RequestMapping(value = "get_user_tag.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getUserTag(){
        MapMessage mapMessage = new MapMessage();
        AuthCurrentUser user = getCurrentUser();
        if(user != null){
            List<AgentGroupUser> groupUserByUser = baseOrgService.getGroupUserByUser(getCurrentUserId());
            Map<String,Object> map = new HashMap<>();
            Set<String> userTags = new HashSet<>();
            map.put("alias",user.getUserId());//jpush用的别名
//        map.put("groupTag", JPushCrmType.AGENT_GROUP.generateTag(groupUserByUser.get(0).getGroupId()));
//        map.put("roleTag",JPushCrmType.AGENT_GROUP.generateTag(groupUserByUser.get(0).getUserRoleId()));
            groupUserByUser.forEach(p -> {
                //获取用户的所有上级部门全部打标签
                Set<AgentGroup> agentGroups = baseOrgService.getAllParentGroupByGroupId(p.getGroupId());
                agentGroups.forEach(g -> userTags.add(JPushCrmType.AGENT_GROUP.generateTag(g.getId())));
                userTags.add(JPushCrmType.AGENT_GROUP.generateTag(p.getGroupId()));
                userTags.add(JPushCrmType.AGENT_ROLE.generateTag(p.getUserRoleId()));
            });
            map.put("tags",userTags);

            mapMessage.put("data",map);
        }
//        else{
//            mapMessage.put("result","900");
//            mapMessage.put("message","用户未登录");
//        }
        //不管成功失败都给客户端返回成功
        mapMessage.put("result","success");
        return mapMessage;
    }

    @RequestMapping(value = "read_push_message.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage readNotice() {
        MapMessage mapMessage = new MapMessage();
        mapMessage.put("result","success");
        String messageId = getRequestString("messageId");
        try {
            agentMessageService.readMessage(getCurrentUserId(), messageId);
            return mapMessage;
        } catch (Exception ex) {
            logger.error("Failed read push, user={}, notice={}", getCurrentUserId(), messageId);
            return mapMessage;
        }
    }
}
