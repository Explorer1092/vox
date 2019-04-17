package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.typeresult;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkTypeResultProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/1/28
 * \* Time: 11:35 AM
 * \* Description:英语纸质听写
 * \
 */
@Named
public class NewHomeworkTypeResultProcessOcrDicTation extends NewHomeworkTypeResultProcessTemplate {
    @Override
    public NewHomeworkTypeResultProcessTemp getNewHomeworkTypeResultTemp() {
        return NewHomeworkTypeResultProcessTemp.OCR_DICTATION;
    }

    @Override
    public MapMessage processHomeworkTypeResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer newHomeworkResultAnswer = resultMap.get(objectiveConfigType);
        NewHomework newHomework = newHomeworkLoader.load(baseHomeworkResult.homeworkId);
        List<NewHomeworkQuestion> newHomeworkQuestions = newHomework.findNewHomeworkQuestions(objectiveConfigType);
        List<String> questionIds = newHomeworkQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList());
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.
                loadQuestionsIncludeDisabled(questionIds);
        List<String> lessionIds = newHomeworkQuestions.stream().map(NewHomeworkQuestion::getQuestionBoxId).distinct().collect(Collectors.toList());
        if (MapUtils.isEmpty(allQuestionMap) || CollectionUtils.isEmpty(lessionIds)) {
            return MapMessage.successMessage();
        }
        List<Long> choosedSentencesList = allQuestionMap.values().stream().map(NewQuestion::getSentenceIds)
                .flatMap(Collection::stream).collect(Collectors.toList());
        Map<String, List<Sentence>> lessonSentenceMap = newEnglishContentLoaderClient.loadEnglishLessonSentences(lessionIds);
        if (CollectionUtils.isEmpty(choosedSentencesList) || MapUtils.isEmpty(lessonSentenceMap)) {
            return MapMessage.successMessage();
        }
        List<Sentence> sentenceList = lessonSentenceMap.values().stream().flatMap(List::stream)
                .filter(s -> choosedSentencesList.contains(s.getId())).collect(Collectors.toList());
        Map<Long, Sentence> sentenceMap = sentenceList.stream().collect(Collectors.toMap(Sentence::getId, Function.identity()));
        List<String> ocrDictationAnswers = newHomeworkResultAnswer.getOcrDictationAnswers();
        Map<String, SubHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(ocrDictationAnswers);
        processResultMap = MapUtils.resort(processResultMap, ocrDictationAnswers);
        List<String> processIds = new ArrayList<>();
        List<String> correctedWords = Lists.newArrayList();
        List<OcrMentalImageDetail> ocrDicTationDetails = processResultMap.values().stream()
                .filter(p -> p != null
                        && p.getOcrDictationImageDetail() != null
                )
                .map(SubHomeworkProcessResult::getOcrDictationImageDetail)
                .collect(Collectors.toList());
        processResultMap.values().stream()
                .filter(p -> p != null
                        && p.getOcrDictationImageDetail() != null
                        && CollectionUtils.isNotEmpty(p.getOcrDictationImageDetail().getForms())
                )
                .map(SubHomeworkProcessResult::getOcrDictationImageDetail)
                .map(OcrMentalImageDetail::getForms)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .filter(f -> Objects.equals(1, f.getJudge()))
                .filter(NewHomeworkUtils.distinctByKey(OcrMentalImageDetail.Form::getText))
                .forEach(f -> correctedWords.add(f.getText()));
        List<Map> datas = Lists.newArrayList();
        choosedSentencesList.forEach(s -> {
            Sentence sentence = sentenceMap.get(s);
            if (sentence != null) {
                datas.add(MapUtils.m("questionAnswers", sentence.getEnText(), "right", correctedWords.contains(sentence.getEnText())));
            }
        });
        return MapMessage.successMessage()
                .add("results", ocrDicTationDetails)
                .add("processIds", processIds)
                .add("questionCount", newHomeworkResultAnswer.getOcrDictationQuestionCount())
                .add("errorQuestionCount", newHomeworkResultAnswer.getOcrDictationQuestionCount() - newHomeworkResultAnswer.getOcrDictationCorrectQuestionCount())
                .add("correctCount", newHomeworkResultAnswer.getOcrDictationCorrectQuestionCount())
                .add("datas", datas);

    }
}
