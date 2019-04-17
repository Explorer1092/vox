package com.voxlearning.utopia.service.newhomework.impl.template.internal.report.livecast;

import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastSubjectiveQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkReportContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.newhomework.impl.template.StatisticsToObjectiveConfigTypeTemple;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class StatisticsToCommonTemple extends StatisticsToObjectiveConfigTypeTemple {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.COMMON;
    }

    @Inject
    private ProcessLiveCastHomeworkAnswerDetailCommonTemplate processLiveCastHomeworkAnswerDetailCommonTemplate;

    //1、初始化数据：liveCastHomeworkResultMap、userMap、liveCastHomeworkProcessResultMap、NewHomeworkPracticeContent、questions
    //2、设置全部人数和类型信息
    //3、对liveCastHomeworkResultMap进行循环统计
    //3->统计类型完成人数、时间、分数、每题做题情况
    //4、数据归整
    @Override
    public LiveHomeworkReport.StatisticsToObjectiveConfigType statisticsToObjectiveConfigType(LiveHomeworkReportContext liveHomeworkReportContext, ObjectiveConfigType type) {
        LiveHomeworkReport.StatisticsToObjectiveConfigType statisticsToObjectiveConfigType = new LiveHomeworkReport.StatisticsToObjectiveConfigType();
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveHomeworkReportContext.getLiveCastHomeworkResultMap();
        Map<Long, User> userMap = liveHomeworkReportContext.getUserMap();
        Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap = liveHomeworkReportContext.getLiveCastHomeworkProcessResultMap();
        LiveCastHomework liveCastHomework = liveHomeworkReportContext.getLiveCastHomework();
        NewHomeworkPracticeContent target = liveCastHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        List<NewHomeworkQuestion> questions = target.processNewHomeworkQuestion(false);
        Map<String, NewQuestion> newQuestionMap = liveHomeworkReportContext.getNewQuestionMap();
        List<LiveHomeworkReport.StatisticsToNewQuestion> statisticsToNewQuestions
                = questions.stream()
                .filter(o -> newQuestionMap.containsKey(o.getQuestionId()))
                .map(o -> {
                    LiveHomeworkReport.StatisticsToNewQuestion statisticsToNewQuestion = new LiveHomeworkReport.StatisticsToNewQuestion();
                    statisticsToNewQuestion.setQuestionId(o.getQuestionId());
                    NewQuestion question = newQuestionMap.get(o.getQuestionId());
                    if (contentTypeMap.containsKey(question.getContentTypeId())) {
                        String content = contentTypeMap.get(question.getContentTypeId()).getName();
                        statisticsToNewQuestion.setContentType(content);
                    }
                    statisticsToNewQuestion.setDifficultyInt(question.getDifficultyInt());
                    if (contentTypeMap.containsKey(question.getContentTypeId())) {
                        statisticsToNewQuestion.setContentType(contentTypeMap.get(question.getContentTypeId()).getName());
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
                    statisticsToNewQuestion.setShowType(showType);
                    statisticsToNewQuestion.setDifficultyInt(question.getDifficultyInt());
                    return statisticsToNewQuestion;
                })
                .collect(Collectors.toList());

        statisticsToObjectiveConfigType.setTotalStudentNum(userMap.size());
        statisticsToObjectiveConfigType.setObjectiveConfigType(type);
        statisticsToObjectiveConfigType.setObjectiveConfigTypeName(type.name());

        int totalScoreToObjectiveConfigType = 0;
        int totalDurationToObjectiveConfigType = 0;
        int questionScore = 100 / target.processNewHomeworkQuestion(true).size();


        for (LiveCastHomeworkResult liveCastHomeworkResult : liveCastHomeworkResultMap.values()) {
            if (liveCastHomeworkResult.getPractices() == null) continue;
            if (liveCastHomeworkResult.isFinishedOfObjectiveConfigType(type)) {
                statisticsToObjectiveConfigType.setObjectiveConfigTypeFinishedNum(statisticsToObjectiveConfigType.getObjectiveConfigTypeFinishedNum() + 1);
                NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastHomeworkResult.getPractices().get(type);
                totalScoreToObjectiveConfigType += SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
                totalDurationToObjectiveConfigType += SafeConverter.toInt(newHomeworkResultAnswer.processDuration());
            }
            NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastHomeworkResult.getPractices().get(type);
            if (newHomeworkResultAnswer != null && MapUtils.isNotEmpty(newHomeworkResultAnswer.processAnswers())) {
                LinkedHashMap<String, String> qidToProcessId = newHomeworkResultAnswer.processAnswers();
                if (MapUtils.isNotEmpty(qidToProcessId)) {
                    // 处理每一题答题确准率的统计
                    for (LiveHomeworkReport.StatisticsToNewQuestion statisticsToNewQuestion : statisticsToNewQuestions) {
                        if (qidToProcessId.containsKey(statisticsToNewQuestion.getQuestionId()) && newQuestionMap.containsKey(statisticsToNewQuestion.getQuestionId())) {
                            if (type.isSubjective()) {
                                String processId = qidToProcessId.get(statisticsToNewQuestion.getQuestionId());
                                if (liveCastHomeworkProcessResultMap.containsKey(processId)) {
                                    LiveCastHomeworkProcessResult liveCastHomeworkProcessResult = liveCastHomeworkProcessResultMap.get(processId);
                                    if (liveCastHomeworkProcessResult != null) {
                                        LiveCastSubjectiveQuestion.StudentSubjectiveQuestionInfo studentSubjectiveQuestionInfo = new LiveCastSubjectiveQuestion.StudentSubjectiveQuestionInfo();
                                        if (CollectionUtils.isNotEmpty(liveCastHomeworkProcessResult.getFiles())) {
                                            List<NewHomeworkQuestionFile> newHomeworkQuestionFiles = liveCastHomeworkProcessResult.getFiles().get(0);
                                            if (CollectionUtils.isNotEmpty(newHomeworkQuestionFiles)) {
                                                for (NewHomeworkQuestionFile newHomeworkQuestionFile : newHomeworkQuestionFiles) {
                                                    studentSubjectiveQuestionInfo.getUseAnswerPicture().add(NewHomeworkQuestionFileHelper.getFileUrl(newHomeworkQuestionFile));
                                                }
                                            }
                                        }
                                        if (CollectionUtils.isEmpty(studentSubjectiveQuestionInfo.getUseAnswerPicture()))
                                            return null;
                                        studentSubjectiveQuestionInfo.setProcessId(liveCastHomeworkProcessResult.getId());
                                        studentSubjectiveQuestionInfo.setUseId(liveCastHomeworkProcessResult.getUserId());
                                        studentSubjectiveQuestionInfo.setComment(liveCastHomeworkProcessResult.getTeacherMark());
                                        studentSubjectiveQuestionInfo.setUseName(userMap.containsKey(liveCastHomeworkProcessResult.getUserId()) ? userMap.get(liveCastHomeworkProcessResult.getUserId()).fetchRealname() : "");
                                        studentSubjectiveQuestionInfo.setScore(SafeConverter.toDouble(liveCastHomeworkProcessResult.getScore()));
                                        studentSubjectiveQuestionInfo.setPercentage(SafeConverter.toDouble(liveCastHomeworkProcessResult.getPercentage()));
                                        studentSubjectiveQuestionInfo.setCorrected(SafeConverter.toBoolean(liveCastHomeworkProcessResult.getReview()));
                                        if (StringUtils.isNotBlank(liveCastHomeworkProcessResult.getCorrectionImg())) {
                                            String[] split = liveCastHomeworkProcessResult.getCorrectionImg().split(",");
                                            for (String pic : split) {
                                                studentSubjectiveQuestionInfo.getTeacherCorrectingPicture().add(pic);
                                            }
                                        }
                                        String[] voices = StringUtils.split(liveCastHomeworkProcessResult.getCorrectionVoice(), ",");
                                        if(ArrayUtils.isNotEmpty(voices)){
                                            studentSubjectiveQuestionInfo.setVoice(Arrays.asList(voices));
                                        }
                                        statisticsToNewQuestion.setQuestionScore(questionScore);
                                        statisticsToNewQuestion.getSubjectiveQuestionInfos().add(studentSubjectiveQuestionInfo);
                                    }
                                }
                            } else {
                                String processId = qidToProcessId.get(statisticsToNewQuestion.getQuestionId());
                                if (liveCastHomeworkProcessResultMap.containsKey(processId)) {
                                    LiveCastHomeworkProcessResult liveCastHomeworkProcessResult = liveCastHomeworkProcessResultMap.get(processId);
                                    if (liveCastHomeworkProcessResult != null) {
                                        NewQuestion question = newQuestionMap.get(statisticsToNewQuestion.getQuestionId());
                                        List<NewQuestionsSubContents> newQuestionsSubContent = question.getContent() != null ? question.getContent().getSubContents() : Collections.emptyList();
                                        String answer = processLiveCastHomeworkAnswerDetailCommonTemplate.pressHomeworkAnswer(newQuestionsSubContent, liveCastHomeworkProcessResult);
                                        if (SafeConverter.toBoolean(liveCastHomeworkProcessResult.getGrasp())) {
                                            statisticsToNewQuestion.setRightNum(statisticsToNewQuestion.getRightNum() + 1);
                                        }
                                        LiveHomeworkReport.StatisticsToNewQuestionAnswer newQuestionAnswer = new LiveHomeworkReport.StatisticsToNewQuestionAnswer();
                                        newQuestionAnswer.setUserId(liveCastHomeworkProcessResult.getUserId());
                                        newQuestionAnswer.setUserName(userMap.containsKey(liveCastHomeworkProcessResult.getUserId()) ? userMap.get(liveCastHomeworkProcessResult.getUserId()).fetchRealname() : "");
                                        statisticsToNewQuestion.getAnswer().computeIfAbsent(answer, k -> new LinkedList<>()).add(newQuestionAnswer);
                                        statisticsToNewQuestion.setTotalNum(statisticsToNewQuestion.getTotalNum() + 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (statisticsToObjectiveConfigType.getObjectiveConfigTypeFinishedNum() != 0) {
            statisticsToObjectiveConfigType.setClazzAverScoreToObjectiveConfig(new BigDecimal(totalScoreToObjectiveConfigType).divide(new BigDecimal(statisticsToObjectiveConfigType.getObjectiveConfigTypeFinishedNum()), BigDecimal.ROUND_HALF_UP).intValue());
            statisticsToObjectiveConfigType.setClazzAverDurationToObjectiveConfig(new BigDecimal(totalDurationToObjectiveConfigType).divide(new BigDecimal(60 * statisticsToObjectiveConfigType.getObjectiveConfigTypeFinishedNum()), BigDecimal.ROUND_HALF_UP).intValue());
        }
        statisticsToNewQuestions.forEach(o -> {
            if (o.getTotalNum() != 0) {
                o.setProportion(new BigDecimal(100 * o.getRightNum()).divide(new BigDecimal(o.getTotalNum()), BigDecimal.ROUND_HALF_UP).intValue());
            }
            List<Map<String, Object>> list = new LinkedList<>();
            o.setAnswerList(list);
            if (MapUtils.isNotEmpty(o.getAnswer())) {
                Map<String, List<LiveHomeworkReport.StatisticsToNewQuestionAnswer>> answer = o.getAnswer();
                if (o.getAnswer().containsKey("回答正确")) {
                    Map<String, Object> map = MapUtils.m("answer", "回答正确",
                            "users", answer.get("回答正确"));
                    answer.remove("回答正确");
                    list.add(map);
                }
                for (String key : answer.keySet()) {
                    Map<String, Object> map = MapUtils.m("answer", key,
                            "users", answer.get(key));
                    list.add(map);
                }
            }
            o.setAnswer(null);
        });

        statisticsToObjectiveConfigType.setStatisticsToNewQuestions(statisticsToNewQuestions);
        return statisticsToObjectiveConfigType;
    }
}
