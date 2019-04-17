package com.voxlearning.utopia.admin.controller.ailesson;

import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.service.ai.client.AiVideoConfigServiceClient;
import com.voxlearning.utopia.service.ai.entity.AIVideoConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
　* @Description: app视频模块管理
　* @author zhiqi.yao
　* @date 2018/4/19 20:47
*/
@Controller
@RequestMapping("/chips/ai")
public class AiVideoConfigController extends AbstractAdminSystemController {

    @Inject
    private AiVideoConfigServiceClient aiVideoConfigServiceClient;

    @RequestMapping(value = "/video/save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveVideo() {

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String dataJson = getRequestString("data");

        AIVideoConfig config = JSONObject.parseObject(dataJson, AIVideoConfig.class);
        if (config == null || StringUtils.isBlank(config.getId())) {
            return MapMessage.errorMessage("参数异常");
        }
        return aiVideoConfigServiceClient.getRemoteReference().saveOrUpdateAIVideoConfigData(config);
    }

    @RequestMapping(value = "/video/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteDialogue() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }

        AIVideoConfig aiVideoConfig = aiVideoConfigServiceClient.getRemoteReference().loadAIVideoConfigById(id);
        if (aiVideoConfig == null || Boolean.TRUE.equals(aiVideoConfig.getDisabled())) {
            return MapMessage.errorMessage("要删除的数据不存在");
        }
        return aiVideoConfigServiceClient.getRemoteReference().deleteAIVideoConfig(id);
    }

    @RequestMapping(value = "/video/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadDialogueDetail() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String id = getRequestString("id");
        AIVideoConfig aiVideoConfig = aiVideoConfigServiceClient.getRemoteReference().loadAIVideoConfigById(id);
        return MapMessage.successMessage().add("data", aiVideoConfig);
    }

    @RequestMapping(value = "/video/index.vpage", method = RequestMethod.GET)
    public String loadDialogueListIndex(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }
        List<Map<String, Object>> result = new ArrayList<>();
        aiVideoConfigServiceClient.getRemoteReference().loadAllAIVideoConfigs().forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("title",e.getTitle());
            map.put("type",e.getType());
            map.put("videoUrl",e.getVideoUrl());
            map.put("updateTime", DateUtils.dateToString(e.getUpdateDate()));
            result.add(map);
        });
        model.addAttribute("result", result);
        return "ailesson/video_index";
    }

    @RequestMapping(value = "/video/addform.vpage", method = RequestMethod.GET)
    public String loadDialogueAddForm(Model model) {
        String id = getRequestString("id");
        model.addAttribute("id", id);
        return "ailesson/video_addform";
    }
}
