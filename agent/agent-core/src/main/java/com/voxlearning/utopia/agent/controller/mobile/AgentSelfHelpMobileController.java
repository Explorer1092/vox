package com.voxlearning.utopia.agent.controller.mobile;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.selfhelp.AgentSelfHelp;
import com.voxlearning.utopia.agent.persist.entity.selfhelp.AgentSelfHelpType;
import com.voxlearning.utopia.agent.service.selfhelp.AgentSelfHelpService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * @Auther: DELL7050
 * @Date: 2018/6/12 14:07
 * @Description:
 */
@Controller
@RequestMapping("/mobile/selfHelp")
public class AgentSelfHelpMobileController extends AbstractAgentController {
    @Inject
    private AgentSelfHelpService agentSelfHelpService;


    /**
     * 天玑 查看资料包详情用
     * @return
     */
    @RequestMapping(value = "selfHelpInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage selfHelpInfo() {
        String itemId = getRequestString("id");
        MapMessage result = MapMessage.successMessage();
        List<AgentSelfHelpType> dataList = agentSelfHelpService.getSelfHelpTypeList();
        if (!StringUtils.isBlank(itemId)) {
            AgentSelfHelp selfHelp = agentSelfHelpService.getById(itemId);
            List<Map<String,String>> list = agentSelfHelpService.assemblyPacketInfo(selfHelp);
            result.put("packets",list);//资料包列表
            result.put("selfHelp",selfHelp);//事项条目
        }
        result.put("dataList",dataList);//事项类型列表
        return result;
    }

    /**
     * 天玑 查询事项联系人列表
     * @return
     */
    @RequestMapping(value = "selfHelpList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage selfHelpList(){
        return agentSelfHelpService.findHelpList();
    }
}
