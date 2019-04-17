package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.newhomework.api.SelfStudyHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyRotReport;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.service.SelfStudyHomeworkService;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.net.message.exam.SaveHomeworkResultRequest;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.flash.FlashVars;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.voxlearning.utopia.api.constant.ErrorCodeConstants.ERROR_CODE_PARAMETER;
import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.COURSE_APP_CONFIGTYPE;
import static com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType.ORAL_INTERVENTIONS;

/**
 * 自学任务的作业部分
 *
 * @author xuesong.zhang
 * @since 2017/2/7
 */
@Controller
@RequestMapping("/student/selfstudy/homework")
public class StudentSelfStudyHomeworkController extends AbstractController {

    @Getter
    @ImportService(interfaceClass = SelfStudyHomeworkLoader.class)
    private SelfStudyHomeworkLoader selfStudyHomeworkLoader;

    @Getter
    @ImportService(interfaceClass = SelfStudyHomeworkService.class)
    private SelfStudyHomeworkService selfStudyHomeworkService;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage index() {
        String homeworkId = getRequestParameter("homeworkId", "");
        User user = getHomeworkUser();
        if (user == null)
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");

        SelfStudyHomework homework = selfStudyHomeworkLoader.loadSelfStudyHomework(homeworkId);
        if (homework == null) {
            return MapMessage.errorMessage("作业不存在，或者作业已被老师删除")
                    .setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_DISABLED);
        }
        if (!user.getId().equals(homework.getStudentId())) {
            return MapMessage.errorMessage("订正作业与当前登录的孩子帐号不符，请检查后重试~")
                    .setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Map<String, Object> homeworkList = selfStudyHomeworkService.generateIndexData(homeworkId, user.getId());
        if (homeworkList.isEmpty()) {
            return MapMessage.errorMessage("作业已经不存在了");
        }
        return MapMessage.successMessage().add("homeworkList", homeworkList);
    }

    @RequestMapping(value = "do.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage doHomework(HttpServletRequest request) {
        User user = getHomeworkUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
        if (studentDetail == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        String homeworkId = getRequestString("homeworkId");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(getRequestString("objectiveConfigType"));
        SelfStudyHomework homework = selfStudyHomeworkLoader.loadSelfStudyHomeworkIncludeDisabled(homeworkId);
        if (homework == null || homework.isDisabledTrue()) {
            return MapMessage.errorMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_DISABLED);
        }

        FlashVars vars = new FlashVars(request);
        vars.add("uid", studentDetail.getId());
        vars.add("hid", homeworkId);
        vars.add("userId", studentDetail.getId());
        vars.add("homeworkId", homeworkId);
        vars.add("objectiveConfigType", objectiveConfigType);
        vars.add("objectiveConfigTypeName", objectiveConfigType != null ? objectiveConfigType.getValue() : "");
        vars.add("subject", homework.getSubject());
        vars.add("ipAddress", HttpRequestContextUtils.getWebAppBaseUrl());
        vars.add("learningType", StudyType.selfstudy);
        vars.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        vars.add("processResultUrl", UrlUtils.buildUrlQuery("/student/selfstudy/homework/processresult" + Constants.AntiHijackExt, MapUtils.m("sid", studentDetail.getId())));

        //作业类型为巩固课程--去查作业课程详情
        if (COURSE_APP_CONFIGTYPE.contains(objectiveConfigType)) {
            String courseUrl = "exam/flash/light/interaction/v2/course" + Constants.AntiHijackExt;
            if (objectiveConfigType.equals(ORAL_INTERVENTIONS)) {
                courseUrl = "exam/flash/video/course" + Constants.AntiHijackExt;
            }
            vars.add("courseUrl", courseUrl);

            vars.add("practices", selfStudyHomeworkService.fetchIntelDiagnosisCourse(homework.getId(), objectiveConfigType));
        } else {
            vars.add("questionUrl", UrlUtils.buildUrlQuery("/student/selfstudy/homework/questions" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "sid", studentDetail.getId())));
            vars.add("completedUrl", UrlUtils.buildUrlQuery("/student/selfstudy/homework/questions/answer" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "sid", studentDetail.getId())));
        }

        String flashVars = vars.getJsonParam();
        Map<String, Object> data = new HashMap<>();
        data.put("flashVars", flashVars);
        return MapMessage.successMessage().add("data", data);
    }

    @RequestMapping(value = "type/result.vpage", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public MapMessage typeResult() {
        String homeworkId = getRequestString("homeworkId");
        String objectiveConfigType = getRequestString("objectiveConfigType");

        User user = getHomeworkUser();
        if (user == null)
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);

        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");

        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail != null) {
            return selfStudyHomeworkService.homeworkForObjectiveConfigTypeResult(homeworkId, ObjectiveConfigType.of(objectiveConfigType), studentDetail.getId());
        } else {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }

    @RequestMapping(value = "questions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getQuestions() {
        String homeworkId = getRequestString("homeworkId");
        String objectiveConfigTypeStr = getRequestString("objectiveConfigType");
        String courseId = getRequestString("courseId");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
        return MapMessage.successMessage().add("result", selfStudyHomeworkService.loadHomeworkQuestions(homeworkId, objectiveConfigType, courseId));
    }

    @RequestMapping(value = "questions/answer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getQuestionsAnswer() {
        User student = getHomeworkUser();
        if (student == null || student.getId() == null) {
            return MapMessage.errorMessage("请登录");
        }
        String homeworkId = getRequestString("homeworkId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业已经不存在了");
        }
        String courseId = getRequestString("courseId");
        String objectiveConfigTypeStr = getRequestString("objectiveConfigType");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
        return MapMessage.successMessage().add("result", selfStudyHomeworkService.loadHomeworkQuestionsAnswer(homeworkId, student.getId(), objectiveConfigType, courseId));
    }

    @RequestMapping(value = "processresult.vpage")
    @ResponseBody
    public MapMessage processResult() {
        User user = getHomeworkUser();
        if (user == null) return MapMessage.errorMessage("请重新登录");
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "SELFSTUDY_PROCESS_RESULT", "/student/selfstudy/homework/processresult.vpage", 100)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        if (UserType.STUDENT != user.fetchUserType()) return MapMessage.errorMessage("请用学生账号登录");

        String data = getRequestParameter("data", "");
        if (StringUtils.isBlank(data)) {
            return MapMessage.errorMessage("参数错误").setErrorCode(ERROR_CODE_PARAMETER);
        }
        try {
            SaveHomeworkResultRequest result = JsonUtils.fromJson(data, SaveHomeworkResultRequest.class);
            if (result == null || StringUtils.isBlank(result.getHomeworkId())) {
                return MapMessage.errorMessage("提交结果数据异常" + JsonUtils.toJson(result));
            }
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(result.getObjectiveConfigType());
            if (objectiveConfigType == null) return MapMessage.errorMessage("作业形式为空" + JsonUtils.toJson(result));
            if (objectiveConfigType.equals(ObjectiveConfigType.ORAL_INTERVENTIONS) && result.getCourseGrasp() == null) {
                return MapMessage.errorMessage("提交结果数据异常" + JsonUtils.toJson(result));
            }
            if (!objectiveConfigType.equals(ObjectiveConfigType.ORAL_INTERVENTIONS) && StringUtils.isBlank(result.getQuestionId())) {
                return MapMessage.errorMessage("提交结果数据异常" + JsonUtils.toJson(result));
            }

            StudyType studyType = StudyType.of(result.getLearningType());
            if (studyType == null) {
                return MapMessage.errorMessage("学习类型异常" + JsonUtils.toJson(result));
            }
            SelfStudyHomework homework = selfStudyHomeworkLoader.loadSelfStudyHomework(result.getHomeworkId());
            if (homework == null) {
                return MapMessage.errorMessage("作业不存在");
            }

            SelfStudyHomeworkContext context = new SelfStudyHomeworkContext();
            context.setUserId(user.getId());
            context.setUser(user);
            context.setHomeworkId(result.getHomeworkId());
            context.setLearningType(studyType);
            context.setObjectiveConfigType(objectiveConfigType);
            context.setBookId(result.getBookId());
            context.setUnitId(result.getUnitId());
            context.setUnitGroupId(result.getUnitGroupId());
            context.setLessonId(result.getLessonId());
            context.setSectionId(result.getSectionId());
            context.setClientType(result.getClientType());
            context.setClientName(result.getClientName());
            context.setIpImei(StringUtils.isNotBlank(result.getIpImei()) ? result.getIpImei() : getWebRequestContext().getRealRemoteAddress());
            context.setUserAgent(getRequest().getHeader("User-Agent"));
            StudentHomeworkAnswer sha = new StudentHomeworkAnswer();
            sha.setAnswer(result.getAnswer());
            sha.setDurationMilliseconds(NewHomeworkUtils.processDuration(result.getDuration()));
            sha.setQuestionId(result.getQuestionId());
            sha.setCourseId(result.getCourseId());
            sha.setCourseGrasp(result.getCourseGrasp());
            context.setStudentHomeworkAnswer(sha);
            if (result.getHwTrajectory() != null) {
                context.putIfAbsent("hwTrajectory", JsonUtils.toJson(result.getHwTrajectory()));
            }
            MapMessage mapMessage = selfStudyHomeworkService.processorHomeworkResult(context);
            if (mapMessage.isSuccess()) {
                return MapMessage.successMessage().add("result", mapMessage.get("result"));
            } else {
                String errorCode = mapMessage.getErrorCode();
                if (StringUtils.equals(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_END, errorCode)) {
                    return MapMessage.errorMessage("该订正任务已过期").setErrorCode(errorCode);
                }
                return MapMessage.errorMessage("提交结果失败").setErrorCode(mapMessage.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error("Failed to save user {} SelfStudyHomework result", user.getId(), ex);
            return MapMessage.errorMessage("提交结果数据异常");
        }
    }

    @RequestMapping(value = "/rot/report.vpage")
    @ResponseBody
    public MapMessage selfStudyRotReport() {
        User student = getHomeworkUser();
        if (student == null || student.getId() == null) {
            return MapMessage.errorMessage("请登录");
        }
        String data = getRequestParameter("data", "");
        if (StringUtils.isBlank(data)) {
            return MapMessage.errorMessage("参数错误").setErrorCode(ERROR_CODE_PARAMETER);
        }
        SelfStudyRotReport selfStudyRotReport = JsonUtils.fromJson(data, SelfStudyRotReport.class);
        if (selfStudyRotReport == null || StringUtils.isBlank(selfStudyRotReport.getVerb()) || StringUtils.isBlank(selfStudyRotReport.getObject())) {
            return MapMessage.errorMessage("提交结果数据异常param:{}", JsonUtils.toJson(selfStudyRotReport));
        }
        selfStudyRotReport.setActor(student.getId());
        selfStudyHomeworkService.selfStudyRotReport(selfStudyRotReport);
        return MapMessage.successMessage();
    }
}
