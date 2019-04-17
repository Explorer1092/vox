package com.voxlearning.utopia.admin.controller.equator.mission;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.equator.service.mission.api.constants.mission.MissionEventType;
import com.voxlearning.equator.service.mission.api.data.mission.MissionEvent;
import com.voxlearning.equator.service.mission.client.MissionServiceClient;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * @author Ruib
 * @since 2018/7/23
 */
@Slf4j
@Controller
@RequestMapping("/equator/mission/event/")
public class MissionEventController extends AbstractEquatorController {
    @Inject private MissionServiceClient missionServiceClient;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET})
    public String nerazzurriTaskEventPage() {
        return "/equator/mission/event/index";
    }

    // 完成应用闯关任务
    @RequestMapping(value = "taskapppractice.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage taskFinished_app_practice() {
        long studentId = getRequestLong("studentId");
        String appKey = getRequestString("appKey");
        int score = getRequestInt("score");
        String grade = getRequestString("grade");
        String term = getRequestString("term");

        User user = userLoaderClient.loadUser(studentId);
        if (null == user || !user.isStudent()) return MapMessage.errorMessage("student id error.");

        OrderProductServiceType type = OrderProductServiceType.safeParse(appKey);
        if (type == OrderProductServiceType.Unknown) return MapMessage.errorMessage("app key error.");

        MissionEvent event = new MissionEvent(MissionEventType.app_practice, studentId);
        event.addAttribute("appKey", appKey);
        event.addAttribute("score", String.valueOf(score));

        if (StringUtils.isNotBlank(grade)) {
            if (!Arrays.asList("1", "2", "3", "4", "5", "6").contains(grade))
                return MapMessage.errorMessage("grade error.");
            event.addAttribute("grade", grade);
        }

        if (StringUtils.isNotBlank(term)) {
            if (!Arrays.asList("1", "2", "0").contains(term))
                return MapMessage.errorMessage("term error.");
            event.addAttribute("term", term);
        }

        missionServiceClient.getMissionService().sendEvent(event);
        return MapMessage.successMessage("事件发送成功");
    }

    // 完成应用闯关任务
    @RequestMapping(value = "assignmentstudy.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage assignmentFinished_study() {
        long studentId = getRequestLong("studentId");
        int score = getRequestInt("score");
        String templateId = getRequestString("templateId");
        int times = getRequestInt("times");

        User user = userLoaderClient.loadUser(studentId);
        if (null == user || !user.isStudent()) return MapMessage.errorMessage("student id error.");

        if (StringUtils.isBlank(templateId)) return MapMessage.errorMessage("template id error.");

        if (times <= 0) return MapMessage.errorMessage("times error.");

        MissionEvent event = new MissionEvent(MissionEventType.study, studentId);
        event.addAttribute("score", String.valueOf(score));
        event.addAttribute("templateId", templateId);
        event.addAttribute("times", String.valueOf(times));

        missionServiceClient.getMissionService().sendEvent(event);
        return MapMessage.successMessage("事件发送成功");
    }
}
