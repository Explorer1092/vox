package com.voxlearning.utopia.agent.controller.authority;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.authority.AgentRecordAuthority;
import com.voxlearning.utopia.agent.service.authority.AgentRecordAuthorityService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/authority/record")
public class AgentRecordAuthorityController extends AbstractAgentController {

    @Inject
    private AgentRecordAuthorityService recordAuthorityService;
    @Inject
    private AgentGroupSupport agentGroupSupport;



    @RequestMapping("authority_page.vpage")
    public String authorityPage(Model model){

        String recordId = getRequestString("recordId");
        Integer recordType = getRequestInt("recordType");

        model.addAttribute("allRoles", AgentRoleType.getValidRoleList());

        AgentRecordAuthority authority = recordAuthorityService.getRecordAuthority(recordId, recordType);

        List<Long> groupIds = new ArrayList<>();
        List<Integer> selectedRoleIds = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();

        Integer rule = 0;
        if(authority != null){
            if(CollectionUtils.isNotEmpty(authority.getGroupIds())){
                groupIds.addAll(authority.getGroupIds());
            }

            if(CollectionUtils.isNotEmpty(authority.getRoleIds())){
                selectedRoleIds.addAll(authority.getRoleIds());
            }

            if(CollectionUtils.isNotEmpty(authority.getUserIds())){
                userIds.addAll(authority.getUserIds());
            }

            if(authority.getRule() != null){
                rule = authority.getRule();
            }
        }

        model.addAttribute("groupIds", groupIds);
        model.addAttribute("selectedRoleIds", selectedRoleIds);
        model.addAttribute("userIds", userIds);
        model.addAttribute("rule", rule);

        return "common/authority";
    }


    @RequestMapping("save_authority.vpage")
    @ResponseBody
    public MapMessage saveAuthority(){
        String recordId = getRequestString("recordId");
        Integer recordType = getRequestInt("recordType");

        if(StringUtils.isBlank(recordId) || recordType < 1){
            return MapMessage.errorMessage("记录ID有误！");
        }

        Set<Long> groupIds = requestLongSet("groupIds"); //选择的部门列表
        Set<Integer> roleIds = requestIntegerSet("roleIds");
        Set<Long> userIds = requestLongSet("userIds");
        Integer rule = getRequestInt("rule");       // 0: 部门和角色取交集   1： 部门和角色取并集
        return recordAuthorityService.saveRecordAuthority(recordId, recordType, agentGroupSupport.removeChildGroup(groupIds), roleIds, userIds, rule);
    }


}
