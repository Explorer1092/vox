package com.voxlearning.washington.controller.open.v1.homework;

import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.ExLinkedHashMap;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.ocr.OcrFeedbackProblemType;
import com.voxlearning.utopia.service.newhomework.api.mapper.request.SaveOcrRecognitionRequest;
import com.voxlearning.utopia.service.newhomework.consumer.IndependentOcrServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.open.AbstractApiController;
import com.voxlearning.washington.controller.open.ApiConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.REQ_APP_KEY;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.REQ_DATA;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/12/25
 */
@Controller
@RequestMapping("/v1/newhomework/independent/ocr/")
public class IndependentOcrApiController extends AbstractApiController {

    @Inject private IndependentOcrServiceClient independentOcrServiceClient;

    /**
     * 提交识别结果
     */
    @RequestMapping(value = "batch/processresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage batchProcessOcrResult() {
        try {
            validateRequired(REQ_DATA, "答题数据");
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String data = getRequestString(REQ_DATA);
        Map<String, Object> requestMap = JsonUtils.fromJson(data);
        if (MapUtils.isEmpty(requestMap)) {
            return failMessage("提交结果数据异常");
        }
        User user = getApiRequestUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        SaveOcrRecognitionRequest request = getRequestObject(SaveOcrRecognitionRequest.class);
        if (request == null || CollectionUtils.isEmpty(request.getOcrMentalImageDetails())) {
            return failMessage("请求参数异常: {}", JsonUtils.toJson(request));
        }
        if (user.isStudent()) {
            request.setStudentId(user.getId());
        }
        if (user.isParent() && request.getStudentId() == null) {
            return failMessage("studentId不允许为空");
        }
        request.setVersion(getClientVersion());

        try {
            Map<String, Object> resultMap = AtomicLockManager.instance().wrapAtomic(independentOcrServiceClient)
                    .keys(user.getId())
                    .proxy()
                    .batchProcessOcrResult(user.getId(), request);
            MapMessage message = successMessage("提交成功!");
            message.putAll(resultMap);
            return message;
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("正在处理，请不要重复提交").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
        } catch (Exception e) {
            logger.error("IndependentOcrApiController.batchProcessOcrResult error. request:{}, e:{}", JsonUtils.toJson(request), e);
            return failMessage("提交失败, 请重试");
        }
    }


    /**
     * 识别报错
     */
    @RequestMapping(value = "error/report.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage reportError() {
        String questionId = getRequestString("questionId");
        String imgUrl = getRequestString("imgUrl");
        String coordinate = getRequestString("coordinate");
        String problemDescription = getRequestString("problemDescription");
        if (StringUtils.isBlank(imgUrl)) {
            return failMessage("图片地址为空");
        }
        if (StringUtils.isBlank(coordinate)) {
            return failMessage("坐标信息为空");
        }
        User user = getApiRequestUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long studentId = user.isStudent() ? user.getId() : getRequestLong("studentId");
        if (studentId == 0L) {
            return failMessage("学生ID不能为空");
        }
        List<String> problemTypeList = StringUtils.toList(getRequestString("problemTypes"), String.class);
        List<String> problemTypeNames = Arrays.stream(OcrFeedbackProblemType.values()).map(OcrFeedbackProblemType::name).collect(Collectors.toList());
        List<OcrFeedbackProblemType> objProblemTypes = problemTypeList.stream()
                .filter(problemTypeNames::contains)
                .map(OcrFeedbackProblemType::valueOf).collect(Collectors.toList());

        try {
            ExLinkedHashMap<String, String> kibanaMap = MapUtils.map("questionId", questionId,
                    "problemTypes", objProblemTypes,
                    "problemDescription", problemDescription,
                    "usertoken", user.getId(),
                    "ip", getWebRequestContext().getRealRemoteAddress(),
                    "clientName", getRequestString(REQ_APP_KEY),
                    "clientVersion", getClientVersion(),
                    "logType", "REPORT_ERROR",
                    "op", "aiImageLogs",
                    "env", RuntimeMode.current().getStageMode());

            return convert2NativeMessage(independentOcrServiceClient.reportError(studentId, imgUrl, coordinate, kibanaMap));
        } catch (Exception ex) {
            logger.error("IndependentOcrApiController.ocrCorrect error. studentId:{}, questionId:{}, imgUrl{}, coordinate:{}, e:{}", studentId, questionId, imgUrl, coordinate, ex);
            return failMessage("上报异常");
        }
    }

    /**
     * 检查记录
     */
    @RequestMapping(value = "result/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchOcrResultList() {
        User user = getApiRequestUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long studentId = user.isStudent() ? user.getId() : getRequestLong("studentId");
        if (studentId < 1) {
            return failMessage("studentId不允许为空");
        }

        int pageNum = getRequestInt("pageNum", 1);
        int pageSize = getRequestInt("pageSize", 10);
        return independentOcrServiceClient.fetchOcrResultList(studentId, pageNum < 1 ? 1 : pageNum, pageSize);
    }

    /**
     * 删除检查记录
     */
    @RequestMapping(value = "delete/result.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteResult(@RequestParam("processIds") String processIds) {
        User user = getApiRequestUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long studentId = user.isStudent() ? user.getId() : getRequestLong("studentId");
        if (studentId < 1) {
            return failMessage("studentId不允许为空");
        }
        List<String> processIdList = StringUtils.toList(processIds, String.class);
        if (CollectionUtils.isEmpty(processIdList)) {
            return failMessage("请求参数异常, processIds: {}", processIds);
        }

        MapMessage message = independentOcrServiceClient.deleteResult(processIdList, studentId);
        return convert2NativeMessage(message);
    }

    /**
     * 反馈
     */
    @RequestMapping(value = "feedback/problem.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage feedbackProblem(@RequestParam("imgUrl") String imgUrl, @RequestParam("problemTypes") String problemTypes, @RequestParam("problemDescription") String problemDescription) {
        User user = getApiRequestUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (StringUtils.isBlank(imgUrl)) {
            return failMessage("imgUrl不允许为空");
        }
        Long studentId = user.isStudent() ? user.getId() : getRequestLong("studentId");
        if (studentId == 0L) {
            return failMessage("学生ID不能为空");
        }

        List<String> problemTypeNames = Arrays.stream(OcrFeedbackProblemType.values()).map(OcrFeedbackProblemType::name).collect(Collectors.toList());
        List<OcrFeedbackProblemType> objProblemTypes = StringUtils.toList(problemTypes, String.class).stream()
                .filter(problemTypeNames::contains)
                .map(OcrFeedbackProblemType::valueOf).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(objProblemTypes)) {
            return MapMessage.errorMessage("请选择正确的问题类型");
        }

        LogCollector.info("backend-general", MapUtils.map(
                "usertoken", user.getId(),
                "studentId", studentId,
                "problemTypes", objProblemTypes,
                "problemDescription", problemDescription,
                "logType", "FEED_BACK",
                "imgUrl", imgUrl,
                "clientName", getRequestString(REQ_APP_KEY),
                "clientVersion", getClientVersion(),
                "ip", getWebRequestContext().getRealRemoteAddress(),
                "op", "aiImageLogs",
                "env", RuntimeMode.current().getStageMode()
        ));
        return successMessage("反馈成功");
    }


    /**
     * 根据题ID查询答案和解析
     */
    @RequestMapping(value = "fetch/answer.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchAnswerAndAnalysis(@RequestParam("questionIds") String questionIds) {
        User user = getApiRequestUser();
        if (user == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        List<String> questionIdList = StringUtils.toList(questionIds, String.class);
        if (CollectionUtils.isEmpty(questionIdList)) {
            return failMessage("请求参数异常, questionIds: {}", questionIds);
        }

        return independentOcrServiceClient.fetchAnswerAndAnalysis(questionIdList);
    }

    /**
     * 口算错因分析
     */
    @RequestMapping(value = "mental/symptomanalysis.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage mentalSymptomAnalysis() {
        User user = getApiRequestUser();
        if (user == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long studentId = user.isStudent() ? user.getId() : getRequestLong("studentId");
        if (studentId == 0L) {
            return failMessage("学生ID不能为空");
        }
        String data = getRequestString("data");
        Map<String, Object> dataJson = JsonUtils.fromJson(data);
        if (MapUtils.isEmpty(dataJson)) {
            return MapMessage.errorMessage("参数错误");
        }
        String imgUrl = SafeConverter.toString(dataJson.get("imgUrl"));
        if (StringUtils.isEmpty(imgUrl)) {
            return MapMessage.errorMessage("图片地址为空");
        }
        List<String> textList = (List<String>) dataJson.get("texts");
        if (CollectionUtils.isEmpty(textList)) {
            return MapMessage.errorMessage("texts为空");
        }
        MapMessage mapMessage = independentOcrServiceClient.mentalSymptomAnalysis(imgUrl, textList, studentId);
        if (mapMessage.isSuccess()) {
            return successMessage().add("symptomAnalysisMap", mapMessage.get("symptomAnalysisMap"));
        } else {
            return failMessage(mapMessage.getInfo());
        }
    }

    /**
     * 保存我的练习册
     */
    @RequestMapping(value = "ocrworkbook/save.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage saveOcrStudentWorkbook(@RequestParam("backCoverImgUrls") String backCoverImgUrls) {
        User user = getApiRequestUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long studentId = user.isStudent() ? user.getId() : getRequestLong("studentId");
        if (studentId < 1) {
            return MapMessage.errorMessage("studentId不允许为空");
        }
        List<String> backCoverImgUrlList = StringUtils.toList(backCoverImgUrls, String.class);
        if (CollectionUtils.isEmpty(backCoverImgUrlList)) {
            return failMessage("请求参数异常, backCoverImgUrls: {}", backCoverImgUrls);
        }
        List<Map<String, Object>> results = independentOcrServiceClient.saveOcrStudentWorkbook(backCoverImgUrlList, studentId);
        return successMessage("保存成功").add("results", results);
    }

}
