package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.TeacherNewTermPlanPointService;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

@Controller
@RequestMapping(value = "/teacherMobile/new/term/plan/point")
@Slf4j
public class MobileTeacherNewTermPlanPointController extends AbstractMobileTeacherController {

    private static final MapMessage NO_LOGIN_MSG = MapMessage.errorMessage("未登录").add("code", 201);

    @ImportService(interfaceClass = TeacherNewTermPlanPointService.class)
    private TeacherNewTermPlanPointService teacherNewTermPlanPointService;


    @RequestMapping(value = "student_show.vpage")
    @ResponseBody
    public MapMessage studentShow() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return NO_LOGIN_MSG;
        }

        return teacherNewTermPlanPointService.studentShow(user.getId());
    }

    @RequestMapping(value = "teacher_show.vpage")
    @ResponseBody
    public MapMessage teacherShow() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return NO_LOGIN_MSG;
        }

        return teacherNewTermPlanPointService.teacherShow(user.getId());
    }

    @RequestMapping(value = "parent_show.vpage")
    @ResponseBody
    public MapMessage parentShow() {
        User user = currentUser();
        if (user == null || !user.isParent()) {
            return NO_LOGIN_MSG;
        }
        Long sid = getRequestLong("sid");
        if (Objects.equals(sid, 0L)) {
            sid = null;
        }
        return teacherNewTermPlanPointService.parentShow(user.getId(), sid);
    }

    @RequestMapping(value = "student_click_go_assign.vpage")
    @ResponseBody
    public MapMessage studentClickGoAssignBtn() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return NO_LOGIN_MSG;
        }

        return teacherNewTermPlanPointService.studentClickGoAssignBtn(user.getId());
    }

    @RequestMapping(value = "parent_click_go_assign.vpage")
    @ResponseBody
    public MapMessage parentClickGoAssignBtn() {
        User user = currentUser();
        if (user == null || !user.isParent()) {
            return NO_LOGIN_MSG;
        }

        return teacherNewTermPlanPointService.parentClickGoAssignBtn(user.getId());
    }
}