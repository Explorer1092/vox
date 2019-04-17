package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportPersonalRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.NewQuestionReportBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.pc.QuestionReportDetail;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class ProcessNewHomeworkAnswerDetailOralPracticeTemplate extends ProcessNewHomeworkAnswerDetailCommonTemplate {

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.ORAL_PRACTICE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void processNewHomeworkAnswerDetail(ReportRateContext reportRateContext) {
        List<String> oralPracticeTypeQuestionIds = reportRateContext.getNewHomework().findQuestionIds(getObjectiveConfigType(), false);

        Map<String, NewHomeworkResult> newHomeworkResultMapToObjectiveConfigType = handlerNewHomeworkResultMap(reportRateContext.getNewHomeworkResultMap(), getObjectiveConfigType());
        Map<String, Map<String, Object>> oralPracticeInformation = new LinkedHashMap<>();
        for (NewHomeworkResult nr : newHomeworkResultMapToObjectiveConfigType.values()) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = nr.getPractices().get(getObjectiveConfigType());
            if (MapUtils.isNotEmpty(newHomeworkResultAnswer.getAnswers())) {
                LinkedHashMap<String, String> answers = newHomeworkResultAnswer.getAnswers();
                for (String qId : answers.keySet()) {
                    //口语练习每题的信息统计
                    NewQuestion question = reportRateContext.getAllNewQuestionMap().get(qId);
                    String processId = answers.get(qId);
                    NewHomeworkProcessResult pr = reportRateContext.getNewHomeworkProcessResultMap().get(processId);
                    if (question == null || pr == null) {
                        continue;
                    }
                    Map<String, Object> questionStatisticInformation;//每一题记载答题的信息
                    List<Map<String, Object>> answerList; //对应一个答题下面每小题的答题信息
                    List<List<NewHomeworkProcessResult.OralDetail>> oralDetails;//每一题的口语回答信息

                    if (oralPracticeInformation.containsKey(qId)) {
                        questionStatisticInformation = oralPracticeInformation.get(qId);
                        questionStatisticInformation.put("size", SafeConverter.toInt(questionStatisticInformation.get("size")) + 1);
                        questionStatisticInformation.put("totalScore", SafeConverter.toDouble(questionStatisticInformation.get("totalScore")) + pr.getScore());
                        answerList = (List<Map<String, Object>>) questionStatisticInformation.get("answerList");
                        oralDetails = pr.getOralDetails();
                        if (CollectionUtils.isNotEmpty(oralDetails)) {
                            for (int i = 0; i < oralDetails.size(); i++) {
                                List<NewHomeworkProcessResult.OralDetail> oralDetailsForBranches = oralDetails.get(i);
                                if (answerList.size() < i + 1) {
                                    break;
                                }
                                Map<String, Object> answerOfBranches = answerList.get(i);//取出每小题的回答信息Map承载
                                List<Map<String, Object>> userInformation = (List<Map<String, Object>>) answerOfBranches.get("users");
                                List<String> userVoiceUrls = new LinkedList<>();// 语音信息
                                double totalOralScore = 0;
                                for (NewHomeworkProcessResult.OralDetail or : oralDetailsForBranches) {
                                    String voiceUrl = or.getAudio();
                                    if (StringUtils.isNoneBlank(voiceUrl)) {
                                        VoiceEngineType voiceEngineType = pr.getVoiceEngineType();
                                        voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                        userVoiceUrls.add(voiceUrl);
                                    }
                                    totalOralScore += SafeConverter.toDouble(or.getOralScore());
                                }
                                int realScore = oralDetailsForBranches.size() > 0 ?
                                        new BigDecimal(totalOralScore)
                                                .divide(new BigDecimal(oralDetailsForBranches.size()), 0, BigDecimal.ROUND_HALF_UP)
                                                .intValue() :
                                        0;
                                String score = oralDetailsForBranches.size() == 1 ?
                                        oralDetailsForBranches.get(0).getOralScoreInterval() :
                                        realScore + "";
                                userInformation.add(MapUtils.m(
                                        "userId", pr.getUserId(),
                                        "userName", reportRateContext.getUserMap().containsKey(pr.getUserId()) ?
                                                reportRateContext.getUserMap().get(pr.getUserId()).fetchRealname() :
                                                "",
                                        "score", score,
                                        "realScore", realScore,
                                        "userVoiceUrls", userVoiceUrls
                                ));
                            }
                        }
                    } else {
                        // first time for the question
                        int showType = 0;
                        List<Integer> submitWays = question
                                .getSubmitWays()
                                .stream()
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(submitWays) &&
                                submitWays.contains(1)) {
                            showType = 1;
                        } else if (CollectionUtils.isNotEmpty(submitWays) &&
                                submitWays.contains(2)) {
                            showType = 2;
                        }
                        answerList = new LinkedList<>();
                        questionStatisticInformation = new LinkedHashMap<>();
                        questionStatisticInformation.put("qid", qId);
                        questionStatisticInformation.put("size", 1);
                        questionStatisticInformation.put("totalScore", pr.getScore());
                        questionStatisticInformation.put("contentType", reportRateContext.getContentTypeMap().get(question.getContentTypeId()) != null ?
                                reportRateContext.getContentTypeMap().get(question.getContentTypeId()).getName() :
                                "无题型");
                        questionStatisticInformation.put("difficulty", question.getDifficultyInt());
                        questionStatisticInformation.put("showType", showType);
                        questionStatisticInformation.put("answerList", answerList);//整个大题各个学生口语题目的回答情况
                        oralDetails = pr.getOralDetails();
                        for (List<NewHomeworkProcessResult.OralDetail> oralDetailsForBranches : oralDetails) {
                            Map<String, Object> answerOfBranches = new LinkedHashMap<>();
                            answerList.add(answerOfBranches);
                            List<Map<String, Object>> userInformation = new LinkedList<>();
                            answerOfBranches.put("answer", "");//预留参数，暂时没用
                            answerOfBranches.put("users", userInformation);//每个小题各个学生的回答情况
                            List<String> userVoiceUrls = new LinkedList<>();
                            double totalOralScore = 0;
                            for (NewHomeworkProcessResult.OralDetail or : oralDetailsForBranches) {
                                String voiceUrl = or.getAudio();
                                if (StringUtils.isNoneBlank(voiceUrl)) {
                                    VoiceEngineType voiceEngineType = pr.getVoiceEngineType();
                                    voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                    userVoiceUrls.add(voiceUrl);
                                }
                                totalOralScore += SafeConverter.toDouble(or.getOralScore());
                            }
                            int realScore = oralDetailsForBranches.size() > 0 ?
                                    new BigDecimal(totalOralScore)
                                            .divide(new BigDecimal(oralDetailsForBranches.size()), 0, BigDecimal.ROUND_HALF_UP)
                                            .intValue() :
                                    0;//实际分数 ， 用来调节样式
                            String score = oralDetailsForBranches.size() == 1 ?
                                    oralDetailsForBranches.get(0).getOralScoreInterval() :
                                    realScore + "";//单个题目的时候显示等级，多个的时候显示分数，
                            userInformation.add(MapUtils.m(
                                    "userId", pr.getUserId(),
                                    "userName", reportRateContext.getUserMap().containsKey(pr.getUserId()) ?
                                            reportRateContext.getUserMap().get(pr.getUserId()).fetchRealname() :
                                            "",
                                    "score", score,
                                    "realScore", realScore,
                                    "userVoiceUrls", userVoiceUrls
                            ));
                        }
                        oralPracticeInformation.put(qId, questionStatisticInformation);
                    }
                }
            }
        }
        List<Map<String, Object>> values = oralPracticeTypeQuestionIds
                .stream()
                .filter(oralPracticeInformation::containsKey)
                .map(qid -> {
                    Map<String, Object> ol = oralPracticeInformation.get(qid);
                    int averageScore = new BigDecimal(SafeConverter.toDouble(ol.get("totalScore")))
                            .divide(new BigDecimal(SafeConverter.toInt(ol.get("size"))), 0, BigDecimal.ROUND_HALF_UP)
                            .intValue();
                    ol.put("averageScore", averageScore);
                    List<Map<String, Object>> answerList = (List<Map<String, Object>>) ol.get("answerList");//整个大题各个学生口语题目的回答情况
                    answerList.forEach(answerOfBranches -> {
                        List<Map<String, Object>> userInformation = (List<Map<String, Object>>) answerOfBranches.get("users");
                        userInformation.sort((o1, o2) -> -Double.compare(SafeConverter.toDouble(o1.get("realScore")), SafeConverter.toDouble(o2.get("realScore"))));
                    });
                    ol.remove("totalScore");
                    ol.remove("size");
                    return ol;
                })
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(values)) {
            reportRateContext.getResult().put(getObjectiveConfigType().name(), values);
        }

    }

    @Override
    public void fetchNewHomeworkCommonObjectiveConfigTypePart(ObjectiveConfigTypePartContext context) {
        NewHomeworkPracticeContent target = context.getTarget();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = context.getNewHomeworkResultMap();
        ObjectiveConfigType type = context.getType();
        Map<Long, User> userMap = context.getUserMap();
        MapMessage mapMessage = MapMessage.successMessage();
        context.setMapMessage(mapMessage);
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(target.processNewHomeworkQuestion(false).stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));
        NewHomework newHomework = context.getNewHomework();
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        for (NewHomeworkQuestion newHomeworkQuestion : target.processNewHomeworkQuestion(null)) {
            NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomework.NewHomeworkQuestionObj(newHomework.getId(), type, Collections.emptyList(), newHomeworkQuestion.getQuestionId());
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
                subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
            }
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        List<String> newHomeworkProcessResultIds = subHomeworkResultAnswerMap.values()
                .stream()
                .map(SubHomeworkResultAnswer::getProcessId)
                .collect(Collectors.toList());
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), newHomeworkProcessResultIds);
        //**************** begin 题信息初始化 NewQuestionReportBO 对于每一题的信息结构 *************//
        List<NewQuestionReportBO> newQuestionReportBOs = target.processNewHomeworkQuestion(false)
                .stream()
                .map(o -> {
                    if (!allQuestionMap.containsKey(o.getQuestionId())) {
                        return null;
                    }
                    NewQuestionReportBO newQuestionReportBO = new NewQuestionReportBO(o.getQuestionId());
                    newQuestionReportBO.setType(2);
                    NewQuestion question = allQuestionMap.get(o.getQuestionId());
                    List<NewQuestionsSubContents> subContents = question.getContent().getSubContents();
                    for (int i = 0; i < subContents.size(); i++) {
                        NewQuestionReportBO.SubQuestion subQuestion = new NewQuestionReportBO.SubQuestion();
                        newQuestionReportBO.getSubQuestions().add(subQuestion);
                    }
                    newQuestionReportBO.setContentType(
                            contentTypeMap.containsKey(question.getContentTypeId()) ?
                                    contentTypeMap.get(question.getContentTypeId()).getName() :
                                    "无题型");
                    newQuestionReportBO.setDifficulty(question.getDifficultyInt());
                    newQuestionReportBO.setDifficultyName(QuestionConstants.newDifficultyMap.get(question.getDifficultyInt()));
                    return newQuestionReportBO;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, NewQuestionReportBO> newQuestionReportBOMap = newQuestionReportBOs
                .stream()
                .collect(Collectors.toMap(NewQuestionReportBO::getQid, Function.identity()));
        //************ begin 每个题信息的填充 NewHomeworkProcessResult 的questionId 和 newQuestionReportBOMap 处理获取 NewQuestionReportBO 然后处理 ********//
        for (NewHomeworkProcessResult p : newHomeworkProcessResultMap.values()) {
            if (!newQuestionReportBOMap.containsKey(p.getQuestionId())) {
                continue;
            }
            if (!allQuestionMap.containsKey(p.getQuestionId())) {
                continue;
            }
            NewQuestion newQuestion = allQuestionMap.get(p.getQuestionId());
            List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
            List<List<BaseHomeworkProcessResult.OralDetail>> oralDetails = p.getOralDetails();
            if (oralDetails.size() != subContents.size()) {
                continue;
            }
            NewQuestionReportBO newQuestionReportBO = newQuestionReportBOMap.get(p.getQuestionId());
            newQuestionReportBO.setNum(newQuestionReportBO.getNum() + 1);
            if (newQuestionReportBO.getSubQuestions().size() != oralDetails.size()) {
                continue;
            }
            int i = 0;
            for (List<NewHomeworkProcessResult.OralDetail> oralDetailsForBranches : oralDetails) {
                NewQuestionReportBO.SubQuestion subQuestion = newQuestionReportBO.getSubQuestions().get(i);
                List<String> userVoiceUrls = new LinkedList<>();
                double totalOralScore = 0;
                for (NewHomeworkProcessResult.OralDetail or : oralDetailsForBranches) {
                    String voiceUrl = or.getAudio();
                    if (StringUtils.isNoneBlank(voiceUrl)) {
                        VoiceEngineType voiceEngineType = p.getVoiceEngineType();
                        voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                        userVoiceUrls.add(voiceUrl);
                    }
                    totalOralScore += SafeConverter.toDouble(or.getOralScore());
                }
                int realScore = oralDetailsForBranches.size() > 0 ?
                        new BigDecimal(totalOralScore)
                                .divide(new BigDecimal(oralDetailsForBranches.size()), 0, BigDecimal.ROUND_HALF_UP)
                                .intValue() :
                        0;//实际分数 ， 用来调节样式
                String score = oralDetailsForBranches.size() == 1 ?
                        oralDetailsForBranches.get(0).getOralScoreInterval() :
                        realScore + "";//单个题目的时候显示等级，多个的时候显示分数，
                NewQuestionReportBO.UserToQuestion u = new NewQuestionReportBO.UserToQuestion();
                u.setUid(p.getUserId());
                u.setUserName(userMap.containsKey(u.getUid()) ? userMap.get(u.getUid()).fetchRealname() : "");
                u.setRealScore(realScore);
                u.setUserVoiceUrls(userVoiceUrls);
                u.setScore(score);
                subQuestion.getUsers().add(u);
                i++;
            }
        }
        //************ end 每个题信息的填充 NewHomeworkProcessResult 的questionId 和 newQuestionReportBOMap 处理获取 NewQuestionReportBO 然后处理 ********//
        mapMessage.add("questions", newQuestionReportBOs);
    }


    @Override
    public void fetchNewHomeworkSingleQuestionPart(ObjectiveConfigTypePartContext context) {
        //********* begin 初始化数据准备 *************//
        Map<Long, NewHomeworkResult> newHomeworkResultMap = context.getNewHomeworkResultMap();
        ObjectiveConfigType type = context.getType();
        Map<Long, User> userMap = context.getUserMap();
        String questionId = context.getQuestionId();
        MapMessage mapMessage = MapMessage.successMessage();
        context.setMapMessage(mapMessage);
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(Collections.singleton(questionId));
        if (!allQuestionMap.containsKey(questionId)) {
            context.setMapMessage(MapMessage.errorMessage());
            return;
        }
        NewHomework newHomework = context.getNewHomework();

        NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomework.NewHomeworkQuestionObj(newHomework.getId(), type, Collections.emptyList(), questionId);
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
            subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
        }

        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);


        List<String> processIds = subHomeworkResultAnswerMap.values().stream().map(SubHomeworkResultAnswer::getProcessId).collect(Collectors.toList());
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
        //********* end 初始化数据准备 *************//


        //**** begin  构件 NewQuestionReportBO 对于返回的数据结构 ******//
        //*******结果数据初构，和process无关的填充//
        NewQuestionReportBO newQuestionReportBO = new NewQuestionReportBO(questionId);
        newQuestionReportBO.setType(2);
        NewQuestion question = allQuestionMap.get(questionId);
        List<NewQuestionsSubContents> subContents = question.getContent().getSubContents();
        for (int i = 0; i < subContents.size(); i++) {
            NewQuestionReportBO.SubQuestion subQuestion = new NewQuestionReportBO.SubQuestion();
            newQuestionReportBO.getSubQuestions().add(subQuestion);
        }
        newQuestionReportBO.setContentType(
                contentTypeMap.containsKey(question.getContentTypeId()) ?
                        contentTypeMap.get(question.getContentTypeId()).getName() :
                        "无题型");
        newQuestionReportBO.setDifficulty(question.getDifficultyInt());
        newQuestionReportBO.setDifficultyName(QuestionConstants.newDifficultyMap.get(question.getDifficultyInt()));

        //**** end  构件 NewQuestionReportBO 对于返回的数据结构 ******//


        //********* begin process相关数据的填充********//
        for (NewHomeworkProcessResult p : newHomeworkProcessResultMap.values()) {
            if (!Objects.equals(questionId, p.getQuestionId())) {
                continue;
            }
            List<List<BaseHomeworkProcessResult.OralDetail>> oralDetails = p.getOralDetails();
            if (oralDetails.size() != subContents.size()) {
                continue;
            }
            newQuestionReportBO.setNum(newQuestionReportBO.getNum() + 1);
            if (newQuestionReportBO.getSubQuestions().size() != oralDetails.size()) {
                continue;
            }
            int i = 0;
            for (List<NewHomeworkProcessResult.OralDetail> oralDetailsForBranches : oralDetails) {
                NewQuestionReportBO.SubQuestion subQuestion = newQuestionReportBO.getSubQuestions().get(i);
                List<String> userVoiceUrls = new LinkedList<>();
                double totalOralScore = 0;
                for (NewHomeworkProcessResult.OralDetail or : oralDetailsForBranches) {
                    String voiceUrl = or.getAudio();
                    if (StringUtils.isNoneBlank(voiceUrl)) {
                        VoiceEngineType voiceEngineType = p.getVoiceEngineType();
                        voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                        userVoiceUrls.add(voiceUrl);
                    }
                    totalOralScore += SafeConverter.toDouble(or.getOralScore());
                }
                int realScore = oralDetailsForBranches.size() > 0 ?
                        new BigDecimal(totalOralScore)
                                .divide(new BigDecimal(oralDetailsForBranches.size()), 0, BigDecimal.ROUND_HALF_UP)
                                .intValue() :
                        0;//实际分数 ， 用来调节样式
                String score = oralDetailsForBranches.size() == 1 ?
                        oralDetailsForBranches.get(0).getOralScoreInterval() :
                        realScore + "";//单个题目的时候显示等级，多个的时候显示分数，
                NewQuestionReportBO.UserToQuestion u = new NewQuestionReportBO.UserToQuestion();
                u.setUid(p.getUserId());
                u.setUserName(userMap.containsKey(u.getUid()) ? userMap.get(u.getUid()).fetchRealname() : "");
                u.setRealScore(realScore);
                u.setUserVoiceUrls(userVoiceUrls);
                u.setScore(score);
                subQuestion.getUsers().add(u);
                i++;
            }
        }
        //********* end process相关数据的填充********//
        mapMessage.add("newQuestionReportBO", newQuestionReportBO);
    }


    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportPersonalRateContext reportRateContext) {
        List<Map<String, Object>> value = new LinkedList<>();
        User user = reportRateContext.getUser();
        List<String> oralPracticeQuestionIds = reportRateContext.getNewHomework().findQuestionIds(getObjectiveConfigType(), false);
        if (reportRateContext.getNewHomeworkResult().isFinishedOfObjectiveConfigType(reportRateContext.getType())) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = reportRateContext.getNewHomeworkResult().getPractices().get(reportRateContext.getType());
            LinkedHashMap<String, String> answers = newHomeworkResultAnswer.getAnswers();
            for (String qid : oralPracticeQuestionIds) {
                NewQuestion question = reportRateContext.getAllNewQuestionMap().get(qid);
                NewHomeworkProcessResult pr = reportRateContext.getNewHomeworkProcessResultMap().get(answers.get(qid));
                if (question != null && pr != null) {
                    Map<String, Object> questionStatisticInformation = new LinkedHashMap<>();
                    int showType = 0;
                    List<Integer> submitWays = question
                            .getSubmitWays()
                            .stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(submitWays) &&
                            submitWays.contains(1)) {
                        showType = 1;
                    } else if (CollectionUtils.isNotEmpty(submitWays) &&
                            submitWays.contains(2)) {
                        showType = 2;
                    }
                    PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(pr.getPracticeId());
                    questionStatisticInformation.put("subject",reportRateContext.getNewHomework().getSubject());
                    questionStatisticInformation.put("categoryId",practiceType!=null ? practiceType.getCategoryId() : "");
                    questionStatisticInformation.put("objectiveConfigType",reportRateContext.getType());
                    questionStatisticInformation.put("qid", qid);
                    questionStatisticInformation.put("store", SafeConverter.toDouble(pr.getScore()));
                    questionStatisticInformation.put("contentType", reportRateContext.getContentTypeMap().get(question.getContentTypeId()) != null ? reportRateContext.getContentTypeMap().get(question.getContentTypeId()).getName() : "无题型");
                    questionStatisticInformation.put("difficulty", question.getDifficultyInt());
                    questionStatisticInformation.put("showType", showType);
                    List<Map<String, Object>> answerList = new LinkedList<>();// 每个整个题目的学生成绩
                    questionStatisticInformation.put("answerList", answerList);
                    List<List<NewHomeworkProcessResult.OralDetail>> oralDetails = pr.getOralDetails();
                    for (List<NewHomeworkProcessResult.OralDetail> oralDetailsForBranches : oralDetails) {
                        Map<String, Object> oralDetailBranchInformation = new LinkedHashMap<>();
                        List<String> userVoiceUrls = new LinkedList<>();
                        oralDetailBranchInformation.put("userVoiceUrls", userVoiceUrls);
                        oralDetailBranchInformation.put("userName", user.fetchRealname());
                        oralDetailBranchInformation.put("userId", user.getId());
                        double totalOralScore = 0;
                        for (NewHomeworkProcessResult.OralDetail or : oralDetailsForBranches) {
                            String voiceUrl = or.getAudio();
                            if (StringUtils.isNotEmpty(voiceUrl)) {
                                VoiceEngineType voiceEngineType = pr.getVoiceEngineType();
                                voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                userVoiceUrls.add(voiceUrl);
                                oralDetailBranchInformation.put("engineScore", or.getStandardScore());
                                questionStatisticInformation.put("engineName", voiceEngineType == null ? "Unknown" : voiceEngineType);
                            }
                            totalOralScore += SafeConverter.toDouble(or.getOralScore());
                        }
                        int realScore = oralDetailsForBranches.size() > 0 ?
                                new BigDecimal(totalOralScore)
                                        .divide(new BigDecimal(oralDetailsForBranches.size()), 0, BigDecimal.ROUND_HALF_UP)
                                        .intValue() :
                                0;
                        String score = oralDetailsForBranches.size() == 1 ?
                                oralDetailsForBranches.get(0).getOralScoreInterval() :
                                realScore + "";
                        oralDetailBranchInformation.put("score", score);
                        oralDetailBranchInformation.put("realScore", realScore);
                        answerList.add(oralDetailBranchInformation);
                    }
                    value.add(questionStatisticInformation);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(value)) {
            reportRateContext.getResultMap().put(reportRateContext.getType(), value);
        }
    }
    @Override
    public void processQuestionPartTypeInfo(Map<Long, NewHomeworkResult> newHomeworkResultMap, NewHomework newHomework, ObjectiveConfigType type, Map<ObjectiveConfigType, Object> result,String cdnBaseUrl) {

        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap
                .values()
                .stream()
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors.toList());
        Map<String, Object> typeResult = new LinkedHashMap<>();
        typeResult.put("type",type);
        typeResult.put("typeName",type.getValue());
        if (CollectionUtils.isEmpty(newHomeworkResults)){
            typeResult.put("finishCount", 0);
            result.put(type, typeResult);
            return;
        }

        List<NewHomework.NewHomeworkQuestionObj> newHomeworkQuestionObjs = newHomework.processSubHomeworkResultAnswerIdsByObjectConfigType(Collections.singleton(type));
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        List<NewHomeworkQuestion> newHomeworkQuestions = target.processNewHomeworkQuestion(false);

        Map<String, QuestionReportDetail> questionReportDetailMap = new LinkedHashMap<>();
        List<QuestionReportDetail> questionsInfo = new LinkedList<>();
        for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
            QuestionReportDetail questionReportDetail = new QuestionReportDetail();
            questionReportDetail.setQuestionId(newHomeworkQuestion.getQuestionId());
            questionReportDetailMap.put(newHomeworkQuestion.getQuestionId(), questionReportDetail);
            questionsInfo.add(questionReportDetail);
        }

        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        int finishCount = 0;
        int totalDuration = 0;
        int totalScore = 0;
        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            finishCount++;
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            Integer duration = newHomeworkResultAnswer.processDuration();
            totalDuration += SafeConverter.toInt(new BigDecimal(SafeConverter.toInt(duration)).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue());
            totalScore += SafeConverter.toInt(SafeConverter.toInt(newHomeworkResultAnswer.processScore(type)));
            for (NewHomework.NewHomeworkQuestionObj obj : newHomeworkQuestionObjs) {
                subHomeworkResultAnswerIds.add(obj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
            }
        }
        //平均分数和平均时长
        int avgScore = 0;
        int avgDuration = 0;
        if (finishCount != 0) {
            avgScore = new BigDecimal(totalScore).divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
            avgDuration = new BigDecimal(totalDuration).divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_UP).intValue();
        }
        typeResult.put("finishCount", finishCount);
        typeResult.put("avgScore", avgScore);
        typeResult.put("avgDuration", avgDuration);

        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        List<String> processIds = new LinkedList<>();
        for (SubHomeworkResultAnswer subHomeworkResultAnswer : subHomeworkResultAnswerMap.values()) {
            processIds.add(subHomeworkResultAnswer.getProcessId());
        }
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
        for (NewHomeworkProcessResult newHomeworkProcessResult : newHomeworkProcessResultMap.values()) {
            if (questionReportDetailMap.containsKey(newHomeworkProcessResult.getQuestionId())) {
                QuestionReportDetail questionReportDetail = questionReportDetailMap.get(newHomeworkProcessResult.getQuestionId());
                questionReportDetail.setTotalNum(1 + questionReportDetail.getTotalNum());
                questionReportDetail.setTotalScore(SafeConverter.toDouble(newHomeworkProcessResult.getScore() + questionReportDetail.getTotalScore()));
            }
        }
        //每个题的得分率
        questionsInfo.stream()
                .filter(o -> o.getTotalNum() > 0)
                .filter(o -> o.getTotalScore() > 0)
                .forEach(o -> {
                    int proportion = new BigDecimal(o.getTotalScore()).divide(new BigDecimal(o.getTotalNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                    o.setProportion(proportion);
                });
        typeResult.put("questionsInfo", questionsInfo);
        typeResult.put("type",type);
        typeResult.put("typeName",type.getValue());
        result.put(type, typeResult);
    }

}
