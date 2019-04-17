package com.voxlearning.utopia.service.newhomework.impl.template.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.content.api.entity.ChineseSentence;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecitewithscore.ParagraphDetailed;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecitewithscore.ReadReciteWithScoreBasicData;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecitewithscore.ReadReciteWithScoreData;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.vacationhomework.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 课文读背(打分)
 * @author: Mr_VanGogh
 * @date: 2018/6/2 下午5:03
 */
@Named
public class VacationProcessNewHomeworkAnswerDetailReadReciteWithScoreTemplate extends VacationProcessNewHomeworkAnswerDetailTemplate {

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.READ_RECITE_WITH_SCORE;
    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportRateContext reportRateContext) {
        ReadReciteWithScoreData readReciteData = new ReadReciteWithScoreData();
        ObjectiveConfigType type = reportRateContext.getType();
        Map<String, ReadReciteWithScoreBasicData> readReciteBasicDataMap = new HashMap<>();
        NewHomeworkPracticeContent target = reportRateContext.getNewHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        handleChineseReadRecite(target, reportRateContext.getAllNewQuestionMap(), readReciteData, readReciteBasicDataMap);
        NewHomeworkResultAnswer newHomeworkResultAnswer = reportRateContext.getNewHomeworkResult().getPractices().get(type);

        for (String questionBoxId : newHomeworkResultAnswer.getAppAnswers().keySet()) {
            if (!readReciteBasicDataMap.containsKey(questionBoxId)) {
                continue;
            }
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(questionBoxId);
            ReadReciteWithScoreBasicData readReciteWithScoreBasicData = readReciteBasicDataMap.get(questionBoxId);
            LinkedHashMap<String, String> answers = newHomeworkResultAppAnswer.getAnswers();
            if (MapUtils.isEmpty(answers)) {
                continue;
            }
            double value = new BigDecimal(SafeConverter.toInt(newHomeworkResultAppAnswer.getStandardNum()) * 100).divide(new BigDecimal(newHomeworkResultAppAnswer.getAnswers().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (value >= NewHomeworkConstants.READ_RECITE_STANDARD) {
                readReciteWithScoreBasicData.setStandard(true);
            }
            for (ParagraphDetailed paragraphDetailed : readReciteWithScoreBasicData.getParagraphDetails()) {
                if (!answers.containsKey(paragraphDetailed.getQuestionId())) {
                    continue;
                }
                String pid = answers.get(paragraphDetailed.getQuestionId());
                if (!reportRateContext.getNewHomeworkProcessResultMap().containsKey(pid)) {
                    continue;
                }
                VacationHomeworkProcessResult vacationHomeworkProcessResult = reportRateContext.getNewHomeworkProcessResultMap().get(pid);
                List<String> voices = CollectionUtils.isNotEmpty(vacationHomeworkProcessResult.getOralDetails()) ?
                        vacationHomeworkProcessResult
                                .getOralDetails()
                                .stream()
                                .flatMap(Collection::stream)
                                .map(o -> VoiceEngineTypeUtils.getAudioUrl(o.getAudio(), vacationHomeworkProcessResult.getVoiceEngineType()))
                                .collect(Collectors.toList()) :
                        Collections.emptyList();
                int duration = new BigDecimal(SafeConverter.toLong(vacationHomeworkProcessResult.getDuration())).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_HALF_UP).intValue();
                String durationStr = NewHomeworkUtils.handlerEnTime(duration);
                paragraphDetailed.setVoices(voices);
                paragraphDetailed.setStandard(SafeConverter.toBoolean(vacationHomeworkProcessResult.grasp));
                paragraphDetailed.setDuration(durationStr);
                readReciteWithScoreBasicData.getVoices().addAll(voices);
            }
        }
        reportRateContext.getResultMap().put(type, readReciteData);
    }

    public void handleChineseReadRecite(NewHomeworkPracticeContent target, Map<String, NewQuestion> allNewQuestionMap, ReadReciteWithScoreData readReciteData, Map<String, ReadReciteWithScoreBasicData> readReciteBasicDataMap) {
        List<NewHomeworkQuestion> questionList = target.processNewHomeworkQuestion(false);

        //key ==》自然段段落编号
        Map<String, Integer> qidToParagraph = new LinkedHashMap<>();
        Map<String, Boolean> qidToDiffMap = new LinkedHashMap<>();
        List<Long> chineseSentenceIds = questionList
                .stream()
                .filter(o -> allNewQuestionMap.containsKey(o.getQuestionId()))
                .map(o -> allNewQuestionMap.get(o.getQuestionId()))
                .filter(Objects::nonNull)
                .filter(question -> CollectionUtils.isNotEmpty(question.getSentenceIds()))
                .map(o -> o.getSentenceIds().get(0))
                .collect(Collectors.toList());

        List<ChineseSentence> chineseSentences = chineseContentLoaderClient.loadChineseSentenceByIds(chineseSentenceIds);
        if (CollectionUtils.isNotEmpty(chineseSentences)) {
            Map<Long, ChineseSentence> mapChineseSentences = chineseSentences.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(AbstractDatabaseEntity::getId, Function.identity()));
            for (NewHomeworkQuestion question : questionList) {
                if (!allNewQuestionMap.containsKey(question.getQuestionId()))
                    continue;
                NewQuestion newQuestion = allNewQuestionMap.get(question.getQuestionId());
                if (CollectionUtils.isEmpty(newQuestion.getSentenceIds()))
                    continue;
                Long sentenceId = newQuestion.getSentenceIds().get(0);
                if (!mapChineseSentences.containsKey(sentenceId))
                    continue;
                ChineseSentence chineseSentence = mapChineseSentences.get(sentenceId);
                qidToDiffMap.put(question.getQuestionId(), chineseSentence.getReciteParagraph());
                qidToParagraph.put(question.getQuestionId(), chineseSentence.getParagraph());
            }
        }
        List<String> lessonIds = target.getApps()
                .stream()
                .filter(Objects::nonNull)
                .filter(o -> Objects.nonNull(o.getLessonId()))
                .map(NewHomeworkApp::getLessonId)
                .collect(Collectors.toList());
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        for (NewHomeworkApp newHomeworkApp : target.getApps()) {
            ReadReciteWithScoreBasicData readReciteBasicData = new ReadReciteWithScoreBasicData();
            readReciteBasicData.setQuestionBoxId(newHomeworkApp.getQuestionBoxId());
            if (newBookCatalogMap.containsKey(newHomeworkApp.getLessonId())) {
                //兼容数据Alias和name
                NewBookCatalog newBookCatalog = newBookCatalogMap.get(newHomeworkApp.getLessonId());
                String lessonName = newBookCatalog.getAlias();
                if (StringUtils.isBlank(lessonName)) {
                    lessonName = newBookCatalog.getName();
                }
                readReciteBasicData.setLessonName(lessonName);
            }
            List<NewHomeworkQuestion> questions = newHomeworkApp.getQuestions();

            //包里面的各个自然段
            for (NewHomeworkQuestion newHomeworkQuestion : questions) {
                //一个自然段
                ParagraphDetailed paragraphDetailed = new ParagraphDetailed();
                //各个字段段
                readReciteBasicData.getParagraphDetails().add(paragraphDetailed);
                paragraphDetailed.setQuestionId(newHomeworkQuestion.getQuestionId());
                paragraphDetailed.setParagraphOrder(SafeConverter.toInt(qidToParagraph.get(newHomeworkQuestion.getQuestionId())));
                paragraphDetailed.setParagraphDifficultyType(SafeConverter.toBoolean(qidToDiffMap.get(newHomeworkQuestion.getQuestionId())));
            }
            if (newHomeworkApp.getQuestionBoxType() == QuestionBoxType.READ) {
                readReciteData.getReadData().add(readReciteBasicData);
            } else {
                readReciteData.getReciteData().add(readReciteBasicData);
            }
            readReciteBasicDataMap.put(newHomeworkApp.getQuestionBoxId(), readReciteBasicData);
        }
    }

}
