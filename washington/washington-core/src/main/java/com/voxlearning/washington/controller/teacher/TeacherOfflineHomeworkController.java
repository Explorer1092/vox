package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2016/9/8
 */
@Controller
@RequestMapping("teacher/offline/homework")
public class TeacherOfflineHomeworkController extends AbstractTeacherController {
    @RequestMapping(value = "index.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage index() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        List<String> homeworkIds = StringUtils.toList(getRequestString("homeworkIds"), String.class);
        List<Long> clazzGroupIds = StringUtils.toLongList(getRequestString("clazzGroupIds"));
        return offlineHomeworkServiceClient.loadIndexData(teacher, homeworkIds, clazzGroupIds);
    }

    /**
     * 布置作业：去除了英语学科的限制
     */
    @RequestMapping(value = "assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveHomework() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String data = getRequestString("data");
        Map<String, Object> homeworkJson = JsonUtils.fromJson(data);
        if (MapUtils.isEmpty(homeworkJson)) {
            return MapMessage.errorMessage("参数错误");
        }
        HomeworkSourceType sourceType = HomeworkSourceType.of(SafeConverter.toString(homeworkJson.get("source")));
        return offlineHomeworkServiceClient.assignOfflineHomework(teacher, homeworkJson, sourceType);
    }

    /**
     * 作业单详情
     */
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage offlineHomeworkDetail() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        List<String> offlineHomeworkIds = StringUtils.toList(getRequestString("ohids"), String.class);
        if (CollectionUtils.isEmpty(offlineHomeworkIds)) {
            return MapMessage.errorMessage("作业单id不能为空");
        }
        String offlineHomeworkId = offlineHomeworkIds.get(0);
        return offlineHomeworkLoaderClient.loadOfflineHomeworkDetail(offlineHomeworkId);
    }
}
