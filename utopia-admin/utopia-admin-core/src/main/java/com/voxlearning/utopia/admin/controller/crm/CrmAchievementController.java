package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.action.api.document.Achievement;
import com.voxlearning.utopia.service.action.api.document.AchievementBuilder;
import com.voxlearning.utopia.service.action.api.document.UserAchievementLog;
import com.voxlearning.utopia.service.action.api.document.UserAchievementRecord;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 5/9/2016
 */
@Controller
@RequestMapping(value = "/crm/achievement/")
public class CrmAchievementController extends CrmAbstractController {

    @Inject
    private ActionLoaderClient actionLoaderClient;

    @RequestMapping(value = "/detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        Long userId = getRequestLong("userId");
        if (0 == userId) {
            return "/crm/achievement/detail";
        }

        User user = userLoaderClient.loadUser(userId);
        if (null != user) {
            model.addAttribute("user", user);
        }

        List<Map<String, Object>> mappers = new ArrayList<>();

        List<UserAchievementRecord> uarList = actionLoaderClient.getRemoteReference().loadUserAchievementRecords(userId);
        if (CollectionUtils.isNotEmpty(uarList)) {
            List<UserAchievementLog> achievementLogs = actionLoaderClient.getRemoteReference().getUserAchievementLogs(userId);
            Map<String, UserAchievementLog> achievementLogMap = achievementLogs.stream()
                    .collect(Collectors.toMap(UserAchievementLog::getId, Function.identity()));

            for (UserAchievementRecord uar : uarList) {
                Achievement achievement = AchievementBuilder.build(uar);
                if (null == achievement || null == achievement.getType() || 0 == achievement.getRank()) continue;

                for (int i = 1; i <= achievement.getRank(); i++) {
                    Map<String, Object> mapper = new HashMap<>();
                    mapper.put("name", achievement.getType().getTitle());
                    mapper.put("level", i);

                    String achievementLogId = UserAchievementLog.generateId(userId, achievement.getType().name(), i);
                    if (achievementLogMap.containsKey(achievementLogId)) {
                        mapper.put("ct", achievementLogMap.get(achievementLogId).getCreateTime());
                    }

                    mappers.add(mapper);
                }
            }
        }

        model.addAttribute("achievements", mappers);

        return "/crm/achievement/detail";
    }
}
