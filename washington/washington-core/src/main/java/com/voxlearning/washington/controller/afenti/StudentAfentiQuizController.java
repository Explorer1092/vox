package com.voxlearning.washington.controller.afenti;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQuizType;
import com.voxlearning.utopia.service.afenti.api.context.QuizResultContext;
import com.voxlearning.utopia.service.afenti.api.context.TermQuizResultContext;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.net.message.exam.AfentiExamRequest;
import com.voxlearning.washington.net.message.exam.AfentiTermQuizRequest;
import com.voxlearning.washington.net.message.exam.SaveAfentiResultRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Controller
@RequestMapping("/afenti/api/quiz")
public class StudentAfentiQuizController extends StudentAfentiBaseController {

    /* **************************************** 单元测验相关 **************************************** */

    // 单元测验入口判断
    @RequestMapping(value = "fetchquizinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchQuizInfo() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        String unitId = getRequestString("unitId");

        return afentiCastleServiceClient.fetchQuizInfo(student, subject, unitId);
    }

    // 获取单元测验题目
    @RequestMapping(value = "fetchquizquestion.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchQuizQuestion() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        String unitId = getRequestString("unitId");

        return afentiCastleServiceClient.fetchQuizQuestions(student, subject, unitId);
    }

    // 获取单元测试报告
    @RequestMapping(value = "fetchquizreport.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchQuizReport() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        AfentiQuizType quizType = AfentiQuizType.safeParse(getRequestString("quizType"));
        String contentId = getRequestString("contentId");

        return afentiCastleServiceClient.fetchQuizReport(student, subject, quizType, contentId);
    }

    // 存储阿分题单元测验答题数据
    @RequestMapping(value = "processquizresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processQuizResult() {
        MapMessage mesg = currentAfentiStudentDetail();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");

        String json = getRequestString("data");
        SaveAfentiResultRequest sarr = JsonUtils.fromJson(json, SaveAfentiResultRequest.class);
        if (null == sarr) {
            logger.error("SaveAfentiResultRequest error data:{}", json);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }

        AfentiExamRequest request;
        try {
            request = AfentiExamRequest.transform(sarr);
        } catch (Exception ex) {
            logger.error("AfentiExamResultRequest error data is " + JsonUtils.toJson(sarr), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }

        return afentiCastleServiceClient.processQuizResult(generateUnitQuizResultContext(student, request));
    }

    private QuizResultContext generateUnitQuizResultContext(StudentDetail student, AfentiExamRequest request) {
        QuizResultContext context = new QuizResultContext();

        // 处理学生的做题结果
        UserAnswerMapper uam = new UserAnswerMapper(request.getQuestionId(), 1D, request.getAnswer());
        uam.setUserAgent(getRequest().getHeader("User-Agent"));
        uam.setUserId(currentUser().getId());
        uam.setHomeworkType(StudyType.afenti.name());
        QuestionScoreResult qsr = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
        List<List<String>> answer = new ArrayList<>();
        List<List<Boolean>> subMaster = new ArrayList<>();
        for (SubScoreResult ssr : qsr.getSubScoreResults()) {
            answer.add(ssr.getStandardAnswer());
            subMaster.add(ssr.getIsRight());
        }

        context.getResult().put("questionId", request.getQuestionId());
        context.getResult().put("questionInfo", MiscUtils.m("answers", answer,
                "userAnswers", request.getAnswer(), "subMaster", subMaster, "master", qsr.getIsRight()));
        context.setStudent(student);
        context.setSubject(Subject.of(request.getSubject()));
        context.setMaster(qsr.getIsRight());
        context.setBookId(request.getBookId());
        context.setUnitId(request.getUnitId());
        context.setQuestionId(request.getQuestionId());
        context.setFinished(request.getFinished());
        return context;
    }

    /* **************************************** 期中期末测验相关 **************************************** */

    // 期中期末测验入口判断
    @RequestMapping(value = "fetchtermquizinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchTermQuizInfo() {
        MapMessage mesg = currentAfentiStudentDetail();
        if (!mesg.isSuccess()) return mesg;
        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        return afentiCastleServiceClient.fetchTermQuizInfo(student);
    }

    // 获取期中或者期末测验题目
    @RequestMapping(value = "fetchtermquizquestion.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchTermQuizQuestion() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");

        return afentiCastleServiceClient.fetchTermQuizQuestions(student, subject, "TQ_201612");
    }

    // 获取期中或者期末测试报告
    @RequestMapping(value = "fetchtermquizreport.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchTermQuizReport() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");

        return afentiCastleServiceClient.fetchTermQuizReport(student, subject);
    }

    // 存储阿分题单元测验答题数据
    @RequestMapping(value = "processtermquizresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processTermQuizResult() {
        MapMessage mesg = currentAfentiStudentDetail();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");

        String json = getRequestString("data");
        AfentiTermQuizRequest request = JsonUtils.fromJson(json, AfentiTermQuizRequest.class);
        if (null == request) {
            logger.error("AfentiTermQuizRequest error data:{}", json);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }

        return afentiCastleServiceClient.processTermQuizResult(generateTermQuizResultContext(student, request));
    }

    private TermQuizResultContext generateTermQuizResultContext(StudentDetail student, AfentiTermQuizRequest request) {
        TermQuizResultContext context = new TermQuizResultContext();

        // 处理学生的做题结果
        UserAnswerMapper uam = new UserAnswerMapper(request.getQuestionId(), 1D, request.getAnswer());
        uam.setUserAgent(getRequest().getHeader("User-Agent"));
        uam.setUserId(currentUser().getId());
        uam.setHomeworkType(StudyType.afenti.name());
        QuestionScoreResult qsr = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
        List<List<String>> answer = new ArrayList<>();
        List<List<Boolean>> subMaster = new ArrayList<>();
        for (SubScoreResult ssr : qsr.getSubScoreResults()) {
            answer.add(ssr.getStandardAnswer());
            subMaster.add(ssr.getIsRight());
        }

        context.getResult().put(request.getQuestionId(), MiscUtils.m("answers", answer,
                "userAnswers", request.getAnswer(), "subMaster", subMaster, "master", qsr.getIsRight()));

        context.setStudent(student);
        context.setSubject(Subject.of(request.getSubject()));
        context.setMaster(qsr.getIsRight());
        context.setBookId(request.getBookId());
        context.setUnitId(request.getUnitId());
        context.setQuestionId(request.getQuestionId());
        context.setFinished(request.getFinished());
        return context;
    }
}
