package com.voxlearning.utopia.mizar.controller.tangram;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.activity.TangramActivityStudent;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.service.business.consumer.TeacherActivityServiceClient;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarDepartment;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserSchool;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarUserSchoolLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 微课堂管理页面
 * Created by Yuechen.Wang on 2016/12/9.
 */
@Controller
@RequestMapping(value = "/activity/tangram")
public class TangramController extends AbstractMizarController {

    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private MizarUserSchoolLoaderClient mizarUserSchoolLoaderClient;
    @Inject private TeacherActivityServiceClient teacherActivityServiceClient;

    // 评委列表
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String tangramIndex(Model model) {
        // 读取出当前部门所有用户
        List<String> departments = mizarUserLoaderClient.loadUserDepartments(currentUserId())
                .stream()
                .map(MizarDepartment::getId)
                .collect(Collectors.toList());

        Map<String, MizarUser> userMap = mizarUserLoaderClient.loadDepartmentUsers(departments)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(MizarUser::getId, Function.identity(), (u, v) -> u));

        String currentUserId = currentUserId();
        List<Map<String, Object>> juryList = new ArrayList<>();

        for (Map.Entry<String, MizarUser> entry : userMap.entrySet()) {
            MizarUser user = entry.getValue();

            List<Long> schoolIds = mizarUserSchoolLoaderClient.loadByUserId(entry.getKey())
                    .stream()
                    .map(MizarUserSchool::getSchoolId)
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(schoolIds)) continue;

            Map<Long, School> schools = schoolLoaderClient.getSchoolLoader()
                    .loadSchools(schoolIds)
                    .getUninterruptibly();

            for (Map.Entry<Long, School> schoolEntry : schools.entrySet()) {
                Map<String, Object> info = new HashMap<>();
                info.put("name", user.getRealName());
                info.put("schoolId", schoolEntry.getKey());
                info.put("schoolName", schoolEntry.getValue().getShortName());
                info.put("judge", StringUtils.equals(user.getId(), currentUserId));

                juryList.add(info);
            }
        }
        model.addAttribute("juryList", juryList);
        return "tangram/index";
    }

    // 学生列表
    @RequestMapping(value = "studentlist.vpage", method = RequestMethod.GET)
    public String schoolData(Model model) {
        Long schoolId = getRequestLong("schoolId");
        if (schoolId <= 0L) {
            return "redirect: /activity/tangram/index.vpage";
        }

        List<Long> schoolIds = currentUserSchools();
        List<Map<String, Object>> schoolList = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly()
                .values()
                .stream()
                .map(school -> MapUtils.m("schoolId", school.getId(), "schoolName", school.getShortName()))
                .collect(Collectors.toList());

        model.addAttribute("schoolList", schoolList);

        List<Map<String, Object>> students = teacherActivityServiceClient.getRemoteReference()
                .loadTangramSchoolStudents(schoolId)
                .stream()
                .map(student -> {
                    Map<String, Object> info = new LinkedHashMap<>();
                    info.put("id", student.getId());
                    info.put("name", student.getStudentName());
                    info.put("code", student.getStudentCode());
                    info.put("class", student.classFullName());
                    info.put("score", student.getScore());
                    info.put("judged", student.alreadyJudged());
                    return info;
                }).collect(Collectors.toList());

        model.addAttribute("studentList", students);
        model.addAttribute("schoolId", schoolId);
        return "tangram/studentlist";
    }

    // 学生信息
    @RequestMapping(value = "student.vpage", method = RequestMethod.GET)
    public String studentData(Model model) {
        Long studentId = getRequestLong("student");
        if (studentId <= 0L) {
            return "redirect: /activity/tangram/index.vpage";
        }

        TangramActivityStudent student = teacherActivityServiceClient.getRemoteReference().loadTangramStudent(studentId);
        if (student == null || student.isDisabledTrue()) {
            return "redirect: /activity/tangram/index.vpage";
        }

        // 防止看不是自己负责学校的学生
        if (!currentUserSchools().contains(student.getSchoolId())) {
            return "redirect: /activity/tangram/index.vpage";
        }

        model.addAttribute("student", student);
        return "tangram/student";
    }

    @RequestMapping(value = "judge.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage judgeStudent() {
        Long studentId = getRequestLong("studentId");
        String score = getRequestString("score");
        String comment = getRequestString("comment");

        TangramActivityStudent student = teacherActivityServiceClient.getRemoteReference().loadTangramStudent(studentId);
        if (student == null || student.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的学生信息");
        }

        // 防止看不是自己负责学校的学生
        if (!currentUserSchools().contains(student.getSchoolId())) {
            return MapMessage.errorMessage("您不能查看该学生的作品");
        }

        if (StringUtils.isAnyBlank(score, comment)) {
            return MapMessage.errorMessage("请选择评分并填写评语");
        }

        try {
            MapMessage retMsg = teacherActivityServiceClient.judgeTangramStudent(
                    studentId, getCurrentUser().getUserId(), score, comment
            );
            if (!retMsg.isSuccess()) {
                return retMsg;
            }

            // 所有的未批改学生
            List<Long> unViewed = teacherActivityServiceClient.getRemoteReference()
                    .loadTangramSchoolStudents(student.getSchoolId())
                    .stream()
                    .filter(stu -> !stu.alreadyJudged() || Objects.equals(stu.getId(), studentId))
                    .map(TangramActivityStudent::getId)
                    .sorted(Long::compare)
                    .collect(Collectors.toList());

            // 找到当前学生的位置
            int index = unViewed.indexOf(studentId);
            // 去找下一个
            Long next = unViewed.get((index + 1) % unViewed.size());
            if (Objects.equals(next, studentId)) next = null;
            return MapMessage.successMessage().add("next", next);
        } catch (Exception ex) {
            logger.error("Failed judge tangram student masterpieces, student={}, jury={}", studentId, getCurrentUser().getAccountName(), ex);
            return MapMessage.errorMessage("打分失败，请联系技术人员");
        }
    }

}
