package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportPersonalRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.NewQuestionReportBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.rate.QuestionDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.pc.QuestionReportDetail;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.newhomework.impl.template.ProcessNewHomeworkAnswerDetailTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.question.api.entity.TestMethod;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class ProcessNewHomeworkAnswerDetailCommonTemplate extends ProcessNewHomeworkAnswerDetailTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.COMMON;
    }

    @Override
    public void processNewHomeworkAnswerDetail(ReportRateContext reportRateContext) {
        Map<String, NewHomeworkResult> newHomeworkResultMapToObjectiveConfigType = handlerNewHomeworkResultMap(reportRateContext.getNewHomeworkResultMap(), reportRateContext.getType());
        if (MapUtils.isEmpty(newHomeworkResultMapToObjectiveConfigType))
            return;
        List<NewHomeworkProcessResult> processResultList = newHomeworkResultMapToObjectiveConfigType.values()
                .stream()
                .map(o -> {
                    NewHomeworkResultAnswer newHomeworkResultAnswer = o.getPractices().get(reportRateContext.getType());
                    LinkedHashMap<String, String> stringStringLinkedHashMap = newHomeworkResultAnswer.processAnswers();
                    return stringStringLinkedHashMap.values();
                })
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(reportRateContext.getNewHomeworkProcessResultMap()::containsKey)
                .map(reportRateContext.getNewHomeworkProcessResultMap()::get)
                .collect(Collectors.toList());

        List<QuestionDetail> values = newInternalProcessHomeworkAnswerDetail(reportRateContext.getUserMap(), reportRateContext.getAllNewQuestionMap(), reportRateContext.getContentTypeMap(), reportRateContext.getNewHomework(), reportRateContext.getType(), reportRateContext.getQuestions(), processResultList);
        if (CollectionUtils.isNotEmpty(values)) {
            reportRateContext.getResult().put(reportRateContext.getType().name(), values);
        }
    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportPersonalRateContext reportRateContext) {
        List<Map<String, Object>> questionAnswerList;
        // 所有应该做的题
        List<String> qids = reportRateContext.getNewHomework().findQuestionIds(reportRateContext.getType(), true);
        Map<String, NewQuestion> newQuestionMap;
        // <已做的题,已做的题结果>
        Map<String, String> processAnswers = new LinkedHashMap<>();
        if (reportRateContext.getNewHomeworkResult().getPractices().containsKey(reportRateContext.getType())) {
            processAnswers = reportRateContext.getNewHomeworkResult().getPractices().get(reportRateContext.getType()).processAnswers();
        }
        if (MapUtils.isEmpty(processAnswers)) {
            return;
        }
        Map<String, NewHomeworkProcessResult> processResultMap = processAnswers.values()
                .stream()
                .filter(reportRateContext.getNewHomeworkProcessResultMap()::containsKey)
                .collect(Collectors
                        .toMap(Function.identity(), reportRateContext.getNewHomeworkProcessResultMap()::get));
        Map<String, NewHomeworkQuestion> newHomeworkQuestionMap = new HashMap<>();
        if (ObjectiveConfigType.READ_RECITE.equals(reportRateContext.getType())) {
            List<NewHomeworkQuestion> newHomeworkQuestions = reportRateContext.getNewHomework().findNewHomeworkQuestions(reportRateContext.getType());
            newHomeworkQuestionMap = newHomeworkQuestions
                    .stream()
                    .collect(Collectors
                            .toMap(NewHomeworkQuestion::getQuestionId, Function.identity()));
            newQuestionMap = doHomeworkProcessor.initReadReciteDate(newHomeworkQuestions, reportRateContext.getType(), false);
        } else {
            newQuestionMap = qids
                    .stream()
                    .filter(reportRateContext.getAllNewQuestionMap()::containsKey)
                    .collect(Collectors
                            .toMap(Function.identity(), reportRateContext.getAllNewQuestionMap()::get));
        }
        Map<String, String> finalProcessAnswers = processAnswers;
        Map<String, NewQuestion> finalNewQuestionMap = newQuestionMap;
        Map<String, NewHomeworkQuestion> finalNewHomeworkQuestionMap = newHomeworkQuestionMap;


        Map<String, TestMethod> allTestMethodMap = new HashMap<>();
        Map<String, String> qidToTestMethodId = new HashMap<>();

        if (reportRateContext.getType() == ObjectiveConfigType.BASIC_KNOWLEDGE) {
            qidToTestMethodId = newQuestionMap.values()
                    .stream()
                    .filter(q -> CollectionUtils.isNotEmpty(q.testMethodList()))
                    .collect(Collectors.toMap(NewQuestion::getId, q -> q.testMethodList().get(0)));
            allTestMethodMap = testMethodLoaderClient.loadTestMethodIncludeDisabled(qidToTestMethodId.values());
        }
        Map<String, String> finalQidToTestMethodId = qidToTestMethodId;
        Map<String, TestMethod> finalAllTestMethodMap = allTestMethodMap;
        questionAnswerList = qids.stream()
                .filter(processAnswers::containsKey)
                .map(qid -> {
                    String testMethodName = "";
                    if (finalQidToTestMethodId.containsKey(qid) && finalAllTestMethodMap.containsKey(finalQidToTestMethodId.get(qid))) {
                        TestMethod testMethod = finalAllTestMethodMap.get(finalQidToTestMethodId.get(qid));
                        testMethodName = testMethod != null ? SafeConverter.toString(testMethod.getName()) : "";
                    }
                    NewHomeworkProcessResult tempResult = processResultMap.get(finalProcessAnswers.get(qid));
                    NewQuestion question = finalNewQuestionMap.getOrDefault(qid, null);
                    if (question == null || question.getContent() == null || question.getContent().getSubContents() == null) {
                        return null;
                    }
                    List<NewQuestionsSubContents> subContents = question.getContent().getSubContents();
                    List<List<String>> standardAnswers = subContents
                            .stream()
                            .map(o -> o.getAnswerList(reportRateContext.getNewHomework().getSubject()))
                            .collect(Collectors.toList());

                    return MapUtils.m("qid", qid,
                            "testMethodName", testMethodName,
                            "contentType", reportRateContext.getContentTypeMap().get(question.getContentTypeId()) != null ?
                                    reportRateContext.getContentTypeMap().get(question.getContentTypeId()).getName() :
                                    "无题型",
                            "difficulty", question.getDifficultyInt(),
                            "standardAnswers", tempResult != null ? NewHomeworkUtils.pressAnswer(subContents, standardAnswers) : "",
                            "userAnswers", tempResult != null ? NewHomeworkUtils.pressAnswer(subContents, tempResult.getUserAnswers()) : "",
                            "review", tempResult != null ? tempResult.getReview() : null,
                            "correction", tempResult != null ? tempResult.getCorrection() : null,
                            "correct_des", (tempResult != null && tempResult.getCorrection() != null) ? tempResult.getCorrection().getDescription() : "",
                            "fileType", (tempResult != null && CollectionUtils.isNotEmpty(tempResult.getFiles())) ?
                                    tempResult
                                            .getFiles()
                                            .stream()
                                            .flatMap(Collection::stream)
                                            .map(NewHomeworkQuestionFile::getFileType)
                                            .collect(Collectors.toSet()) :
                                    Collections.emptyList(),
                            "fileUrl", (tempResult != null && CollectionUtils.isNotEmpty(tempResult.getFiles())) ?
                                    tempResult
                                            .getFiles()
                                            .stream()
                                            .flatMap(Collection::stream)
                                            .map(NewHomeworkQuestionFileHelper::getFileUrl)
                                            .collect(Collectors.toList()) :
                                    Collections.emptyList(),
                            "articleName", finalNewQuestionMap.get(qid) != null ?
                                    finalNewQuestionMap.get(qid).getArticleName() :
                                    "",
                            "paragraphCName", finalNewQuestionMap.get(qid) != null ?
                                    finalNewQuestionMap.get(qid).getParagraph() :
                                    "",
                            "answerWay", finalNewHomeworkQuestionMap.get(qid) != null ?
                                    finalNewHomeworkQuestionMap.get(qid).processAnswerWay() :
                                    ""
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(questionAnswerList)) {
            reportRateContext.getResultMap().put(reportRateContext.getType(), questionAnswerList);
        }
    }

    @Override
    public void fetchNewHomeworkCommonObjectiveConfigTypePart(ObjectiveConfigTypePartContext context) {

        //************ begin 数据初始化准备 *******************//
        NewHomeworkPracticeContent target = context.getTarget();
        NewHomework newHomework = context.getNewHomework();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = context.getNewHomeworkResultMap();
        ObjectiveConfigType type = context.getType();
        Map<Long, User> userMap = context.getUserMap();
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(target.processNewHomeworkQuestion(false).stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));


        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        Map<ObjectiveConfigType, List<NewHomework.NewHomeworkQuestionObj>> objectiveConfigTypeListMap = newHomework.processSubHomeworkResultAnswerIds(Collections.singleton(type));
        List<NewHomework.NewHomeworkQuestionObj> newHomeworkQuestionObjs = objectiveConfigTypeListMap.getOrDefault(type, Collections.emptyList());
        for (NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj : newHomeworkQuestionObjs) {
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
                subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
            }
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        List<String> newHomeworkProcessResultIds = subHomeworkResultAnswerMap.values()
                .stream()
                .map(SubHomeworkResultAnswer::getProcessId)
                .collect(Collectors.toList());
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(context.getNewHomework().getId(), newHomeworkProcessResultIds);

        //************ end 数据初始化准备 *******************//


        //************** begin  数据初始化:对应每题的信息  NewQuestionReportBO 对应着返回数据的结构 *************//
        List<NewQuestionReportBO> newQuestionReportBOs = target.processNewHomeworkQuestion(false)
                .stream()
                .map(o -> {
                    if (!allQuestionMap.containsKey(o.getQuestionId())) {
                        return null;
                    }
                    NewQuestionReportBO newQuestionReportBO = new NewQuestionReportBO(o.getQuestionId());
                    newQuestionReportBO.setType(1);
                    NewQuestion question = allQuestionMap.get(o.getQuestionId());
                    List<NewQuestionsSubContents> subContents = question.getContent().getSubContents();
                    //复合体的结构
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
        //************** end  数据初始化:对应每题的信息  NewQuestionReportBO 对应着返回数据的结构 *************//

        Map<String, NewQuestionReportBO> newQuestionReportBOMap = newQuestionReportBOs
                .stream()
                .collect(Collectors.toMap(NewQuestionReportBO::getQid, Function.identity()));
        //********* begin 数据处理：NewHomeworkProcessResult questionId 来根据 newQuestionReportBOMap 获得NewQuestionReportBO 然后数据汇聚 ********//
        for (NewHomeworkProcessResult p : newHomeworkProcessResultMap.values()) {
            if (!newQuestionReportBOMap.containsKey(p.getQuestionId())) {
                continue;
            }
            if (!allQuestionMap.containsKey(p.getQuestionId())) {
                continue;
            }
            NewQuestion newQuestion = allQuestionMap.get(p.getQuestionId());
            List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
            List<List<String>> userAnswers = p.getUserAnswers();
            if (userAnswers.size() != subContents.size()) {
                continue;
            }
            NewQuestionReportBO newQuestionReportBO = newQuestionReportBOMap.get(p.getQuestionId());
            newQuestionReportBO.setNum(newQuestionReportBO.getNum() + 1);
            NewQuestionReportBO.UserToQuestion u = new NewQuestionReportBO.UserToQuestion();
            u.setUid(p.getUserId());
            u.setUserName(userMap.containsKey(u.getUid()) ? userMap.get(u.getUid()).fetchRealname() : "");
            for (int i = 0; i < subContents.size(); i++) {
                NewQuestionsSubContents newQuestionsSubContents = subContents.get(i);
                List<String> strings = userAnswers.get(i);
                boolean b = p.getSubGrasp().get(i).stream().allMatch(SafeConverter::toBoolean);
                String answer;
                if (b) {
                    answer = "全对学生";
                } else {
                    answer = NewHomeworkUtils.pressAnswer(Collections.singletonList(newQuestionsSubContents), Collections.singletonList(strings));
                    if (!newQuestionReportBO.getSubQuestions().get(i).getUserAnswersMap().containsKey(answer)) {
                        newQuestionReportBO.getSubQuestions().get(i).getUserAnswersMap().put(answer, strings);
                    }
                }
                newQuestionReportBO.getSubQuestions().get(i).getMap().computeIfAbsent(answer, l -> new ArrayList<>()).add(u);
            }
            if (!SafeConverter.toBoolean(p.getGrasp())) {
                newQuestionReportBO.setErrorNum(newQuestionReportBO.getErrorNum() + 1);
            }
        }
        //********* end 数据处理 ********//


        //********* begin 数据后处理，失分率和答案的显示 ********//
        newQuestionReportBOMap.values()
                .stream()
                .filter(o -> o.getNum() > 0)
                .forEach(o -> {
                    int errorRate = new BigDecimal(100 * o.getErrorNum()).divide(new BigDecimal(o.getNum()), BigDecimal.ROUND_HALF_UP, 0).intValue();
                    o.setErrorRate(errorRate);
                    List<NewQuestionReportBO.SubQuestion> subQuestions = o.getSubQuestions();
                    if (subQuestions.size() > 0) {
                        //复合体信息处理
                        for (NewQuestionReportBO.SubQuestion subQuestion : subQuestions) {
                            Map<String, List<NewQuestionReportBO.UserToQuestion>> map = subQuestion.getMap();
                            if (map.containsKey("全对学生")) {
                                List<NewQuestionReportBO.UserToQuestion> userToQuestions = map.get("全对学生");
                                subQuestion.getAnswer().add(MapUtils.m(
                                        "grasp", true,
                                        "answerWord", "全对学生", "userToQuestions", userToQuestions
                                ));
                                map.remove("全对学生");
                            }
                            for (Map.Entry<String, List<NewQuestionReportBO.UserToQuestion>> entry : map.entrySet()) {
                                subQuestion.getAnswer().add(MapUtils.m(
                                        "grasp", false,
                                        "answerWord", entry.getKey(), "userToQuestions", entry.getValue(),
                                        "userAnswers", subQuestion.getUserAnswersMap().get(entry.getKey())
                                ));
                            }
                            subQuestion.setUserAnswersMap(null);
                            subQuestion.setMap(null);
                        }
                    }
                });
        //********* end 数据后处理，失分率和答案的显示 ********//
        List<NewQuestionReportBO> reportBOs = newQuestionReportBOMap.values()
                .stream()
                .sorted((o1, o2) -> Integer.compare(o2.getErrorRate(), o1.getErrorRate()))
                .collect(Collectors.toList());
        MapMessage mapMessage = MapMessage.successMessage();
        context.setMapMessage(mapMessage);
        mapMessage.add("questions", reportBOs);
    }

    @Override
    public void fetchNewHomeworkSingleQuestionPart(ObjectiveConfigTypePartContext context) {
        //********* begin 初始化数据准备 *************//
        Map<Long, NewHomeworkResult> newHomeworkResultMap = context.getNewHomeworkResultMap();
        ObjectiveConfigType type = context.getType();
        Map<Long, User> userMap = context.getUserMap();
        String questionId = context.getQuestionId();
        NewHomework newHomework = context.getNewHomework();


        MapMessage mapMessage = MapMessage.successMessage();
        context.setMapMessage(mapMessage);
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(Collections.singleton(questionId));
        if (!allQuestionMap.containsKey(questionId)) {
            context.setMapMessage(MapMessage.errorMessage());
            return;
        }
        Map<ObjectiveConfigType, List<NewHomework.NewHomeworkQuestionObj>> objectiveConfigTypeListMap = newHomework.processSubHomeworkResultAnswerIds(Collections.singleton(type));
        Map<String, NewHomework.NewHomeworkQuestionObj> newHomeworkQuestionObjMap = objectiveConfigTypeListMap.getOrDefault(type, Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(NewHomework.NewHomeworkQuestionObj::getQuestionId, Function.identity()));
        if (!newHomeworkQuestionObjMap.containsKey(questionId)) {
            return;
        }
        NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = newHomeworkQuestionObjMap.get(questionId);
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
            subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
        }

        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);


        List<String> processIds = subHomeworkResultAnswerMap.values().stream().map(SubHomeworkResultAnswer::getProcessId).collect(Collectors.toList());

        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);

        NewQuestion newQuestion = allQuestionMap.get(questionId);
        //********* end 初始化数据准备 *************//


        //**** begin  构件 NewQuestionReportBO 对于返回的数据结构 ******//
        NewQuestionReportBO newQuestionReportBO = new NewQuestionReportBO(questionId);
        //************不相关process的数据填充：搭建数据初始化//
        newQuestionReportBO.setType(1);
        List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
        for (int i = 0; i < subContents.size(); i++) {
            NewQuestionReportBO.SubQuestion subQuestion = new NewQuestionReportBO.SubQuestion();
            newQuestionReportBO.getSubQuestions().add(subQuestion);
        }
        newQuestionReportBO.setContentType(
                contentTypeMap.containsKey(newQuestion.getContentTypeId()) ?
                        contentTypeMap.get(newQuestion.getContentTypeId()).getName() :
                        "无题型");
        newQuestionReportBO.setDifficulty(newQuestion.getDifficultyInt());
        newQuestionReportBO.setDifficultyName(QuestionConstants.newDifficultyMap.get(newQuestion.getDifficultyInt()));
        //************不相关process的数据填充//
        //**** end  NewQuestionReportBO 对于返回的数据结构 ******//


        //******** begin process的处理 ********//
        for (NewHomeworkProcessResult p : newHomeworkProcessResultMap.values()) {
            if (!questionId.equals(p.getQuestionId())) {
                continue;
            }
            List<List<String>> userAnswers = p.getUserAnswers();
            if (userAnswers.size() != subContents.size()) {
                continue;
            }
            if (p.getSubGrasp().size() != subContents.size()) {
                continue;
            }
            newQuestionReportBO.setNum(newQuestionReportBO.getNum() + 1);
            NewQuestionReportBO.UserToQuestion u = new NewQuestionReportBO.UserToQuestion();
            u.setUid(p.getUserId());
            u.setUserName(userMap.containsKey(u.getUid()) ? userMap.get(u.getUid()).fetchRealname() : "");
            for (int i = 0; i < subContents.size(); i++) {
                NewQuestionsSubContents newQuestionsSubContents = subContents.get(i);
                List<String> strings = userAnswers.get(i);
                boolean b = p.getSubGrasp().get(i).stream().allMatch(SafeConverter::toBoolean);
                String answer;
                if (b) {
                    answer = "全对学生";
                } else {
                    answer = NewHomeworkUtils.pressAnswer(Collections.singletonList(newQuestionsSubContents), Collections.singletonList(strings));
                }
                newQuestionReportBO.getSubQuestions().get(i).getMap().computeIfAbsent(answer, l -> new ArrayList<>()).add(u);
            }
            if (!SafeConverter.toBoolean(p.getGrasp())) {
                newQuestionReportBO.setErrorNum(newQuestionReportBO.getErrorNum() + 1);
            }
        }
        //******** end process的处理 ********//


        //数据后处理，给出返回结构:（1）答案文案，map key 是答案文案
        if (newQuestionReportBO.getNum() > 0) {
            int errorRate = new BigDecimal(100 * newQuestionReportBO.getErrorNum()).divide(new BigDecimal(newQuestionReportBO.getNum()), BigDecimal.ROUND_HALF_UP, 0).intValue();
            newQuestionReportBO.setErrorRate(errorRate);
            List<NewQuestionReportBO.SubQuestion> subQuestions = newQuestionReportBO.getSubQuestions();
            if (subQuestions.size() > 0) {
                for (NewQuestionReportBO.SubQuestion subQuestion : subQuestions) {
                    Map<String, List<NewQuestionReportBO.UserToQuestion>> map = subQuestion.getMap();
                    if (map.containsKey("全对学生")) {
                        List<NewQuestionReportBO.UserToQuestion> userToQuestions = map.get("全对学生");
                        subQuestion.getAnswer().add(MapUtils.m(
                                "grasp", true,
                                "answerWord", "全对学生", "userToQuestions", userToQuestions
                        ));
                        map.remove("全对学生");
                    }
                    for (Map.Entry<String, List<NewQuestionReportBO.UserToQuestion>> entry : map.entrySet()) {
                        subQuestion.getAnswer().add(MapUtils.m(
                                "grasp", false,
                                "answerWord", entry.getKey(), "userToQuestions", entry.getValue()
                        ));
                    }
                    subQuestion.setMap(null);
                }
            }
        }
        mapMessage.add("newQuestionReportBO", newQuestionReportBO);
    }

    @Override
    public String processStudentPartTypeScore(NewHomework newHomework, NewHomeworkResultAnswer newHomeworkResultAnswer, ObjectiveConfigType type) {
        //普通非主观题
        Integer score = newHomeworkResultAnswer.processScore(type);

        return SafeConverter.toInt(score) + "分";
    }

    @Override
    public void processQuestionPartTypeInfo(Map<Long, NewHomeworkResult> newHomeworkResultMap, NewHomework newHomework, ObjectiveConfigType type, Map<ObjectiveConfigType, Object> result, String cdnBaseUrl) {
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values()
                .stream()
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors.toList());
        Map<String, Object> typeResult = new LinkedHashMap<>();
        typeResult.put("type", type);
        typeResult.put("typeName", type.getValue());
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            typeResult.put("finishCount", 0);
            result.put(type, typeResult);
            return;
        }

        List<NewHomework.NewHomeworkQuestionObj> newHomeworkQuestionObjs = newHomework.processSubHomeworkResultAnswerIdsByObjectConfigType(Collections.singleton(type));
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        List<NewHomeworkQuestion> newHomeworkQuestions = target.processNewHomeworkQuestion(false);

        //begin==初始化数据
        Map<String, QuestionReportDetail> questionReportDetailMap = new LinkedHashMap<>();
        List<QuestionReportDetail> questionsInfo = new LinkedList<>();
        for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
            QuestionReportDetail questionReportDetail = new QuestionReportDetail();
            questionReportDetail.setQuestionId(newHomeworkQuestion.getQuestionId());
            questionReportDetailMap.put(newHomeworkQuestion.getQuestionId(), questionReportDetail);
            questionsInfo.add(questionReportDetail);
        }
        //end==初始化数据

        //begin == 计算平均分和平均时间：顺带 subHomeworkResultAnswerIds
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
        //end == 计算平均分和平均时间：顺带 subHomeworkResultAnswerIds

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
                if (SafeConverter.toBoolean(newHomeworkProcessResult.getGrasp())) {
                    questionReportDetail.setRightNum(1 + questionReportDetail.getRightNum());
                }
            }
        }
        //每个题的正确率
        questionsInfo.stream()
                .filter(o -> o.getTotalNum() > 0)
                .filter(o -> o.getRightNum() > 0)
                .forEach(o -> {
                    int proportion = new BigDecimal(100 * o.getRightNum()).divide(new BigDecimal(o.getTotalNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                    o.setProportion(proportion);
                });
        typeResult.put("questionsInfo", questionsInfo);
        result.put(type, typeResult);
    }

    /**
     * 处理答案，各种题型的答案结果会有不同
     *
     * @return 没做的返回无法查看；做完的，返回“,”分隔的答案字符串
     */
    private String pressHomeworkAnswer(List<NewQuestionsSubContents> qscs, NewHomeworkProcessResult processResult) {
        if (SafeConverter.toBoolean(processResult.getGrasp())) {
            return "答案正确";
        }
        List<List<String>> userAnswerList = processResult.getUserAnswers();
        return NewHomeworkUtils.pressAnswer(qscs, userAnswerList);
    }


    public List<QuestionDetail> newInternalProcessHomeworkAnswerDetail(Map<Long, User> userMap, Map<String, NewQuestion> allNewQuestionMap, Map<Integer, NewContentType> contentTypeMap, NewHomework newHomework, ObjectiveConfigType type, List<String> qids, List<NewHomeworkProcessResult> processResultList) {
        Map<String, NewQuestion> newQuestionMap;
        Map<String, NewHomeworkQuestion> newHomeworkQuestionMap = new HashMap<>();
        //旧朗读背诵需要补全自然段名字
        if (ObjectiveConfigType.READ_RECITE == type) {
            List<NewHomeworkQuestion> newHomeworkQuestions = newHomework.findNewHomeworkQuestions(type);
            newHomeworkQuestionMap = newHomeworkQuestions
                    .stream()
                    .collect(Collectors
                            .toMap(NewHomeworkQuestion::getQuestionId, Function.identity()));
            newQuestionMap = doHomeworkProcessor.initReadReciteDate(newHomeworkQuestions, type, false);
        } else {
            newQuestionMap = qids
                    .stream()
                    .filter(allNewQuestionMap::containsKey)
                    .map(allNewQuestionMap::get)
                    .collect(Collectors
                            .toMap(NewQuestion::getId, Function.identity()));
        }
        Map<String, TestMethod> allTestMethodMap = new HashMap<>();
        Map<String, String> qidToTestMethodId = new HashMap<>();

        if (type == ObjectiveConfigType.BASIC_KNOWLEDGE) {
            qidToTestMethodId = newQuestionMap.values()
                    .stream()
                    .filter(q -> CollectionUtils.isNotEmpty(q.testMethodList()))
                    .collect(Collectors.toMap(NewQuestion::getId, q -> q.testMethodList().get(0)));
            allTestMethodMap = testMethodLoaderClient.loadTestMethodIncludeDisabled(qidToTestMethodId.values());
        }

        List<String> qIds = newHomework.findQuestionIds(type, true);
        Map<String, QuestionDetail> questionDetailMap = new LinkedHashMap<>();
        for (String qid : qIds) {
            if (!newQuestionMap.containsKey(qid))
                continue;
            NewQuestion newQuestion = newQuestionMap.get(qid);
            QuestionDetail questionDetail = new QuestionDetail();
            int showType = 0;
            List<Integer> submitWays = newQuestion
                    .getSubmitWays()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(submitWays) && submitWays.contains(1)) {
                showType = 1;
            } else if (CollectionUtils.isNotEmpty(submitWays) && submitWays.contains(2)) {
                showType = 2;
            }
            String testMethodName = "";
            if (qidToTestMethodId.containsKey(qid) && allTestMethodMap.containsKey(qidToTestMethodId.get(qid))) {
                TestMethod testMethod = allTestMethodMap.get(qidToTestMethodId.get(qid));
                testMethodName = testMethod != null ? SafeConverter.toString(testMethod.getName(), "") : "";
            }
            questionDetail.setQid(qid);
            questionDetail.setTestMethodName(testMethodName);
            questionDetail.setShowType(showType);
            questionDetail.setContentType(contentTypeMap.get(newQuestion.getContentTypeId()) != null ?
                    contentTypeMap.get(newQuestion.getContentTypeId()).getName() :
                    "无题型");
            questionDetail.setDifficulty(newQuestion.getDifficultyInt());
            questionDetail.setAnswerWay(newHomeworkQuestionMap.get(qid) != null ? newHomeworkQuestionMap.get(qid).processAnswerWay() : "");
            questionDetailMap.put(qid, questionDetail);
        }
        for (NewHomeworkProcessResult processResult : processResultList) {
            if (!questionDetailMap.containsKey(processResult.getQuestionId()))
                continue;
            if (!allNewQuestionMap.containsKey(processResult.getQuestionId()))
                continue;
            if (!userMap.containsKey(processResult.getUserId()))
                continue;
            User user = userMap.get(processResult.getUserId());
            NewQuestion newQuestion = allNewQuestionMap.get(processResult.getQuestionId());
            QuestionDetail questionDetail = questionDetailMap.get(processResult.getQuestionId());
            questionDetail.setTotalNum(1 + questionDetail.getTotalNum());
            boolean grasp = SafeConverter.toBoolean(processResult.getGrasp());
            if (!grasp) {
                questionDetail.setErrorNum(1 + questionDetail.getErrorNum());
            }
            List<NewQuestionsSubContents> nscs = newQuestion.getContent().getSubContents();
            String answer = pressHomeworkAnswer(nscs, processResult);
            List<String> showPics = processResult
                    .findAllFiles()
                    .stream()
                    .map(NewHomeworkQuestionFileHelper::getFileUrl)
                    .collect(Collectors.toList());
            Boolean review = processResult.getReview();
            Correction correction = processResult.getCorrection();
            QuestionDetail.StudentDetail studentDetail = new QuestionDetail.StudentDetail();
            studentDetail.setAnswer(answer);
            studentDetail.setUserId(user.getId());
            studentDetail.setUserName(user.fetchRealnameIfBlankId());
            studentDetail.setShowPics(showPics);
            studentDetail.setImgUrl(user.fetchImageUrl());
            studentDetail.setReview(review);
            studentDetail.setCorrection(correction);
            studentDetail.setCorrect_des((correction != null) ? correction.getDescription() : "");
            if (grasp) {
                QuestionDetail.Answer rightAnswer;
                if (questionDetail.getRightAnswer() == null) {
                    rightAnswer = new QuestionDetail.Answer();
                    rightAnswer.setAnswer("答案正确");
                    questionDetail.setRightAnswer(rightAnswer);
                } else {
                    rightAnswer = questionDetail.getRightAnswer();
                }
                rightAnswer.getUsers().add(studentDetail);
            } else {
                QuestionDetail.Answer answerDetail;
                if (questionDetail.getErrorAnswerMap().containsKey(answer)) {
                    answerDetail = questionDetail.getErrorAnswerMap().get(answer);
                } else {
                    answerDetail = new QuestionDetail.Answer();
                    answerDetail.setAnswer(answer);
                    questionDetail.getErrorAnswerMap().put(answer, answerDetail);
                    questionDetail.getErrorAnswerList().add(answerDetail);
                }
                answerDetail.getUsers().add(studentDetail);
            }
        }
        //计算错题率
        //将正确答案的放到回答的list中
        List<QuestionDetail> errorExamList = new LinkedList<>();
        for (String qid : qIds) {
            if (!questionDetailMap.containsKey(qid))
                continue;
            QuestionDetail questionDetail = questionDetailMap.get(qid);
            if (questionDetail.getTotalNum() > 0) {
                int rate = new BigDecimal(questionDetail.getErrorNum() * 100)
                        .divide(new BigDecimal(questionDetail.getTotalNum()), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue();
                questionDetail.setRate(rate);
            }
            if (questionDetail.getRightAnswer() != null) {
                questionDetail.getErrorAnswerList().add(questionDetail.getRightAnswer());
            }
            questionDetail.setErrorAnswerMap(null);
            errorExamList.add(questionDetail);
        }
        if (type != ObjectiveConfigType.KEY_POINTS && type != ObjectiveConfigType.INTERESTING_PICTURE) {
            errorExamList.sort((o1, o2) -> Integer.compare(o2.getRate(), o1.getRate()));
        }
        return errorExamList;
    }
}
