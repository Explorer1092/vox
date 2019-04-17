package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.action.api.document.UserGrowth;
import com.voxlearning.utopia.service.action.api.document.UserGrowthLog;
import com.voxlearning.utopia.service.action.client.ActionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xinxin
 * @since 5/9/2016
 */
@Controller
@RequestMapping(value = "/crm/growth/")
public class CrmGrowthController extends CrmAbstractController {

    @Inject private ActionLoaderClient actionLoaderClient;

    @RequestMapping(value = "/detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        Long userId = getRequestLong("userId");
        if (0 == userId) {
            return "/crm/student/growth/detail";
        }

        User user = userLoaderClient.loadUser(userId);
        if (null != user) {
            model.addAttribute("user", user);
        }

        UserGrowth userGrowth = actionLoaderClient.getRemoteReference().loadUserGrowth(userId);
        model.addAttribute("growth", null == userGrowth ? 0 : userGrowth.getGrowthValue());
        model.addAttribute("growthLevel", null == userGrowth ? 1 : userGrowth.toLevel());


        List<UserGrowthLog> userGrowthLogs = actionLoaderClient.getRemoteReference().getUserGrowthLogs(userId);
        List<Map<String, Object>> mappers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userGrowthLogs)) {
            userGrowthLogs.forEach(log -> {
                Map<String, Object> mapper = new HashMap<>();
                mapper.put("actionTime", log.getActionTime());
                mapper.put("delta", log.getDelta());
                mapper.put("type", log.getType().getTitle());
                mappers.add(mapper);
            });
        }
        model.addAttribute("logs", mappers);


        return "/crm/growth/detail";
    }
}
