package com.voxlearning.utopia.agent.controller.mobile.dimension;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.dimension.AgentDimensionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Map;

@Controller
@RequestMapping("/mobile/dimension/")
public class AgentDimensionController extends AbstractAgentController {

    @Inject
    private AgentDimensionService agentDimensionService;

    @RequestMapping("dimension_data.vpage")
    @ResponseBody
    public MapMessage getDimensions(){
        Long id = getRequestLong("id",0);
        Integer idType = getRequestInt("idType",1);

        Map<Integer, String> dataMap = agentDimensionService.getDimensionMap(getCurrentUserId(), id, idType, true);
        return MapMessage.successMessage().add("dataMap", dataMap);
    }

}
