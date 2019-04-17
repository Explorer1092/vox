package com.voxlearning.utopia.service.newhomework.impl.template.internal.report.livecast;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastCommonData;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.template.ProcessLiveCastHomeworkAnswerDetailTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
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
public class ProcessLiveCastHomeworkAnswerDetailCommonTemplate extends ProcessLiveCastHomeworkAnswerDetailTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.COMMON;
    }

    //初始化数据：包含这个类型的newHomeworkResultMapToObjectiveConfigType、对应liveCastHomeworkProcessResultList
    @Override
    public void processNewHomeworkAnswerDetail(LiveCastReportRateContext liveCastReportRateContext) {

        Map<String, LiveCastHomeworkResult> newHomeworkResultMapToObjectiveConfigType = liveCastReportRateContext.getLiveCastHomeworkResultMap()
                .values()
                .stream()
                .filter(o -> MapUtils.isNotEmpty(o.getPractices()) && o.getPractices().containsKey(liveCastReportRateContext.getType()))
                .collect(Collectors.toMap(LiveCastHomeworkResult::getId, Function.identity()));

        Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap = liveCastReportRateContext.getLiveCastHomeworkProcessResultMap();
        List<LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultList = newHomeworkResultMapToObjectiveConfigType.values()
                .stream()
                .map(o -> o.getPractices().get(liveCastReportRateContext.getType()).processAnswers().values())
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(liveCastHomeworkProcessResultMap::containsKey)
                .map(liveCastHomeworkProcessResultMap::get)
                .collect(Collectors.toList());
        List<LiveCastCommonData> values = internalProcessHomeworkAnswerDetail(liveCastReportRateContext.getType(),
                liveCastReportRateContext.getLiveCastHomework(),
                liveCastReportRateContext.getUserMap(),
                liveCastReportRateContext.getAllNewQuestionMap(),
                liveCastReportRateContext.getContentTypeMap(),
                liveCastHomeworkProcessResultList);
        if (CollectionUtils.isNotEmpty(values)) {
            liveCastReportRateContext.getResult().put(liveCastReportRateContext.getType().name(), values);
        }
    }

    // 优化接口
    private List<LiveCastCommonData> internalProcessHomeworkAnswerDetail(ObjectiveConfigType type,
                                                                         LiveCastHomework liveCastHomework,
                                                                         Map<Long, User> userMap, Map<String, NewQuestion> allNewQuestionMap,
                                                                         Map<Integer, NewContentType> contentTypeMap,
                                                                         List<LiveCastHomeworkProcessResult> processResultList) {

        NewHomeworkPracticeContent target = liveCastHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (target == null)
            return Collections.emptyList();
        List<NewHomeworkQuestion> questions = target.processNewHomeworkQuestion(false);
        if (CollectionUtils.isEmpty(questions))
            return Collections.emptyList();
        Map<String, LiveCastCommonData> liveCastCommonDataMap = new LinkedHashMap<>();
        for (NewHomeworkQuestion newHomeworkQuestion : questions) {
            NewQuestion question = allNewQuestionMap.get(newHomeworkQuestion.getQuestionId());
            if (question == null) continue;
            LiveCastCommonData liveCastCommonData = new LiveCastCommonData();
            liveCastCommonData.setQid(newHomeworkQuestion.getQuestionId());
            if (contentTypeMap.containsKey(question.getContentTypeId())) {
                liveCastCommonData.setContentType(contentTypeMap.get(question.getContentTypeId()).getName());
            }
            int showType = 0;
            Set<Integer> submitWays = question.getSubmitWays() != null ?
                    question.getSubmitWays()
                            .stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toSet()) :
                    Collections.emptySet();
            if (CollectionUtils.isNotEmpty(submitWays)
                    && submitWays.contains(1)) {
                showType = 1;
            } else if (CollectionUtils.isNotEmpty(submitWays)
                    && submitWays.contains(2)) {
                showType = 2;
            }
            liveCastCommonData.setShowType(showType);
            liveCastCommonData.setDifficultyInt(question.getDifficultyInt());
            liveCastCommonDataMap.put(newHomeworkQuestion.getQuestionId(), liveCastCommonData);
        }
        for (LiveCastHomeworkProcessResult liveCastHomeworkProcessResult : processResultList) {
            LiveCastCommonData liveCastCommonData = liveCastCommonDataMap.get(liveCastHomeworkProcessResult.getQuestionId());
            if (liveCastCommonData == null)
                continue;
            NewQuestion question = allNewQuestionMap.get(liveCastHomeworkProcessResult.getQuestionId());
            liveCastCommonData.setTotalNum(liveCastCommonData.getTotalNum() + 1);
            if (SafeConverter.toBoolean(liveCastHomeworkProcessResult.getGrasp())) {
                liveCastCommonData.setRightNum(liveCastCommonData.getRightNum() + 1);
            }
            List<NewQuestionsSubContents> newQuestionsSubContent = question.getContent() != null ? question.getContent().getSubContents() : Collections.emptyList();
            String answer = pressHomeworkAnswer(newQuestionsSubContent, liveCastHomeworkProcessResult);
            LiveCastCommonData.AnswerType answerType;
            if (liveCastCommonData.getAnswerTypeMap().containsKey(answer)) {
                answerType = liveCastCommonData.getAnswerTypeMap().get(answer);
            } else {
                answerType = new LiveCastCommonData.AnswerType();
                answerType.setAnswer(answer);
                liveCastCommonData.getAnswerTypeMap().put(answer, answerType);
            }
            User user = userMap.get(liveCastHomeworkProcessResult.getUserId());
            if (user != null) {
                LiveCastCommonData.UserAnswerToQuestion userAnswerToQuestion = new LiveCastCommonData.UserAnswerToQuestion();
                answerType.getUserAnswerToQuestions().add(userAnswerToQuestion);
                userAnswerToQuestion.setUserId(user.getId());
                userAnswerToQuestion.setImgUrl(user.fetchImageUrl());
                userAnswerToQuestion.setUserName(user.fetchRealname());
            }
            liveCastCommonData.setFlag(true);
        }
        liveCastCommonDataMap = liveCastCommonDataMap.values()
                .stream()
                .filter(LiveCastCommonData::isFlag)
                .collect(Collectors.toMap(LiveCastCommonData::getQid, Function.identity()));
        liveCastCommonDataMap.values()
                .forEach(o -> {
                    if (o.getTotalNum() != 0) {
                        o.setRate(new BigDecimal((o.getTotalNum() - o.getRightNum()) * 100)
                                .divide(new BigDecimal(o.getTotalNum()), 0, BigDecimal.ROUND_HALF_UP)
                                .intValue());
                    }
                });
        return liveCastCommonDataMap.values()
                .stream()
                .sorted((o1, o2) -> Integer.compare(o2.getRate(), o1.getRate()))
                .collect(Collectors.toList());
    }


    //1、答题记录进行循环 一题对应一LiveCastCommonData
    //1->1 对于回答，查看是否存在:存在将这个学生加入，不存在新创建，加入该学生信息
    //2、处理数据和对数据排序
    public String pressHomeworkAnswer(List<NewQuestionsSubContents> qscs, LiveCastHomeworkProcessResult processResult) {
        if (SafeConverter.toBoolean(processResult.getGrasp())) {
            return "答案正确";
        }
        List<List<String>> userAnswerList = processResult.getUserAnswers();
        return NewHomeworkUtils.pressAnswer(qscs, userAnswerList);
    }


    //1、对基础数据进行初始化
    //2、对 answer的循环处理
    //后期修改组织代码方式1、根据作业进行初始化话数据构件2、处理学生数据3、将数据后期加工处理
    @Override
    public void processNewHomeworkAnswerDetailPersonal(LiveCastReportRateContext liveCastReportRateContext) {
        LiveCastHomeworkResult liveCastHomeworkResult = liveCastReportRateContext.getLiveCastHomeworkResult();
        Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap = liveCastReportRateContext.getLiveCastHomeworkProcessResultMap();
        Map<String, NewQuestion> allNewQuestionMap = liveCastReportRateContext.getAllNewQuestionMap();
        Map<Integer, NewContentType> contentTypeMap = liveCastReportRateContext.getContentTypeMap();
        ObjectiveConfigType type = liveCastReportRateContext.getType();
        NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastHomeworkResult.getPractices().get(type);
        LinkedHashMap<String, String> qidToProcessIdMap = newHomeworkResultAnswer.processAnswers();
        List<LiveCastHomeworkProcessResult> processResultList = qidToProcessIdMap.values()
                .stream()
                .filter(liveCastHomeworkProcessResultMap::containsKey)
                .map(liveCastHomeworkProcessResultMap::get)
                .collect(Collectors.toList());
        List<LiveCastCommonData> liveCastCommonDatas = newInternalProcessHomeworkAnswerDetailToPerson(type,
                liveCastReportRateContext.getLiveCastHomework(),
                allNewQuestionMap,
                contentTypeMap,
                processResultList);
        if (CollectionUtils.isNotEmpty(liveCastCommonDatas)) {
            liveCastReportRateContext.getResultMap().put(type, liveCastCommonDatas);
        }
    }

    // 优化接口，等待数据测试
    private List<LiveCastCommonData> newInternalProcessHomeworkAnswerDetailToPerson(ObjectiveConfigType type,
                                                                                    LiveCastHomework liveCastHomework,
                                                                                    Map<String, NewQuestion> allNewQuestionMap,
                                                                                    Map<Integer, NewContentType> contentTypeMap,
                                                                                    List<LiveCastHomeworkProcessResult> processResultList) {
        NewHomeworkPracticeContent target = liveCastHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (target == null)
            return Collections.emptyList();
        List<NewHomeworkQuestion> questions = target.processNewHomeworkQuestion(false);
        if (CollectionUtils.isEmpty(questions))
            return Collections.emptyList();
        Map<String, LiveCastCommonData> liveCastCommonDataMap = new LinkedHashMap<>();
        for (NewHomeworkQuestion newHomeworkQuestion : questions) {
            NewQuestion question = allNewQuestionMap.get(newHomeworkQuestion.getQuestionId());
            if (question == null) continue;
            LiveCastCommonData liveCastCommonData = new LiveCastCommonData();
            liveCastCommonData.setQid(newHomeworkQuestion.getQuestionId());
            if (contentTypeMap.containsKey(question.getContentTypeId())) {
                liveCastCommonData.setContentType(contentTypeMap.get(question.getContentTypeId()).getName());
            }
            int showType = 0;
            List<Integer> submitWays = question.getSubmitWays() != null ?
                    question.getSubmitWays()
                            .stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()) :
                    Collections.emptyList();
            if (CollectionUtils.isNotEmpty(submitWays)
                    && submitWays.contains(1)) {
                showType = 1;
            } else if (CollectionUtils.isNotEmpty(submitWays)
                    && submitWays.contains(2)) {
                showType = 2;
            }
            liveCastCommonData.setShowType(showType);
            liveCastCommonData.setDifficultyInt(question.getDifficultyInt());
            liveCastCommonDataMap.put(newHomeworkQuestion.getQuestionId(), liveCastCommonData);
        }
        for (LiveCastHomeworkProcessResult liveCastHomeworkProcessResult : processResultList) {
            if (!liveCastCommonDataMap.containsKey(liveCastHomeworkProcessResult.getQuestionId()))
                continue;
            NewQuestion question = allNewQuestionMap.get(liveCastHomeworkProcessResult.getQuestionId());
            List<NewQuestionsSubContents> subContents = question.getContent().getSubContents();
            List<List<String>> standardAnswers = subContents
                    .stream()
                    .map(NewQuestionsSubContents::getAnswerList)
                    .collect(Collectors.toList());
            LiveCastCommonData liveCastCommonData = liveCastCommonDataMap.get(liveCastHomeworkProcessResult.getQuestionId());
            liveCastCommonData.setStandardAnswers(NewHomeworkUtils.pressAnswer(subContents, standardAnswers));
            liveCastCommonData.setSubGrasp(liveCastHomeworkProcessResult.getSubGrasp());
            liveCastCommonData.setGrasp(liveCastHomeworkProcessResult.getGrasp());
            liveCastCommonData.setFlag(true);
            liveCastCommonData.setCorrectionImg(liveCastHomeworkProcessResult.getCorrectionImg());
            liveCastCommonData.setUserAnswers(NewHomeworkUtils.pressAnswer(subContents, liveCastHomeworkProcessResult.getUserAnswers()));
        }
        List<LiveCastCommonData> result = new LinkedList<>();
        for (Map.Entry<String, LiveCastCommonData> entry : liveCastCommonDataMap.entrySet()) {
            LiveCastCommonData value = entry.getValue();
            if (value.isFlag()) {
                result.add(entry.getValue());
            }
        }
        return result;
    }
}
