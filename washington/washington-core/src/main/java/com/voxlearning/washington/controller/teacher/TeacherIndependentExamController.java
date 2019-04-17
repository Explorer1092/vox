package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.newexam.consumer.client.EvaluationReportLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/teacher/independent/exam")
public class TeacherIndependentExamController extends AbstractTeacherController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private EvaluationReportLoaderClient evaluationReportLoaderClient;

    /**
     * 班级列表
     */
    @RequestMapping(value = "clazzlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage clazzList() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 试卷列表
     */
    @RequestMapping(value = "paperlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadPaperList() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 预览试卷
     */
    @RequestMapping(value = "preview.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage previewPaper() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 布置测评
     */
    @RequestMapping(value = "assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveNewExam() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 删除测评
     */
    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteNewExam() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 获取测评列表
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadExamList() {
        return MapMessage.errorMessage("功能已下线");
    }
}
