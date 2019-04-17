package com.voxlearning.utopia.agent.controller.common;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * common 功能
 */
@Controller
@RequestMapping("/common")
@Slf4j
public class CommonController extends AbstractAgentController {
    @Inject
    AgentRegionService agentRegionService;
    /**
     * 加载region信息
     * 此处是提供给前端公用的加载区域树的结构，如果前端不是需要区域树，请不要调用此方法
     */
    @RequestMapping(value = "region/loadregion.vpage", method = RequestMethod.GET)
    @ResponseBody
    String loadRegion() {
        try {
            List<Map<String, Object>> retList = new ArrayList<>();
            Map<String, Map<String, Object>> allRegionTree = agentRegionService.getAllRegionTreeCopy();

            Set<String> allKeySet = allRegionTree.keySet();
            for (String regionCode : allKeySet) {
                Map<String, Object> regionItem = allRegionTree.get(regionCode);
                if (regionItem.get("pcode") == null) {
                    retList.add(regionItem);
                }
            }

//            return JsonUtils.toJson(agentRegionService.loadUserRegionTree(getCurrentUser(), false));
            return JsonUtils.toJson(retList);
        } catch (Exception ex) {
            log.error("加载region失败，msg:{}", ex.getMessage(), ex);
            return "";
        }
    }
}
