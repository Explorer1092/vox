package com.voxlearning.utopia.agent.mockexam.controller;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.mockexam.controller.view.ExamStudentScoreView;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.service.ExamPlanService;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamMakeupParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamReplenishParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamScoreQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamUploadParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamStudentScoreDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import java.util.HashMap;

import static com.voxlearning.utopia.agent.mockexam.controller.ResourceCode.Operation.*;

/**
 * @author xiaolei.li
 * @version 2018/8/12
 */
@Slf4j
@Controller
@RequestMapping("mockexam/exam")
public class ExamExamController extends AbstractAgentController {

    @Resource
    private ExamPlanService examPlanService;

    /**
     * 重考
     *
     * @param params 重考参数
     * @return 执行结果
     */
    @OperationCode(EXAM_REPLENISH)
    @RequestMapping(value = "replenish.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage replenish(@RequestBody ExamReplenishParams params) {
        AuthCurrentUser user = getCurrentUser();
        params.setOperatorId(user.getUserId());
        params.setOperatorName(user.getUserName());
        Result<HashMap<Long, String>> result = examPlanService.replenishExam(params);
        return ViewBuilder.fetch(result);
    }

    /**
     * 补考
     *
     * @param params 补考参数
     * @return 执行结果
     */
    @OperationCode(EXAM_MAKEUP)
    @RequestMapping(value = "makeup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage makeup(@RequestBody ExamMakeupParams params) {
        AuthCurrentUser user = getCurrentUser();
        params.setOperatorId(user.getUserId());
        params.setOperatorName(user.getUserName());
        Result<HashMap<Long, String>> result = examPlanService.makeupExam(params);
        return ViewBuilder.fetch(result);
    }

    /**
     * 成绩查询
     *
     * @param params 请求参数
     * @return 成绩详情
     */
    @OperationCode(EXAM_SCORE_QUERY)
    @RequestMapping(value = "scores.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage queryScore(@RequestBody ExamScoreQueryParams params) {
        Result<ExamStudentScoreDto> result = examPlanService.queryScore(params);
        if (result.isSuccess()) {
            ExamStudentScoreDto dto = result.getData();
            ExamStudentScoreView view = ExamStudentScoreView.Builder.build(dto);
            return ViewBuilder.success(view);
        } else {
            throw new BusinessException(result);
        }
    }

    /**
     * 查询学生人数
     */
    @OperationCode(PLAN_QUERY)
    @ResponseBody
    @RequestMapping(value = "student.vpage", method = RequestMethod.GET)
    public MapMessage MapMessage(@RequestParam Long id) {
        Result<Integer> result = examPlanService.countExamStudent(id);
        return ViewBuilder.fetch(result);
    }

//    /**
//     * 上传试卷
//     *
//     * @return 执行结果
//     */
//    @RequestMapping(value = "uploadPaper.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage upload() {
//        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
//        ExamUploadParams params = new ExamUploadParams();
//        params.setInputFile(multipartRequest.getFile("testPaper"));
//        Result<Boolean> result = examPlanService.uploadPaper(params);
//        return ViewBuilder.fetch(result);
//    }
}
