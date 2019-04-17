package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author zhangbin
 * @since 2017/7/31
 */

@Controller
@RequestMapping(value = "/v1/teacher/expand/homework/report")
@Slf4j
public class TeacherExpandHomeworkReportApiController extends AbstractTeacherApiController {

    @RequestMapping(value = "homeworklist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage teacherClazzList() {
        return failMessage("功能已下线");
    }

    @RequestMapping(value = "grouplist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage groupList() {
        return failMessage("功能已下线");
    }

    @RequestMapping(value = "deleteexpandhomework.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteExpandHomework() {
        return failMessage("功能已下线");
    }
}
