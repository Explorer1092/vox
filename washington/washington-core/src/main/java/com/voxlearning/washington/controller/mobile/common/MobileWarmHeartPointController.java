package com.voxlearning.washington.controller.mobile.common;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.WarmHeartPlanPointService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

@Controller
@RequestMapping(value = "/usermobile/warm/heart/plan")
@Slf4j
public class MobileWarmHeartPointController extends AbstractMobileController {

    @ImportService(interfaceClass = WarmHeartPlanPointService.class)
    private WarmHeartPlanPointService heartPlanPointService;

    @RequestMapping(value = "student_show.vpage")
    @ResponseBody
    public MapMessage studentShow() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return noLoginResult;
        }

        return heartPlanPointService.studentShow(user.getId());
    }

    @RequestMapping(value = "teacher_show.vpage")
    @ResponseBody
    public MapMessage teacherShow() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return noLoginResult;
        }

        return heartPlanPointService.teacherShow(user.getId());
    }

    @RequestMapping(value = "parent_show.vpage")
    @ResponseBody
    public MapMessage parentShow() {
        User user = currentUser();
        if (user == null || !user.isParent()) {
            return noLoginResult;
        }
        Long sid = getRequestLong("sid");
        if (Objects.equals(sid, 0L)) {
            sid = null;
        }
        return heartPlanPointService.parentShow(user.getId(), sid);
    }

    @RequestMapping(value = "student_click_go_assign.vpage")
    @ResponseBody
    public MapMessage studentClickGoAssignBtn() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return noLoginResult;
        }

        return heartPlanPointService.studentClickGoAssignBtn(user.getId());
    }

    @RequestMapping(value = "parent_click_go_assign.vpage")
    @ResponseBody
    public MapMessage parentClickGoAssignBtn() {
        User user = currentUser();
        if (user == null || !user.isParent()) {
            return noLoginResult;
        }

        return heartPlanPointService.parentClickGoAssignBtn(user.getId());
    }

}
