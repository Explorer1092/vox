package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.WarmHeartPlanService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.mobile.teacher.AbstractMobileTeacherController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/studentMobile/warm/heart/plan")
@Slf4j
public class MobileStudentWarmHeartPlanController extends AbstractMobileTeacherController {

    @ImportService(interfaceClass = WarmHeartPlanService.class)
    private WarmHeartPlanService stuWarmHeartPlanService;

    /**
     * 活动首页
     *
     * @return
     */
    @RequestMapping(value = "index.vpage")
    @ResponseBody
    public MapMessage index() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return noLoginResult;
        }
        return stuWarmHeartPlanService.loadStudentStatus(user);
    }
}


