package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author guoqiang.li
 * @since 2017/5/2
 */
@Controller
@RequestMapping(value = "/v1/teacher/newexam")
public class TeacherNewExamApiController extends AbstractTeacherApiController {
    /**
     * 试卷列表
     */
    @RequestMapping(value = "paperlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadPaperList() {
        return failMessage("功能已下线");
    }

    /**
     * 布置考试
     */
    @RequestMapping(value = "assign.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage saveNewExam() {
        return failMessage("功能已下线");
    }

    /**
     * 删除考试
     */
    @RequestMapping(value = "delete.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteExam() {
        return failMessage("功能已下线");
    }
}
