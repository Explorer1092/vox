package com.voxlearning.washington.controller;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newexam.consumer.client.EvaluationReportLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.teacher.AbstractTeacherController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/container/evaluation")
public class EvaluationController extends AbstractTeacherController {


    @Inject private EvaluationReportLoaderClient evaluationReportLoaderClient;


    @RequestMapping(value = "fetchevaluationreport.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchEvaluationReportLoader() {
        String examId = this.getRequestString("examId");
        if (StringUtils.isBlank(examId)) {
            return MapMessage.errorMessage("参数错误");
        }
        User user = currentUser();
        try {
            return evaluationReportLoaderClient.fetchEvaluationReport(examId,user);
        } catch (Exception e) {
            logger.error("share Evaluation Report To Jzt of {}  examId  failed ",examId);
            return MapMessage.errorMessage();
        }
    }


    @RequestMapping(value = "fetchnewexamkkillinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchNewExamSkillInfo() {
        String examId = this.getRequestString("examId");
        if (StringUtils.isBlank(examId)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return evaluationReportLoaderClient.fetchNewExamSkillInfo(examId);
        } catch (Exception e) {
            logger.error("fetch NewExam Skill Info {}  examId  failed ",examId);
            return MapMessage.errorMessage();
        }
    }
}
