package com.voxlearning.utopia.agent.controller.sysconfig;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.constants.AgentDateConfigType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentDateConfig;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDateConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * 日期配置
 * Created by yaguang.wang
 * on 2017/3/27.
 */
@Controller
@RequestMapping("/sysconfig/dateconfig")
@Slf4j
public class AgentDateConfigController extends AbstractAgentController {
    @Inject private AgentDateConfigService agentDateConfigService;

    @RequestMapping(value = "configpage.vpage", method = RequestMethod.GET)
    public String configPage(Model model) {
        AgentDateConfig cityManagerConfig = agentDateConfigService.findDateConfigByType(AgentDateConfigType.CITY_MANAGER_CONFIG_SCHOOL);
        if (cityManagerConfig != null) {
            model.addAttribute("cityManagerConfig", cityManagerConfig);
        }
        return "/sysconfig/dateconfig/agentdateconfigpage";
    }

    @RequestMapping(value = "updatecitymanagerconfigschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    @OperationCode("5229ed71d167487b")
    public MapMessage updateCityManagerConfigSchool() {
        Integer startTime = getRequestInt("start");
        Integer endTime = getRequestInt("end");
        return agentDateConfigService.updateCityManagerConfigSchoolTime(startTime, endTime);
    }
}
