package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.TeacherWinterPlanService;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

@Controller
@RequestMapping(value = "/teacherMobile/winter/plan")
@Slf4j
public class MobileTeacherWinterPlanController extends AbstractMobileTeacherController {

    private static final MapMessage NO_LOGIN_MSG = MapMessage.errorMessage("未登录").add("code", 201);

    @ImportService(interfaceClass = TeacherWinterPlanService.class)
    private TeacherWinterPlanService teacherWinterPlanService;

    @RequestMapping(value = "index.vpage")
    @ResponseBody
    public MapMessage index() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return NO_LOGIN_MSG;
        }
        return teacherWinterPlanService.loadTeacherStatus(user.getId());
    }

    @RequestMapping(value = "clazz_info.vpage")
    @ResponseBody
    public MapMessage clazzInfo() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return NO_LOGIN_MSG;
        }
        return teacherWinterPlanService.loadTeacherClazzInfo(user.getId());
    }

    @RequestMapping(value = "plan_detail.vpage")
    @ResponseBody
    public MapMessage planDetail() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return NO_LOGIN_MSG;
        }
        long studentId = getRequestLong("studentId");
        if (Objects.equals(studentId, 0)) {
            return MapMessage.errorMessage("学生ID不可为空");
        }

        return teacherWinterPlanService.loadStudentPlanDetail(studentId);
    }

    @RequestMapping(value = "assign.vpage")
    @ResponseBody
    public MapMessage assign() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return NO_LOGIN_MSG;
        }
        return teacherWinterPlanService.assgin(user.getId());
    }

    @RequestMapping(value = "student_assign.vpage")
    @ResponseBody
    public MapMessage studentAssign() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return NO_LOGIN_MSG;
        }
        return teacherWinterPlanService.studentAssgin(user.getId());
    }

}


