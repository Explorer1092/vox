package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.WarmHeartPlanService;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

@Controller
@RequestMapping(value = "/teacherMobile/warm/heart/plan")
@Slf4j
public class MobileTeacherWarmHeartPlanController extends AbstractMobileTeacherController {

    @ImportService(interfaceClass = WarmHeartPlanService.class)
    private WarmHeartPlanService teacherWarmHeartPlanService;

    /**
     * 活动首页
     *
     * @return
     */
    @RequestMapping(value = "index.vpage")
    @ResponseBody
    public MapMessage index() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return noLoginResult;
        }
        return teacherWarmHeartPlanService.loadTeacherStatus(user);
    }

    /**
     * 启动计划
     *
     * @return
     */
    @RequestMapping(value = "assign.vpage")
    @ResponseBody
    public MapMessage assign() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return noLoginResult;
        }
        return teacherWarmHeartPlanService.assgin(user.getId());
    }

    /**
     * 查看班级学生详情
     *
     * @return
     */
    @RequestMapping(value = "clazz_info.vpage")
    @ResponseBody
    public MapMessage clazzInfo() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return noLoginResult;
        }
        return teacherWarmHeartPlanService.loadTeacherClazzInfo(user.getId());
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
            return noLoginResult;
        }

        long classId = getRequestLong("classId");
        if (Objects.equals(classId, 0L)) {
            return MapMessage.errorMessage("班级ID不可为空");
        }

        return teacherWarmHeartPlanService.loadClockInfo(user.getId(), classId);
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
            return noLoginResult;
        }

        long studentId = getRequestLong("studentId");
        if (Objects.equals(studentId, 0L)) {
            return MapMessage.errorMessage("必须选择一个学生");
        }

        return teacherWarmHeartPlanService.loadStudentClockInfo(studentId);
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
            return noLoginResult;
        }

        long studentId = getRequestLong("studentId");
        if (Objects.equals(studentId, 0L)) {
            return MapMessage.errorMessage("必须选择一个学生");
        }

        return teacherWarmHeartPlanService.praise(studentId);
    }

}


