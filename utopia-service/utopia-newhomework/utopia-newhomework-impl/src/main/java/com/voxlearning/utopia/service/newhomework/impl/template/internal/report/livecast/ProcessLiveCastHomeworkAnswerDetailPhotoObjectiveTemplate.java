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
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastSubjectiveQuestion;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.newhomework.impl.template.ProcessLiveCastHomeworkAnswerDetailTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class ProcessLiveCastHomeworkAnswerDetailPhotoObjectiveTemplate extends ProcessLiveCastHomeworkAnswerDetailTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.PHOTO_OBJECTIVE;
    }

    @Override
    public void processNewHomeworkAnswerDetail(LiveCastReportRateContext liveCastReportRateContext) {
        LiveCastHomework liveCastHomework = liveCastReportRateContext.getLiveCastHomework();
        ObjectiveConfigType type = liveCastReportRateContext.getType();
        NewHomeworkPracticeContent target = liveCastHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveCastReportRateContext.getLiveCastHomeworkResultMap();
        List<LiveCastHomeworkResult> liveCastHomeworkResults = liveCastHomeworkResultMap.values().stream()
                .filter(o -> Objects.nonNull(o.getPractices()))
                .filter(o -> o.getPractices().containsKey(type))
                .collect(Collectors.toList());
        List<NewHomeworkQuestion> questions = target.processNewHomeworkQuestion(true);
        int questionScore = 100 / questions.size();
        int totalNum = liveCastReportRateContext.getUserMap().size();
        Map<String, LiveCastSubjectiveQuestion> liveCastSubjectiveQuestionMap = questions.stream()
                .map(NewHomeworkQuestion::getQuestionId)
                .collect(Collectors.toMap(Function.identity(),
                        o -> {
                            LiveCastSubjectiveQuestion liveCastSubjectiveQuestion = new LiveCastSubjectiveQuestion();
                            liveCastSubjectiveQuestion.setQid(o);
                            liveCastSubjectiveQuestion.setQuestionScore(questionScore);
                            liveCastSubjectiveQuestion.setTotalNum(totalNum);
                            return liveCastSubjectiveQuestion;
                        }));
        for (LiveCastHomeworkResult liveCastHomeworkResult : liveCastHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastHomeworkResult.getPractices().get(type);
            if (newHomeworkResultAnswer != null) {
                LinkedHashMap<String, String> answers = newHomeworkResultAnswer.processAnswers();
                if (MapUtils.isNotEmpty(answers)) {
                    for (Map.Entry<String, String> entry : answers.entrySet()) {
                        LiveCastSubjectiveQuestion liveCastSubjectiveQuestion = liveCastSubjectiveQuestionMap.get(entry.getKey());
                        liveCastSubjectiveQuestion.setFinishedNum(liveCastSubjectiveQuestion.getFinishedNum() + 1);
                        liveCastSubjectiveQuestion.setFlag(true);
                    }
                }
            }
        }
        List<LiveCastSubjectiveQuestion> result = questions.stream()
                .map(NewHomeworkQuestion::getQuestionId)
                .filter(liveCastSubjectiveQuestionMap::containsKey)
                .map(liveCastSubjectiveQuestionMap::get)
                .filter(LiveCastSubjectiveQuestion::isFlag)
                .collect(Collectors.toList());
        liveCastReportRateContext.getResult().put(type.name(), result);
    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(LiveCastReportRateContext liveCastReportRateContext) {
        ObjectiveConfigType type = liveCastReportRateContext.getType();
        User user = liveCastReportRateContext.getUser();
        LiveCastHomeworkResult liveCastHomeworkResult = liveCastReportRateContext.getLiveCastHomeworkResult();
        Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap = liveCastReportRateContext.getLiveCastHomeworkProcessResultMap();
        LiveCastHomework liveCastHomework = liveCastReportRateContext.getLiveCastHomework();
        NewHomeworkPracticeContent target = liveCastHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        List<NewHomeworkQuestion> questions = target.processNewHomeworkQuestion(true);
        int questionScore = 100 / questions.size();
        if (liveCastHomeworkResult.isFinishedOfObjectiveConfigType(type) && MapUtils.isNotEmpty(liveCastHomeworkProcessResultMap)) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastHomeworkResult.getPractices().get(type);
            LinkedHashMap<String, String> answers = newHomeworkResultAnswer.processAnswers();
            Map<String, LiveCastSubjectiveQuestion> liveCastSubjectiveQuestionMap = questions
                    .stream()
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toMap(Function.identity(),
                            o -> {
                                LiveCastSubjectiveQuestion liveCastSubjectiveQuestion = new LiveCastSubjectiveQuestion();
                                liveCastSubjectiveQuestion.setQid(o);
                                liveCastSubjectiveQuestion.setQuestionScore(questionScore);
                                return liveCastSubjectiveQuestion;
                            }));
            List<LiveCastHomeworkProcessResult> liveCastHomeworkProcessResults = answers.values()
                    .stream()
                    .filter(liveCastHomeworkProcessResultMap::containsKey)
                    .map(liveCastHomeworkProcessResultMap::get)
                    .collect(Collectors.toList());
            for (LiveCastHomeworkProcessResult n : liveCastHomeworkProcessResults) {
                if (liveCastSubjectiveQuestionMap.containsKey(n.getQuestionId())) {
                    LiveCastSubjectiveQuestion liveCastSubjectiveQuestion = liveCastSubjectiveQuestionMap.get(n.getQuestionId());
                    LiveCastSubjectiveQuestion.StudentSubjectiveQuestionInfo studentSubjectiveQuestionInfo = new LiveCastSubjectiveQuestion.StudentSubjectiveQuestionInfo();
                    if (CollectionUtils.isNotEmpty(n.getFiles())) {
                        List<NewHomeworkQuestionFile> newHomeworkQuestionFiles = n.getFiles().get(0);
                        if (CollectionUtils.isNotEmpty(newHomeworkQuestionFiles)) {
                            for (NewHomeworkQuestionFile newHomeworkQuestionFile : newHomeworkQuestionFiles) {
                                studentSubjectiveQuestionInfo.getUseAnswerPicture().add(NewHomeworkQuestionFileHelper.getFileUrl(newHomeworkQuestionFile));
                            }
                        }
                    }
                    if (CollectionUtils.isEmpty(studentSubjectiveQuestionInfo.getUseAnswerPicture())) continue;
                    studentSubjectiveQuestionInfo.setProcessId(n.getId());
                    studentSubjectiveQuestionInfo.setUseId(n.getUserId());
                    studentSubjectiveQuestionInfo.setComment(n.getTeacherMark());
                    studentSubjectiveQuestionInfo.setUseName(user.fetchRealname());
                    studentSubjectiveQuestionInfo.setComment(n.getTeacherMark());
                    studentSubjectiveQuestionInfo.setScore(SafeConverter.toDouble(n.getScore()));
                    studentSubjectiveQuestionInfo.setPercentage(SafeConverter.toDouble(n.getPercentage()));
                    studentSubjectiveQuestionInfo.setCorrected(SafeConverter.toBoolean(n.getReview()));
                    if (StringUtils.isNotBlank(n.getCorrectionImg())) {
                        String[] split = n.getCorrectionImg().split(",");
                        for (String pic : split) {
                            studentSubjectiveQuestionInfo.getTeacherCorrectingPicture().add(pic);
                        }
                    }
                    String[] voices = StringUtils.split(n.getCorrectionVoice(), ",");
                    if(ArrayUtils.isNotEmpty(voices)){
                        studentSubjectiveQuestionInfo.setVoice(Arrays.asList(voices));
                    }
                    liveCastSubjectiveQuestion.setFlag(true);
                    liveCastSubjectiveQuestion.getStudentSubjectiveQuestionInfos().add(studentSubjectiveQuestionInfo);
                }
            }
            List<LiveCastSubjectiveQuestion> result = questions
                    .stream()
                    .map(NewHomeworkQuestion::getQuestionId)
                    .filter(liveCastSubjectiveQuestionMap::containsKey)
                    .map(liveCastSubjectiveQuestionMap::get)
                    .filter(LiveCastSubjectiveQuestion::isFlag)
                    .collect(Collectors.toList());
            liveCastReportRateContext.getResultMap().put(type, result);
        }
    }
}
