package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 课外拓展作业，学生做作业
 *
 * @author zhangbin
 * @since 2017/7/22 15:08
 */

@Controller
@RequestMapping("/student/expand/homework")
public class StudentExpandHomeworkController extends AbstractController {

    @RequestMapping(value = "homeworklist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getHomeworkList() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage index() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "do.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage doHomework() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "questions.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map getQuestions() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "questions/answer.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map getQuestionsAnswer() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "processresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage processResult() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "processvoice.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage processVoice() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "type/result.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage typeResult() {
        return MapMessage.errorMessage("功能已下线");
    }
}
