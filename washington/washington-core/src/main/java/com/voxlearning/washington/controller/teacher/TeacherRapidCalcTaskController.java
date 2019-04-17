package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 速算王国老师发布任务
 * @author guoqiang.li
 * @since 2016/11/2
 */
@Controller
@RequestMapping("teacher/rapidcalc/task")
public class TeacherRapidCalcTaskController extends AbstractTeacherController {
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage taskList() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        if (teacher.getSubject() != Subject.MATH) {
            return MapMessage.errorMessage("暂时只支持数学学科");
        }
        String clazzs = getRequestString("clazzs");
        Set<Long> clazzGroupIdSet = parseGroupIds(clazzs);
        if (CollectionUtils.isEmpty(clazzGroupIdSet)) {
            return MapMessage.errorMessage("班组信息错误");
        }
        return MapMessage.errorMessage("活动已下线");
    }

    @RequestMapping(value = "assign.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage assignTask() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        if (teacher.getSubject() != Subject.MATH) {
            return MapMessage.errorMessage("暂时只支持数学学科");
        }
        String clazzs = getRequestString("clazzs");
        Set<Long> clazzGroupIdSet = parseGroupIds(clazzs);
        if (CollectionUtils.isEmpty(clazzGroupIdSet)) {
            return MapMessage.errorMessage("班组信息错误");
        }
        String taskName = getRequestString("taskName");
        if (StringUtils.isBlank(taskName)) {
            return MapMessage.successMessage("任务信息错误");
        }
        return MapMessage.errorMessage("活动已下线");
    }

    private Set<Long> parseGroupIds(String clazzs) {
        List<String> clazzIdGroupIdList = Arrays.asList(clazzs.trim().split(","));
        Set<Long> clazzGroupIdSet = new LinkedHashSet<>();
        clazzIdGroupIdList.forEach(str -> {
            String[] strs = str.split("_");
            if (strs.length == 2) {
                clazzGroupIdSet.add(SafeConverter.toLong(strs[1]));
            }
        });
        return clazzGroupIdSet;
    }
}
