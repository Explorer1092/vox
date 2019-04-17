/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.afenti;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.core.SyslogLevel;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.afenti.api.AfentiSocialService;
import com.voxlearning.utopia.service.afenti.api.AfentiWrongQuestionSource;
import com.voxlearning.utopia.service.afenti.api.AfentiWrongQuestionStateType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiPromptType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.client.AsyncAfentiCacheServiceClient;
import com.voxlearning.utopia.service.afenti.consumer.AfentiServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkQueueServiceClient;
import com.voxlearning.utopia.service.order.api.loader.UserOrderLoader;
import com.voxlearning.utopia.service.question.api.constant.ChineseQuestionType;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.washington.net.message.exam.AfentiExamRequest;
import com.voxlearning.washington.net.message.exam.SaveAfentiResultRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;
import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.AVAILABLE_SUBJECT;
import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.SUBJECT_AFENTI_VIDEO_REF;

/**
 * @author Ruib
 * @since 2016/8/4
 */
@Controller
@RequestMapping("/afenti/api")
public class StudentAfentiController extends StudentAfentiBaseController {

    @Inject
    private AsyncAfentiCacheServiceClient asyncAfentiCacheServiceClient;
    @Inject
    private AfentiServiceClient afentiServiceClient;
    @Inject
    private NewHomeworkQueueServiceClient newHomeworkQueueServiceClient;
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private GrayFunctionManagerClient grayFunctionManagerClient;

    @ImportService(interfaceClass = AfentiSocialService.class)
    private AfentiSocialService afentiSocialService;
    @ImportService(interfaceClass = UserOrderLoader.class)
    private UserOrderLoader userOrderLoader;

    // 登陆页
    @RequestMapping(value = "login.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage login() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");

        // log pv
        OrderProductServiceType type = getOrderProductServiceType(subject);
        LogCollector.getInstance().collect(SyslogLevel.info, "a17zy_app_pv_logs",
                MiscUtils.map(
                        "app_key", type.name(),
                        "user_id", currentUserId(),
                        "platform", isMobileRequest(getRequest()) ? "app" : "pc",
                        "env", RuntimeMode.getCurrentStage(),
                        "subject", subject.name(),
                        "client_ip", getWebRequestContext().getRealRemoteAddress()
                ));

        return afentiCastleServiceClient.login(student, subject);
    }

    //校验教材
    @RequestMapping(value = "validatebook.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage validateBook() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        String learningType = getRequestParameter("learningType", "castle");
        AfentiLearningType type = AfentiLearningType.safeParse(learningType);

        boolean matched = true;
        if (subject != Subject.MATH) { //目前只验证数学
            return MapMessage.successMessage().add("matched", matched);
        }

        AfentiBook book = afentiServiceClient.fetchAfentiBook(student.getId(), subject, type);
        if (book != null && book.book != null) {
            boolean isInGrayArea = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(student, "AfentiMath", "LimitSameGrade");
            Integer bookClazzLevel = book.book.getClazzLevel();
            if (isInGrayArea && bookClazzLevel != null && student.getClazzLevel() != null
                    && student.getClazzLevel().getLevel() > bookClazzLevel) {
                matched = false;
            }
        }
        return MapMessage.successMessage().add("matched", matched);
    }

    // 根据年级获取教材
    @RequestMapping(value = "fetchgradebooklist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchGradeBookList() {
        ClazzLevel clazzLevel = ClazzLevel.parse(getRequestInt("clazzLevel"));

        Subject subject = Subject.of(getRequestString("subject"));
        // 学前默认取一年级教材
        if (clazzLevel.getLevel() >= 51 && clazzLevel.getLevel() <= 54) {
            clazzLevel = ClazzLevel.FIRST_GRADE;
        }
        String learningType = getRequestParameter("learningType", "castle");
        AfentiLearningType type = AfentiLearningType.safeParse(learningType);
        List<NewBookProfile> books = afentiCastleServiceClient.fetchGradeBookList(clazzLevel, subject, type);
        List<Map<String, Object>> bookMaps = newBookPaintedSkin(books); // 画皮
        return MapMessage.successMessage().add("bookList", bookMaps);
    }

    // 更换教材
    @RequestMapping(value = "activebook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage activeBook() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        String bookId = getRequestString("bookId");
        String learningType = getRequestParameter("learningType", "castle");
        AfentiLearningType type = AfentiLearningType.safeParse(learningType);

        return afentiCastleServiceClient.activeBook(student.getId(), bookId, subject, type);
    }

    // 更换教材历史
    @RequestMapping(value = "changebookhistory.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage changeBookHistory() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        String learningType = getRequestParameter("learningType", "castle");
        AfentiLearningType type = AfentiLearningType.safeParse(learningType);
        List<NewBookProfile> books = afentiCastleServiceClient.fetchChangeBookHistory(student.getId(), subject, type);
        List<Map<String, Object>> bookMaps = newBookPaintedSkin(books); // 画皮
        return MapMessage.successMessage().add("bookList", bookMaps);
    }

    // 学习城堡页 -- 获取单元数据
    @RequestMapping(value = "units.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchBookUnits() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        String learningType = getRequestParameter("learningType", "castle");
        AfentiLearningType type = AfentiLearningType.safeParse(learningType);
        return afentiCastleServiceClient.fetchBookUnits(student, subject, type);
    }

    // 关卡页 -- 根据单元获取关卡数据
    @RequestMapping(value = "ranks.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchUnitRanks() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        String unitId = getRequestString("unitId");
        String learningType = getRequestParameter("learningType", "castle");
        AfentiLearningType type = AfentiLearningType.safeParse(learningType);
        return afentiCastleServiceClient.fetchUnitRanks(student, unitId, subject, type);
    }

    // 进入关卡 -- 获取关卡题目
    @RequestMapping(value = "questions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchRankQuestions() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        String unitId = getRequestString("unitId");
        int rank = getRequestInt("rank");
        String learningType = getRequestParameter("learningType", "castle");
        AfentiLearningType type = AfentiLearningType.safeParse(learningType);
        return afentiCastleServiceClient.fetchQuestions(student, unitId, rank, subject, type);
    }

    // 存储阿分题学习城堡答题数据
    @RequestMapping(value = "processcastleresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processCastleResult() {
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

        CastleResultContext context;
        try {
            context = generateCastleResultContextAndPersistToBigData(student, request);
        } catch (Exception ex) {
            logger.error("Persist afenti castle result to big data error", ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }

        if (context == null) {
            return MapMessage.errorMessage(AfentiErrorType.DATA_ERROR.getInfo()).setErrorCode(AfentiErrorType.DATA_ERROR.getCode());
        }

        return afentiCastleServiceClient.processCastleResult(context);

    }

    private CastleResultContext generateCastleResultContextAndPersistToBigData(StudentDetail student, AfentiExamRequest request) {
        CastleResultContext context = new CastleResultContext();

        // 处理学生的做题结果
        UserAnswerMapper uam = new UserAnswerMapper(request.getQuestionId(), 1D, request.getAnswer());
        uam.setUserAgent(getRequest().getHeader("User-Agent"));
        uam.setUserId(currentUser().getId());
        uam.setHomeworkType(StudyType.afenti.name());
        QuestionScoreResult qsr = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
        if (qsr == null || qsr.getSubScoreResults() == null) {
            logger.error("Afenti GenerateCastleResultContext Error,userId={},questionID={},userAnswer={}", student.getId(), request.getQuestionId(), request.getAnswer());
            return null;
        }

        List<List<String>> answer = new ArrayList<>();
        List<List<Boolean>> subMaster = new ArrayList<>();
        for (SubScoreResult ssr : qsr.getSubScoreResults()) {
            answer.add(ssr.getStandardAnswer());
            subMaster.add(ssr.getIsRight());
        }

        // 上报数据
        JournalNewHomeworkProcessResult result = new JournalNewHomeworkProcessResult();
        Subject subject = Subject.of(request.getSubject());
        result.setCreateAt(new Date()); // ser.setCmt_time(new Date());
        result.setUpdateAt(new Date());
        result.setStudyType(StudyType.afenti); // ser.setLearning_type(StudyType.afenti);
        result.setClazzId(student.getClazz().getId()); // ser.setClass_id(student.getClazzId());
        GroupMapper group = deprecatedGroupLoaderClient.loadStudentGroups(student.getId(), false).stream()
                .filter(g -> g.getSubject() == subject).findFirst().orElse(null);
        result.setClazzGroupId(group == null ? 0L : group.getId()); // ser.setGroup_id(0L);
        result.setClazzLevel(student.getClazzLevelAsInteger()); // ser.setClass_level(student.getClazzLevelAsInteger());
        result.setBookId(request.getBookId()); // ser.setBook_id(request.getBookId());
        result.setUnitId(request.getUnitId()); // ser.setUnit_id(request.getUnitId());
        result.setUserId(student.getId()); // ser.setUid(student.getId());
        result.setQuestionId(request.getQuestionId()); // ser.setEid(request.getQuestionId());
        result.setGrasp(qsr.getIsRight()); // ser.setAtag(qsr.getIsRight());
        result.setSubGrasp(subMaster); // ser.setSatag(subMaster);
        result.setDuration(request.getFinishTime()); // ser.setCmt_timelen(request.getFinishTime());
        result.setSubject(subject); // ser.setSubject(Subject.of(request.getSubject()));
        result.setUserAnswers(request.getAnswer()); // ser.setAnswers(request.getAnswer());
        result.setClientType(request.getClientType()); // ser.setClient_type(request.getClientType());
        result.setClientName(request.getClientType()); // ser.setClient_name(request.getClientType());
        result.setAlgoW("");
        result.setAlgoV("");
        if (StringUtils.isNotBlank(request.getScoreCoefficient())) {
            Map map = JsonUtils.fromJson(StringUtils.replace(request.getScoreCoefficient(), "'", "\""), Map.class);
            result.setAlgoW(map == null ? "" : SafeConverter.toString(map.get("algo_w")));
            result.setAlgoV(map == null ? "" : SafeConverter.toString(map.get("algo_v")));
        }

        if (subject == Subject.CHINESE) {
            Map<String, ChineseQuestionType> typeMap = questionLoaderClient.getChineseQuestionType(Collections.singletonList(request.getQuestionId()));
            ChineseQuestionType type = typeMap != null ? typeMap.getOrDefault(request.getQuestionId(), ChineseQuestionType.unknown) : ChineseQuestionType.unknown;
            result.setQuestionPattern(type.name());
        }

        result.setClientId(currentUserId().toString());
        newHomeworkQueueServiceClient.saveJournalNewHomeworkProcessResults(Collections.singletonList(result));
        // newHomeworkQueueServiceClient.saveJournalNewHomeworkProcessResultToKafka(Collections.singletonList(result));

        // 记录做过题
        asyncAfentiCacheServiceClient.getAsyncAfentiCacheService()
                .AfentiLastWeekUsedCacheManager_record(student.getId(), Subject.of(request.getSubject()))
                .awaitUninterruptibly();

        context.getResult().put("questionId", request.getQuestionId());
        context.getResult().put("questionInfo", MiscUtils.m("answers", answer,
                "userAnswers", request.getAnswer(), "subMaster", subMaster, "master", qsr.getIsRight()));
        context.setStudent(student);
        context.setSubject(Subject.of(request.getSubject()));
        context.setBookId(request.getBookId());
        context.setUnitId(request.getUnitId());
        context.setRank(request.getRank());
        context.setQuestionId(request.getQuestionId());
        context.setMaster(qsr.getIsRight());
        context.setLearningType(request.getLearningType() == AfentiLearningType.preparation ? AfentiType.假期预习 : AfentiType.学习城堡);
        context.setFinished(request.getFinished());
        context.setAfentiLearningType(request.getLearningType() == null ? AfentiLearningType.castle : request.getLearningType());
        return context;
    }

    // 复习页面关卡页 -- 根据学科获取单元关卡数据
    @RequestMapping(value = "review/ranks.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchReviewRanks() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;
        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        return afentiCastleServiceClient.fetchReviewRanks(student, subject);
    }

    // 进入复习冲刺关卡 -- 获取关卡题目
    @RequestMapping(value = "review/questions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchReviewRankQuestions() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        String unitId = getRequestString("unitId");

        return afentiCastleServiceClient.fetchReviewQuestions(student, unitId, subject);
    }

    // 存储阿分题复习冲刺答题数据
    @RequestMapping(value = "review/processresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processReveiewResult() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        String json = getRequestString("data");
        SaveAfentiResultRequest sarr = JsonUtils.fromJson(json, SaveAfentiResultRequest.class);
        if (null == sarr) {
            logger.error("processReveiewResult SaveAfentiResultRequest error data:{}", json);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }

        AfentiExamRequest request;
        try {
            request = AfentiExamRequest.transform(sarr);
        } catch (Exception ex) {
            logger.error("processReveiewResult AfentiExamResultRequest error data is " + JsonUtils.toJson(sarr), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }

        ReviewResultContext context;
        try {
            context = generateReviewResultContextAndPersistToBigData(student, request);
        } catch (Exception ex) {
            logger.error("Persist afenti review result to big data error", ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }

        if (context == null) {
            return MapMessage.errorMessage(AfentiErrorType.DATA_ERROR.getInfo()).setErrorCode(AfentiErrorType.DATA_ERROR.getCode());
        }

        return afentiCastleServiceClient.processReviewResult(context);
    }

    private ReviewResultContext generateReviewResultContextAndPersistToBigData(StudentDetail student, AfentiExamRequest request) {
        ReviewResultContext context = new ReviewResultContext();

        // 处理学生的做题结果
        UserAnswerMapper uam = new UserAnswerMapper(request.getQuestionId(), 1D, request.getAnswer());
        uam.setUserAgent(getRequest().getHeader("User-Agent"));
        uam.setUserId(currentUser().getId());
        uam.setHomeworkType(StudyType.afenti.name());
        QuestionScoreResult qsr = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
        if (qsr == null || qsr.getSubScoreResults() == null) {
            logger.error("Afenti GenerateReviewResultContext Error,userId={},questionID={},userAnswer={}", student.getId(), request.getQuestionId(), request.getAnswer());
            return null;
        }

        List<List<String>> answer = new ArrayList<>();
        List<List<Boolean>> subMaster = new ArrayList<>();
        for (SubScoreResult ssr : qsr.getSubScoreResults()) {
            answer.add(ssr.getStandardAnswer());
            subMaster.add(ssr.getIsRight());
        }

        // 上报数据
        JournalNewHomeworkProcessResult result = new JournalNewHomeworkProcessResult();
        Subject subject = Subject.of(request.getSubject());
        result.setCreateAt(new Date()); // ser.setCmt_time(new Date());
        result.setUpdateAt(new Date());
        result.setStudyType(StudyType.afenti); // ser.setLearning_type(StudyType.afenti);
        result.setClazzId(student.getClazz().getId()); // ser.setClass_id(student.getClazzId());
        GroupMapper group = deprecatedGroupLoaderClient.loadStudentGroups(student.getId(), false).stream()
                .filter(g -> g.getSubject() == subject).findFirst().orElse(null);
        result.setClazzGroupId(group == null ? 0L : group.getId()); // ser.setGroup_id(0L);
        result.setClazzLevel(student.getClazzLevelAsInteger()); // ser.setClass_level(student.getClazzLevelAsInteger());
        result.setBookId(request.getBookId()); // ser.setBook_id(request.getBookId());
        result.setUnitId(request.getUnitId()); // ser.setUnit_id(request.getUnitId());
        result.setUserId(student.getId()); // ser.setUid(student.getId());
        result.setQuestionId(request.getQuestionId()); // ser.setEid(request.getQuestionId());
        result.setGrasp(qsr.getIsRight()); // ser.setAtag(qsr.getIsRight());
        result.setSubGrasp(subMaster); // ser.setSatag(subMaster);
        result.setDuration(request.getFinishTime()); // ser.setCmt_timelen(request.getFinishTime());
        result.setSubject(subject); // ser.setSubject(Subject.of(request.getSubject()));
        result.setUserAnswers(request.getAnswer()); // ser.setAnswers(request.getAnswer());
        result.setClientType(request.getClientType()); // ser.setClient_type(request.getClientType());
        result.setClientName(request.getClientType()); // ser.setClient_name(request.getClientType());
        result.setAlgoW("");
        result.setAlgoV("");
        if (StringUtils.isNotBlank(request.getScoreCoefficient())) {
            Map map = JsonUtils.fromJson(StringUtils.replace(request.getScoreCoefficient(), "'", "\""), Map.class);
            result.setAlgoW(map == null ? "" : SafeConverter.toString(map.get("algo_w")));
            result.setAlgoV(map == null ? "" : SafeConverter.toString(map.get("algo_v")));
        }

        Map<String, String> map = new HashMap<>();
        map.put("flag", AfentiLearningType.review.name());
        result.setAdditions(map);

        result.setClientId(currentUserId().toString());
        newHomeworkQueueServiceClient.saveJournalNewHomeworkProcessResults(Collections.singletonList(result));

        // 记录做过题
        asyncAfentiCacheServiceClient.getAsyncAfentiCacheService()
                .AfentiLastWeekUsedCacheManager_record(student.getId(), Subject.of(request.getSubject()))
                .awaitUninterruptibly();

        context.getResult().put("questionId", request.getQuestionId());
        context.getResult().put("questionInfo", MiscUtils.m("answers", answer,
                "userAnswers", request.getAnswer(), "subMaster", subMaster, "master", qsr.getIsRight()));
        context.setStudent(student);
        context.setSubject(Subject.of(request.getSubject()));
        context.setBookId(request.getBookId());
        context.setUnitId(request.getUnitId());
        context.setQuestionId(request.getQuestionId());
        context.setMaster(qsr.getIsRight());
        context.setFinished(request.getFinished());
        return context;
    }

    //期末复习页面关卡页 -- 关卡家长奖励
    @RequestMapping(value = "review/parentreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reviewParentreward() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;
        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        String unitId = getRequestString("unitId");
        return afentiCastleServiceClient.generateReviewFamilyReward(student, unitId, subject);
    }

    //期末复习页面排名页 -- 做题排名
    @RequestMapping(value = "review/ranking.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage reviewRanking() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;
        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        return afentiCastleServiceClient.fetchReviewRanking(student, subject);
    }

    //期末复习页面排名页 -- 家庭参与值排名
    @RequestMapping(value = "review/homeRanking.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage reviewHomeRanking() {
        MapMessage mesg = currentAfentiStudentDetail();
        if (!mesg.isSuccess()) return mesg;
        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        return afentiCastleServiceClient.fetchReviewHomeRanking(student);
    }

    // 预习页--获取数学攻略视频
    @RequestMapping(value = "prepration/video.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchPreprationVideo() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        if (subject != Subject.MATH) {
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
        return afentiCastleServiceClient.fetchPeparationVideo(student, subject);
    }

    // 获取错题精灵首页
    @RequestMapping(value = "elfIndex.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchElfIndex() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        return afentiElfServiceClient.fetchIndexElf(student.getId(), subject);
    }

    // 获取错题精灵
    @RequestMapping(value = "elf.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchElf() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        String typeString = getRequestParameter("currentType", "incorrect");
        AfentiWrongQuestionStateType type = AfentiWrongQuestionStateType.safeParse(typeString);
        return afentiElfServiceClient.fetchElf(student.getId(), subject, type);
    }

    // 获取错题精灵首页v2
    @RequestMapping(value = "v2/elfIndex.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchV2ElfIndex() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        return afentiElfServiceClient.fetchIndexElfV2(student.getId(), subject);
    }

    //分页获取错题精灵
    @RequestMapping(value = "elfPage.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchElfPage() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        String typeString = getRequestParameter("currentType", "incorrect");
        AfentiWrongQuestionStateType type = AfentiWrongQuestionStateType.safeParse(typeString);

        String sourceString = getRequestParameter("source", "castle");
        AfentiWrongQuestionSource source = AfentiWrongQuestionSource.safeParse(sourceString);

        int pageNum = getRequestInt("pageNum", 1);
        ;
        int pageSize = getRequestInt("pageSize", 20);

        return afentiElfServiceClient.fetchElfPage(student.getId(), subject, type, source, pageNum, pageSize);
    }

    // 存储阿分题错题精灵答题数据
    @RequestMapping(value = "processelfresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processElfResult() {
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
        ElfResultContext context = generateElfResultContextAndPersistToBigData(student, request);
        if (context == null) {
            return MapMessage.errorMessage(AfentiErrorType.DATA_ERROR.getInfo()).setErrorCode(AfentiErrorType.DATA_ERROR.getCode());
        }
        return afentiElfServiceClient.processElfResult(context);
    }

    // 错题宝首页
    @RequestMapping(value = "/video/index.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage videoIndex() {
        Subject subject = Subject.ofWithUnknown(getRequestString("subject"));
        if (!AVAILABLE_SUBJECT.contains(subject)) return MapMessage.errorMessage();

        StudentDetail student = currentStudentDetail();
        Clazz clazz = student.getClazz();
        if (null == clazz || (!clazz.isPrimaryClazz() && !clazz.isInfantClazz())) return MapMessage.errorMessage();

        // 获取相应学科产品是否在有效期内
        Date current = new Date();
        OrderProductServiceType type = SUBJECT_AFENTI_VIDEO_REF.get(subject);
        Map<String, Object> pi = userOrderLoader.fetchLatestAfentiVideoPurchaseInfo(student.getId(),
                Collections.singleton(type.name())).getOrDefault(type.name(), new HashMap<>());
        if (MapUtils.isEmpty(pi)) return MapMessage.errorMessage();

        int gradeNum = SafeConverter.toInt(pi.get("grade"));
        int termNum = SafeConverter.toInt(pi.get("term"));

        MapMessage mesg = afentiServiceClient.getAfentiService().wrongQuestionPlusIndex(student.getId(), subject,
                gradeNum, termNum, "COMMON");
        if (!mesg.isSuccess()) return mesg;

        mesg.add("productId", SafeConverter.toString(pi.get("productId")));
        mesg.add("subject", subject);

        long endTimestamp = SafeConverter.toLong(pi.get("endTimestamp"));
        if (current.getTime() < endTimestamp) {
            mesg.add("hasOpened", true);
            mesg.add("validityDate", (endTimestamp - current.getTime()) / (24 * 60 * 60 * 1000));
        } else {
            mesg.add("hasOpened", false);
            mesg.add("validityDate", 0);
        }

        // 黑名单
        mesg.add("forbidden", userBlacklistServiceClient.isInBlackListByStudent(Collections.singletonList(student))
                .getOrDefault(student.getId(), false));

        return mesg;
    }

    // 视频道具:视频课程列表
    @RequestMapping(value = "/video/list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage videoList() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");

        // 获取相应学科产品是否在有效期内
        Date current = new Date();
        OrderProductServiceType type = SUBJECT_AFENTI_VIDEO_REF.get(subject);
        Map<String, Object> pi = userOrderLoader.fetchLatestAfentiVideoPurchaseInfo(student.getId(),
                Collections.singleton(type.name())).getOrDefault(type.name(), new HashMap<>());
        if (MapUtils.isEmpty(pi)) return MapMessage.errorMessage();

        int gradeNum = SafeConverter.toInt(pi.get("grade"));
        int termNum = SafeConverter.toInt(pi.get("term"));

        MapMessage result = afentiServiceClient.getAfentiService().getCurrentCourseVideoList(student.getId(), subject, gradeNum, termNum, "COMMON");
        if (!result.isSuccess())
            return result;

        long endTimestamp = SafeConverter.toLong(pi.get("endTimestamp"));
        String productName = SafeConverter.toString(pi.get("productName"));
        if (current.getTime() < endTimestamp) {
            result.add("hasOpened", true);
            result.add("validityDate", (endTimestamp - current.getTime()) / (24 * 60 * 60 * 1000));
        } else {
            result.add("hasOpened", false);
            result.add("validityDate", 0);
        }

        // 将配好的产品名称更新到原视频名称
        MapMessage courseMess = (MapMessage) result.get("courseInfo");
        if (courseMess != null) {
            courseMess.set("name", productName);
            result.set("courseInfo", courseMess);
        }

        return result;
    }

    // 视频道具:视频课程详细
    @RequestMapping(value = "/video/detail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage videoDetail() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");

        // 学前学生年级修改为一年级
        ClazzLevel clazzLevel = student.isInfantStudent() ? ClazzLevel.FIRST_GRADE : student.getClazzLevel();

        Subject subject = (Subject) mesg.get("subject");
        String lessonId = getRequestString("lessonId");
        Term term = SchoolYear.newInstance().currentTerm();

        if (StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("缺少课时ID");

        Boolean openedVideoCourse = false;

        // 获取相应学科产品是否在有效期内
        Date current = new Date();
        OrderProductServiceType type = SUBJECT_AFENTI_VIDEO_REF.get(subject);
        Map<String, Object> pi = userOrderLoader.fetchLatestAfentiVideoPurchaseInfo(student.getId(),
                Collections.singleton(type.name())).getOrDefault(type.name(), new HashMap<>());
        if (MapUtils.isEmpty(pi)) return MapMessage.errorMessage();

        long endTimestamp = SafeConverter.toLong(pi.get("endTimestamp"));
        if (current.getTime() < endTimestamp)
            openedVideoCourse = true;

        MapMessage result = afentiServiceClient.getAfentiService().getCurrentLessonVideoDetail(lessonId, openedVideoCourse);
        if (result.isSuccess()) {
            result.add("hasOpened", openedVideoCourse);
            result.add("subject", subject);
            result.add("clazzLevel", clazzLevel.getLevel());
            result.add("term", term.getKey());
        }
        return result;
    }

    // 视频道具:视频课程详细
    @RequestMapping(value = "/video/addplayrecord.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage videoPlayRecord() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        String lessonId = getRequestString("lessonId");

        // 获取相应学科产品是否在有效期内
        Date current = new Date();
        OrderProductServiceType type = SUBJECT_AFENTI_VIDEO_REF.get(subject);
        Map<String, Object> pi = userOrderLoader.fetchLatestAfentiVideoPurchaseInfo(student.getId(),
                Collections.singleton(type.name())).getOrDefault(type.name(), new HashMap<>());
        if (MapUtils.isEmpty(pi)) return MapMessage.errorMessage();

        long endTimestamp = SafeConverter.toLong(pi.get("endTimestamp"));

        if (!afentiServiceClient.getAfentiService().isAddedVideoViewRecord(student.getId(), lessonId)) {
            // 加入新的观看记录
            afentiServiceClient.getAfentiService().addVideoViewRecord(student.getId(), lessonId);
        }

        return MapMessage.successMessage();
    }

    private ElfResultContext generateElfResultContextAndPersistToBigData(StudentDetail student, AfentiExamRequest request) {
        ElfResultContext context = new ElfResultContext();

        // 处理学生的做题结果
        UserAnswerMapper uam = new UserAnswerMapper(request.getQuestionId(), 1D, request.getAnswer());
        uam.setUserAgent(getRequest().getHeader("User-Agent"));
        uam.setUserId(currentUser().getId());
        uam.setHomeworkType(StudyType.afenti.name());
        QuestionScoreResult qsr = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
        List<List<String>> answer = new ArrayList<>();
        List<List<Boolean>> subMaster = new ArrayList<>();

        //题目错误导致偶发空指针错误，打出对应题目id
        if (qsr == null || qsr.getSubScoreResults() == null) {
            logger.error("Afenti GenerateElfResultContext Error,userId={},questionId={}, userAnswer={}", student.getId(), request.getQuestionId(), request.getAnswer());
            return null;
        }
        for (SubScoreResult ssr : qsr.getSubScoreResults()) {
            answer.add(ssr.getStandardAnswer());
            subMaster.add(ssr.getIsRight());
        }

        // 上报数据
        JournalNewHomeworkProcessResult result = new JournalNewHomeworkProcessResult();
        Subject subject = Subject.of(request.getSubject());
        result.setCreateAt(new Date()); // ser.setCmt_time(new Date());
        result.setUpdateAt(new Date());
        result.setStudyType(StudyType.afenti); // ser.setLearning_type(StudyType.afenti);
        result.setClazzId(student.getClazz().getId()); // ser.setClass_id(student.getClazzId());
        GroupMapper group = deprecatedGroupLoaderClient.loadStudentGroups(student.getId(), false).stream()
                .filter(g -> g.getSubject() == subject).findFirst().orElse(null);
        result.setClazzGroupId(group == null ? 0L : group.getId()); // ser.setGroup_id(0L);
        result.setClazzLevel(student.getClazzLevelAsInteger()); // ser.setClass_level(student.getClazzLevelAsInteger());
        result.setUserId(student.getId()); // ser.setUid(student.getId());
        result.setQuestionId(request.getQuestionId()); // ser.setEid(request.getQuestionId());
        result.setGrasp(qsr.getIsRight()); // ser.setAtag(qsr.getIsRight());
        result.setSubGrasp(subMaster); // ser.setSatag(subMaster);
        result.setDuration(request.getFinishTime()); // ser.setCmt_timelen(request.getFinishTime());
        result.setSubject(subject); // ser.setSubject(Subject.of(request.getSubject()));
        result.setUserAnswers(request.getAnswer()); // ser.setAnswers(request.getAnswer());
        result.setClientType(request.getClientType()); // ser.setClient_type(request.getClientType());
        result.setClientName(request.getClientType()); // ser.setClient_name(request.getClientType());
        result.setClientId(currentUserId().toString());
        newHomeworkQueueServiceClient.saveJournalNewHomeworkProcessResults(Collections.singletonList(result));
        // newHomeworkQueueServiceClient.saveJournalNewHomeworkProcessResultToKafka(Collections.singletonList(result));

        // 记录做过题
        asyncAfentiCacheServiceClient.getAsyncAfentiCacheService()
                .AfentiLastWeekUsedCacheManager_record(student.getId(), Subject.of(request.getSubject()))
                .awaitUninterruptibly();
        context.getResult().put("questionId", request.getQuestionId());
        context.getResult().put("questionInfo", MiscUtils.m("answers", answer,
                "userAnswers", request.getAnswer(), "subMaster", subMaster, "master", qsr.getIsRight()));
        context.setStudent(student);
        context.setSubject(Subject.of(request.getSubject()));
        context.setQuestionId(request.getQuestionId());
        context.setMaster(qsr.getIsRight());
        context.setAfentiState(request.getAfentiState());
        context.setOriginalQuestionId(request.getOriginalQuestionId());
        return context;
    }

    // 将某个引导流程置为完成
    @RequestMapping(value = "completeguide.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage completeiGuide() {
        MapMessage mesg = currentAfentiStudentDetail();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        String name = getRequest().getParameter("guideName");
        return afentiServiceClient.completeGuide(student.getId(), name);
    }

    // 阿分题内弹窗，只有一个哦~
    @RequestMapping(value = "popup.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage popup() {
        Subject subject = Subject.of(getRequestString("subject"));
        String popup;
        switch (subject) {
            case ENGLISH: {
                popup = StringUtils.defaultString(getPageBlockContentGenerator()
                        .getPageBlockContentHtml("AfentiEnglishIndex", "AfentiEnglishPopup")
                        .replace("\r", "").replace("\n", "").replace("\t", ""));
                break;
            }
            case MATH: {
                popup = StringUtils.defaultString(getPageBlockContentGenerator()
                        .getPageBlockContentHtml("AfentiMathIndex", "AfentiMathPopup")
                        .replace("\r", "").replace("\n", "").replace("\t", ""));
                break;
            }
            default:
                popup = "";
        }
        return StringUtils.isEmpty(popup) ? MapMessage.errorMessage() : MapMessage.successMessage().add("popup", popup);
    }

    // 阿分题灰度
    @RequestMapping(value = "gray.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage gray() {
        MapMessage mesg = currentAfentiStudentDetail();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        String mainFunctionName = getRequestString("mainFunctionName");
        String subFunctionName = getRequestString("subFunctionName");
        boolean hit = getGrayFunctionManagerClient().getStudentGrayFunctionManager()
                .isWebGrayFunctionAvailable(student, mainFunctionName, subFunctionName);
        return MapMessage.successMessage().add("hit", hit);
    }

    // 单独获取学豆数量、星星数量、提示小红点
    @RequestMapping(value = "brief.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage brief() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");

        if (!asyncAfentiCacheServiceClient.getAsyncAfentiCacheService()
                .AfentiLoginCacheManager_notified(student.getId(), subject)
                .take()) {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("TS", System.currentTimeMillis());
            message.put("U", student.getId());
            message.put("S", subject);
            afentiServiceClient.getAfentiService().sendLoginMessage(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        }

        // 学豆数量
        UserIntegral ui = student.getUserIntegral();
        long integral = ui == null ? 0L : ui.getUsable();
        // 星星数量
        int star = afentiLoaderClient.loadUserTotalStar(student.getId(), subject);
        // 小红点提示
        Map<AfentiPromptType, Boolean> map = asyncAfentiCacheServiceClient.getAsyncAfentiCacheService()
                .AfentiPromptCacheManager_fetch(student.getId(), subject)
                .take();

        MapMessage mapMessage = afentiSocialService.fetchPopupMessage(student, subject);
        MapMessage resultMapMessage = MapMessage.successMessage().add("integral", integral).add("star", star).add("prompt", map);
        if (mapMessage.isSuccess()) {
            resultMapMessage.putAll(mapMessage);
        }
        return resultMapMessage;
    }
}
