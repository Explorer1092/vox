package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.ParentNewTermPlanService;
import com.voxlearning.utopia.service.campaign.api.TeacherNewTermPlanPointService;
import com.voxlearning.utopia.service.campaign.api.TeacherNewTermPlanService;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping(value = "/teacherMobile/new/term/plan")
@Slf4j
public class MobileTeacherNewTermPlanController extends AbstractMobileTeacherController {

    private static final MapMessage NO_LOGIN_MSG = MapMessage.errorMessage("未登录").add("code", 201);

    @ImportService(interfaceClass = TeacherNewTermPlanPointService.class)
    private TeacherNewTermPlanPointService teacherNewTermPlanPointService;
    @ImportService(interfaceClass = TeacherNewTermPlanService.class)
    private TeacherNewTermPlanService teacherNewTermPlanService;
    @ImportService(interfaceClass = ParentNewTermPlanService.class)
    private ParentNewTermPlanService parentNewTermPlanService;

    @RequestMapping(value = "index.vpage")
    @ResponseBody
    public MapMessage index() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return NO_LOGIN_MSG;
        }
        return teacherNewTermPlanService.loadTeacherStatus(user.getId());
    }

    @RequestMapping(value = "assign.vpage")
    @ResponseBody
    public MapMessage assign() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return NO_LOGIN_MSG;
        }
        return teacherNewTermPlanService.assgin(user.getId());
    }

    /**
     * 查看所有班级的学生参与详情
     *
     * @return
     */
    @RequestMapping(value = "clazz_info.vpage")
    @ResponseBody
    public MapMessage clazzInfo() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return NO_LOGIN_MSG;
        }
        return teacherNewTermPlanService.loadTeacherClazzInfo(user.getId());
    }

    /**
     * 查看学生打卡情况
     *
     * @return
     */
    @RequestMapping(value = "clock_info.vpage")
    @ResponseBody
    public MapMessage clockInfo() {

        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return NO_LOGIN_MSG;
        }

        long classId = getRequestLong("classId");
        if (Objects.equals(classId, 0L)) {
            return MapMessage.errorMessage("班级ID不可为空");
        }

        return teacherNewTermPlanService.loadClockInfo(user.getId(), classId);
    }

    /**
     * 查看单个学生打卡情况
     *
     * @return
     */
    @RequestMapping(value = "student_clock_info.vpage")
    @ResponseBody
    public MapMessage studentClockInfo() {

        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return NO_LOGIN_MSG;
        }

        long studentId = getRequestLong("studentId");
        if (Objects.equals(studentId, 0L)) {
            return MapMessage.errorMessage("必须选择一个学生");
        }

        return teacherNewTermPlanService.loadStudentClockInfo(studentId);
    }

    /**
     * 老师点赞发push
     *
     * @return
     */
    @RequestMapping(value = "praise.vpage")
    @ResponseBody
    public MapMessage praise() {

        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return NO_LOGIN_MSG;
        }

        long studentId = getRequestLong("studentId");
        if (Objects.equals(studentId, 0L)) {
            return MapMessage.errorMessage("必须选择一个学生");
        }

        return teacherNewTermPlanService.praise(studentId);
    }

    /**
     * 学生自主参与，点击“去指定我的计划”按钮
     *
     * @return
     */
    @RequestMapping(value = "student_join_plan.vpage")
    @ResponseBody
    public MapMessage studentJoinPlan() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return NO_LOGIN_MSG;
        }
        teacherNewTermPlanPointService.studentClickGoAssignBtn(user.getId());
        return teacherNewTermPlanService.sendParentPush(user.getId());
    }

    /**
     * 获取学生目标详情
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "student_target_detail.vpage")
    public MapMessage studentTargetDetail() {
        User user = currentUser();
        if (user == null || (!user.isStudent() && !user.isParent())) {
            return NO_LOGIN_MSG;
        }

        Long studentId = user.getId();
        if (user.isParent()) {
            studentId = getRequestLong("sid");
            if (studentId == 0) {
                List<StudentParentRef> studentRefs = parentLoaderClient.loadParentStudentRefs(user.getId());
                if (CollectionUtils.isEmpty(studentRefs)) {
                    return MapMessage.errorMessage("请指定一个孩子");
                } else {
                    studentId = studentRefs.get(0).getStudentId();
                }
            }
        }

        MapMessage mapMessage = teacherNewTermPlanService.getTargetDetail(studentId);
        mapMessage.add("user_type", user.getUserType());
        if (user.isParent()) {
            mapMessage.add("sign_up_status", parentNewTermPlanService.getSignUpStatus(user.getId()));
        }
        return mapMessage;
    }

    @RequestMapping(value = "sign_up.vpage")
    @ResponseBody
    public MapMessage teacherSignUp() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return NO_LOGIN_MSG;
        }
        return teacherNewTermPlanService.teacherSignUp(user.getId());
    }

}


