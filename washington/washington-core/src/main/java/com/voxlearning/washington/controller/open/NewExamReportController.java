package com.voxlearning.washington.controller.open;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.washington.controller.teacher.AbstractTeacherController;
import com.voxlearning.washington.data.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/container")
public class NewExamReportController extends AbstractTeacherController {

    @Inject private RaikouSystem raikouSystem;

    /**
     * 学生答题信息
     */
    @RequestMapping(value = "report/newpaperpersonalanswer.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage paperPersonalAnswer() {
        MapMessage message;
        Long userId = getRequestLong("userId");
        String examId = getRequestString("examId");

        User currentUser = currentUser();
        if (currentUser == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (examId == null) {
            return MapMessage.errorMessage("考试信息不存在");
        }
        if (currentUser.isStudent()) {
            //打点老版模考学生pc查看报告
            newExamReportLoaderClient.studentViewExamReportKafka(examId, userId, "pc");
        }
        try {
            message = newExamReportLoaderClient.fetchNewExamPaperQuestionPersonalAnswerInfo(examId, userId);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return message;
    }

    /**
     * 学生答题信息(一起测版)
     */
    @RequestMapping(value = "report/paperstudentanswer.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage paperStudentAnswer() {
        MapMessage message;
        Long userId = getRequestLong("userId");
        String examId = getRequestString("examId");

        User currentUser = currentUser();
        if (currentUser == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (examId == null) {
            return MapMessage.errorMessage("考试信息不存在");
        }
        if (currentUser.isStudent()) {
            //打点一起测学生pc查看报告
            newExamReportLoaderClient.studentViewExamReportKafka(examId, userId, "pc");
        }
        try {
            message = newExamReportLoaderClient.fetchNewExamPaperStudentAnswerInfo(examId, userId);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return message;
    }



    /**
     * 学生答题信息
     */
    @RequestMapping(value = "restoredata.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage restoreData() {
        MapMessage message;
        String ids = getRequestString("ids");

        List<String> newExamResultIds = StringUtils.toList(ids,String.class);

        try {
            message = newExamServiceClient.restoreData(newExamResultIds);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return message;
    }

    /**
     * 试卷题信息
     */
    @RequestMapping(value = "report/newpaperquestioninfov1.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage paperQuestionInfoV1() {
        MapMessage message;
        Long sid = getRequestLong("userId");
        String examId = getRequestString("examId");
        try {
            message = newExamReportLoaderClient.fetchNewExamPaperQuestionInfo(examId, sid);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return message;
    }

    /**
     * 试卷题信息
     */
    @RequestMapping(value = "report/newpaperquestioninfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage paperQuestionInfo() {
        MapMessage message;
        String paperId = getRequestString("paperId");
        String examId = getRequestString("examId");
        try {
            message = newExamReportLoaderClient.fetchNewExamPaperQuestionInfo(examId, paperId);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return message;
    }

    /**
     * 学生考试报告
     */
    @RequestMapping(value = "student/newexam/report.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage studentNewExamReport() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请先登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        String examId = getRequestString("exam_id");
        Long userId = getRequestLong("user_id");
        if (examId == null) {
            return MapMessage.errorMessage("考试信息不存在");
        }

        NewExamResult newExamResult = newExamResultLoaderClient.loadNewExamResult(examId, userId);
        User student = raikouSystem.loadUser(userId);
        if(newExamResult == null){
            if(user.isParent()){
                if(student == null){
                    return MapMessage.errorMessage("家长您现在还没绑定孩子");
                }
                return MapMessage.errorMessage("您的孩子："+ student.fetchRealname() +"没有参加本次考试哦");
            }else {
                if(student == null){
                    return MapMessage.errorMessage("没有参加本次考试哦");
                }
                return MapMessage.errorMessage(student.fetchRealname() +"没有参加本次考试哦");
            }
        }
        String domain = RuntimeMode.current().le(Mode.TEST) ? Constants.YQC_REPORT_URL_TEST : Constants.YQC_REPORT_URL_PROD;
        String url = UrlUtils.buildUrlQuery(domain + "/report_api/v1/student_report", MapUtils.map("exam_id", examId, "student_id", userId));
        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                    .get(url)
                    .contentType("application/json").socketTimeout(3 * 1000)
                    .execute();
            if (response != null && response.getStatusCode() == 200) {
                Map<String, Object> dataMap = JsonUtils.fromJson(response.getResponseString());
                Object success = dataMap.get("success");
                if ("true".equals(success)) {
                    return MapMessage.successMessage().add("data", dataMap.get("data"));
                } else {
                    return MapMessage.errorMessage(SafeConverter.toString(dataMap.get("msg")));
                }
            } else {
                return MapMessage.errorMessage("获取考试报告失败, 请稍后重试");
            }
        } catch (Exception e) {
            logger.error("获取学生考试报告接口失败:{}", url);
            return MapMessage.errorMessage("获取考试报告失败, 请稍后重试");
        }
    }

    @RequestMapping(value = "newexam/share/report.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage share() {
        Long clazzId = getRequestLong("clazzId");
        String examId = getRequestString("examId");
        return newExamReportLoaderClient.shareReport(examId, clazzId);
    }

    /**
     * 单元检测-作业单信息
     */
    @RequestMapping(value = "unit/test/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadUnitTestDetail() {
        String examIds = getRequestString("examIds");
        List<String> newExamIdList = StringUtils.toList(examIds, String.class);
        if (CollectionUtils.isEmpty(newExamIdList)) {
            return MapMessage.errorMessage("单元检测错误");
        }
        MapMessage mapMessage = newExamReportLoaderClient.loadUnitTestDetail(newExamIdList);
        NewExam newExam = newExamLoaderClient.load(newExamIdList.iterator().next());
        //老师信息
        if (newExam.getTeacherId() != null) {
            Teacher teacher = teacherLoaderClient.loadTeacher(newExam.getTeacherId());
            mapMessage.add("teacherUrl", NewHomeworkUtils.getUserAvatarImgUrl(getCdnBaseUrlAvatarWithSep(), teacher.fetchImageUrl()));
            mapMessage.add("teacherId", teacher.getId());
            mapMessage.add("teacherName", teacher.fetchRealname());
            mapMessage.add("teacherShareMsg", "我刚布置了单元检测，内容如下。建议家长督促学生完成。");
        }
        return mapMessage;
    }
}
