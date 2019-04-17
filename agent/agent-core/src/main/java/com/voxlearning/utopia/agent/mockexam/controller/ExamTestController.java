package com.voxlearning.utopia.agent.mockexam.controller;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPlanDao;
import com.voxlearning.utopia.agent.mockexam.domain.ExamPlanDomain;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPlan;
import com.voxlearning.utopia.agent.mockexam.integration.ExamPaperClient;
import com.voxlearning.utopia.agent.mockexam.service.ExamPlanService;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 用于测试的rest接口
 *
 * @author xiaolei.li
 * @version 2018/8/29
 */
@Slf4j
@Controller
@RequestMapping("mockexam/exam/test")
public class ExamTestController extends AbstractAgentController {

    @Resource
    private ExamPaperClient examPaperClient;

    @Resource
    private ExamPlanDao examPlanDao;

    @Resource
    private ExamPlanDomain examPlanDomain;

    @Resource
    private ExamPlanService examPlanService;

    /**
     * 试卷检查
     */
//    @OperationCode(PLAN_QUERY)
    @ResponseBody
    @RequestMapping(value = "check.vpage", method = RequestMethod.GET)
    public ExamPaperClient.CheckResponse detail(@RequestParam Long id) {
        ExamPlan plan = examPlanDomain.retrieve(id);
        ExamPaperClient.CheckResponse response = examPaperClient.check(plan);
        return response;
    }

    /**
     * 查询学生人数
     */
//    @OperationCode(PLAN_QUERY)
    @ResponseBody
    @RequestMapping(value = "studentcount.vpage", method = RequestMethod.GET)
    public MapMessage MapMessage(@RequestParam Long id) {
        Result<Integer> result = examPlanService.countExamStudent(id);
        return ViewBuilder.fetch(result);
    }
}
