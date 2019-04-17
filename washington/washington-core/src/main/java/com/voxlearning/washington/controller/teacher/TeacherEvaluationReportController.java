package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newexam.consumer.client.EvaluationReportLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/teacher/evaluation/report")
public class TeacherEvaluationReportController extends AbstractTeacherController {
    @Inject private EvaluationReportLoaderClient evaluationReportLoaderClient;


//    //大数据测试接口
//    @RequestMapping(value = "fetchbigdata.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage fetchBigData() {
//        String examId = this.getRequestString("examId");
//        if (StringUtils.isBlank(examId)) {
//            return MapMessage.errorMessage("参数错误");
//        }
//        try {
//            return evaluationReportLoaderClient.fetchBigData(examId);
//        } catch (Exception e) {
//            logger.error("share Evaluation Report To Jzt of {}  examId  failed ", examId);
//            return MapMessage.errorMessage();
//        }
//    }

    @RequestMapping(value = "shareevaluationreporttojzt.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage shareEvaluationReportToJzt() {
        String examId = this.getRequestString("examId");
        if (StringUtils.isBlank(examId)) {
            return MapMessage.errorMessage("参数错误");
        }
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("老师没有登入");
        }
        try {
            return evaluationReportLoaderClient.shareEvaluationReportToJzt(teacher, examId);
        } catch (Exception e) {
            logger.error("share Evaluation Report To Jzt of {}  examId  failed ", examId);
            return MapMessage.errorMessage();
        }
    }
}
