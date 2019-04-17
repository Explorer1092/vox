package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.CalculateResult;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.Calculator;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.CalculatorManager;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/20
 */
@Named
public class FH_CalculateScoreAndDuration extends SpringContainerSupport implements FinishHomeworkTask {
    @Inject
    private CalculatorManager calculatorManager;
    @Inject
    private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;

    @Override
    public void execute(FinishHomeworkContext context) {
        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();
        if (ObjectiveConfigType.BASIC_APP.equals(objectiveConfigType)
                || ObjectiveConfigType.READING.equals(objectiveConfigType)
                || ObjectiveConfigType.LEVEL_READINGS.equals(objectiveConfigType)
                || ObjectiveConfigType.KEY_POINTS.equals(objectiveConfigType)
                || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.equals(objectiveConfigType)
                || ObjectiveConfigType.NATURAL_SPELLING.equals(objectiveConfigType)
                || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(objectiveConfigType)
                || ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(objectiveConfigType)
                || ObjectiveConfigType.ORAL_COMMUNICATION.equals(objectiveConfigType)
                || ObjectiveConfigType.DUBBING_WITH_SCORE.equals(objectiveConfigType)
                || ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.equals(objectiveConfigType)) {
            LinkedHashMap<String, NewHomeworkResultAppAnswer> nhraMap = context.getResult().getPractices().get(objectiveConfigType).getAppAnswers();
            double totalScore = 0d;
            Long totalDuration = 0L;
            for (NewHomeworkResultAppAnswer nhra : nhraMap.values()) {
                totalDuration += nhra.getDuration();
                totalScore += nhra.getScore();
            }
            double avgScore = new BigDecimal(totalScore).divide(new BigDecimal(nhraMap.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            context.setPracticeScore(avgScore);
            context.setPracticeDureation(totalDuration);
        } else if (ObjectiveConfigType.NEW_READ_RECITE.equals(objectiveConfigType)
                || ObjectiveConfigType.DUBBING.equals(objectiveConfigType)) {
            //新语文读背、趣味配音不再计算分数
            LinkedHashMap<String, NewHomeworkResultAppAnswer> nhraMap = context.getResult().getPractices().get(objectiveConfigType).getAppAnswers();
            Long totalDuration = 0L;
            for (NewHomeworkResultAppAnswer nhra : nhraMap.values()) {
                totalDuration += nhra.getDuration();
            }
            context.setPracticeDureation(totalDuration);
        } else if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.equals(objectiveConfigType)) {
            Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(context.getHomeworkId(), context.getOcrMentalAnswerIds());
            int questionCount = 0;
            int correctQuestionCount = 0;
            for (NewHomeworkProcessResult processResult : processResultMap.values()) {
                OcrMentalImageDetail ocrMentalImageDetail = processResult.getOcrMentalImageDetail();
                if (ocrMentalImageDetail != null && CollectionUtils.isNotEmpty(ocrMentalImageDetail.getForms())) {
                    for (OcrMentalImageDetail.Form form : ocrMentalImageDetail.getForms()) {
                        if (form.getJudge() < 2) {
                            questionCount++;
                            if (Objects.equals(1, form.getJudge())) {
                                correctQuestionCount++;
                            }
                        }
                    }
                }
            }
            double score = 0D;
            if (questionCount != 0) {
                score = new BigDecimal(correctQuestionCount * 100).divide(new BigDecimal(questionCount), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            context.setPracticeScore(score);
            context.setOcrMentalQuestionCount(questionCount);
            context.setOcrMentalCorrectQuestionCount(correctQuestionCount);
            context.setPracticeFinished(true);
        } else if (ObjectiveConfigType.OCR_DICTATION.equals(objectiveConfigType)) {
            NewHomeworkPracticeContent practiceContent = context.getHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(objectiveConfigType);
            if (practiceContent == null || CollectionUtils.isEmpty(practiceContent.getQuestions())) {
                return;
            }
            Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(context.getHomeworkId(), context.getOcrDictationAnswerIds());
            int questionCount = practiceContent.getQuestions().size();
            long correctQuestionCount = processResultMap.values().stream()
                    .filter(p -> p != null
                            && p.getOcrDictationImageDetail() != null
                            && CollectionUtils.isNotEmpty(p.getOcrDictationImageDetail().getForms())
                    )
                    .map(NewHomeworkProcessResult::getOcrDictationImageDetail)
                    .map(OcrMentalImageDetail::getForms)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .filter(f -> Objects.equals(1, f.getJudge()))
                    .filter(NewHomeworkUtils.distinctByKey(OcrMentalImageDetail.Form::getText))
                    .count();
            double score = 0D;
            if (questionCount != 0) {
                score = new BigDecimal(correctQuestionCount * 100).divide(new BigDecimal(questionCount), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            context.setPracticeScore(score);
            context.setOcrDictationQuestionCount(questionCount);
            context.setOcrDictationCorrectQuestionCount(SafeConverter.toInt(correctQuestionCount));
            context.setPracticeFinished(true);
        } else {
            NewHomeworkResultAnswer answer = context.getResult().getPractices().get(objectiveConfigType);
            Set<String> processIds = new HashSet<>(answer.getAnswers().values());
            // 计算分数和耗时
            Calculator calculator = calculatorManager.getCalculator(objectiveConfigType);
            if (calculator != null) {
                CalculateResult result = calculator.calculate(context.getHomeworkId(), processIds);
                if (result != null) {
                    context.setPracticeScore(result.getScore());
                    context.setPracticeDureation(result.getDuration());
                }
            }
        }
    }

}
