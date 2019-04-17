package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description: 学生端:自主练习Controller
 * For:H5页面
 * @author: Mr_VanGogh
 * @date: 2018/4/16 下午5:43
 */
@Controller
@RequestMapping("/student/independent/homework")
public class StudentIndependentHomeworkController extends AbstractController {

    /**
     * 保存学习时长到缓存中
     */
    @RequestMapping(value = "savelearntime.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveLearningTime() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 获取学习时长:1.从缓存中获取,没有返回空
     */
    @RequestMapping(value = "loadlearntime.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadLearningTime() {
       return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 获取教材列表
     */
    @RequestMapping(value = "booklist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadBookList() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 获取单元信息列表
     */
    @RequestMapping(value = "unitlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage unitList() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 获取单元关卡列表
     */
    @RequestMapping(value = "stagelist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage stageList() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 自主练习作业首页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage independentHomeworkIndex() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 自主练习做作业
     */
    @RequestMapping(value = "do.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage doIndependentHomework() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 自主练习作业中间结果
     */
    @RequestMapping(value = "type/result.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage independentHomeworkForObjectiveConfigTypeResult() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 自主练习作业题目
     */
    @RequestMapping(value = "questions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage independentHomeworkQuestions() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 自主练习作业答案
     */
    @RequestMapping(value = "questions/answer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage independentHomeworkQuestionsAnswer() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 自主练习作业提交答案
     */
    @RequestMapping(value = "processresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processIndependentHomeworkResult() {
        return MapMessage.errorMessage("功能已下线");
    }
}
