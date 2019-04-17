package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.impl.loader.TikuStrategy;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.question.consumer.QuestionContentTypeLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by tanguohong on 2016/3/10.
 */
@Named
public class ER_CalculateScore extends SpringContainerSupport implements NewExamResultTask {

    @Inject
    private QuestionContentTypeLoaderClient questionContentTypeLoaderClient;
    @Inject
    private TikuStrategy tikuStrategy;

    @Override
    public void execute(NewExamResultContext context) {
        if (context.getDurationMilliseconds() == null) {
            logger.error("How long does student {} spend on newQuestion {} ???", context.getUserId(), context.getQuestionId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_DURATION);
            return;
        }

        NewQuestion newquestion = tikuStrategy.loadQuestionIncludeDisabled(context.getQuestionId());
        QuestionScoreResult scoreResult = new QuestionScoreResult();

        if (CollectionUtils.isNotEmpty(context.getOralScoreDetails())) {
            //根据试卷获取对于的题，然后获取对于小题的阔度分
            NewPaper newPaper = context.getNewPaper();
            if (newPaper != null && CollectionUtils.isNotEmpty(newPaper.getQuestions())) {
                NewPaperQuestion target = null;
                for (NewPaperQuestion newPaperQuestion : newPaper.getQuestions()) {
                    if (Objects.equals(newPaperQuestion.getId(), context.getQuestionId())) {
                        target = newPaperQuestion;
                        break;
                    }
                }
                if (target != null) {
                    //做保护措施
                    //只有在有数值，而且不为零的情况下去设置新的跨度值，
                    double intervalScore = SafeConverter.toDouble(target.getIntervalScore());
                    if (intervalScore > 0) {
                        context.setIntervalScore(intervalScore);
                    }
                }
            }
            NewExam newExam = context.getNewExam();
            if (newExam != null) {
                context.setOralGradeType(SafeConverter.toInt(newExam.getOralGradeType()));
            }
            int oralScoreDetailsSize = context.getOralScoreDetails().size();
            int questionSubContentSize = newquestion.getContent().getSubContents().size();
            if (oralScoreDetailsSize != questionSubContentSize) {
                LogCollector.info("backend-general", MapUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getUserId(),
                        "mod1", context.getNewExamId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_ORAL_SCORE_DETAILS_ERROR,
                        "mod3", JsonUtils.toJson(context.getOralScoreDetails()),
                        "mod4", context.getQuestionId(),
                        "op", "student exam result"
                ));
                logger.error("OralScoreDetails size {} not equal question subContent size {}", oralScoreDetailsSize, questionSubContentSize);
                context.errorResponse("口语题答案数量错误");
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_ORAL_SCORE_DETAILS_ERROR);
                return;
            }
            List<Double> subScoreList = context.calculateStudentOralScore();
            Double stScore = subScoreList.stream().mapToDouble(SafeConverter::toDouble).sum();

            scoreResult.setIsRight(true);
            scoreResult.setQuestionId(context.getQuestionId());

            List<SubScoreResult> subScoreResults = new ArrayList<>();
            for (int i = 0; i < oralScoreDetailsSize; i++) {
                SubScoreResult subScoreResult = new SubScoreResult();
                subScoreResult.setUserAnswer(Collections.singletonList(""));
                subScoreResult.setStandardAnswer(Collections.singletonList(""));
                subScoreResult.setIsRight(Collections.singletonList(true));
                subScoreResult.setScore(subScoreList.get(i));
                subScoreResults.add(subScoreResult);
            }
            scoreResult.setSubScoreResults(subScoreResults);


            scoreResult.setTotalScore(stScore);
        } else {
            //纯主观题无需算分
            if (newquestion == null || questionContentTypeLoaderClient.isSubjective(newquestion.findSubContentTypeIds())) {
                scoreResult.setIsRight(false);
                scoreResult.setQuestionId(context.getQuestionId());
                SubScoreResult subScoreResult = new SubScoreResult();
                subScoreResult.setUserAnswer(Collections.singletonList(""));
                subScoreResult.setStandardAnswer(Collections.singletonList(""));
                subScoreResult.setIsRight(Collections.singletonList(true));
                scoreResult.setSubScoreResults(Collections.singletonList(subScoreResult));
            } else {
                UserAnswerMapper uam = new UserAnswerMapper(context.getQuestionId(), context.getStandardScore(), context.getAnswer(), true);
                // 下面是为了输出日志用的
                uam.setUserAgent(context.getUserAgent());
                uam.setUserId(context.getUserId());
                uam.setHomeworkId(context.getNewExamId());
                uam.setHomeworkType(StudyType.examination.name());

                scoreResult = tikuStrategy.loadQuestionScoreResult(uam, context.getQuestionId(), null);
                if (scoreResult == null) {
                    logger.error("Cannot calculate newQuestion {} score", context.getQuestionId());
                    context.errorResponse();
                    context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CALCULATE_SCORE);
                    return;
                }

                // 脱式计算题，重新赋值userAnswer
                List<NewQuestionsSubContents> subContents = newquestion.getContent().getSubContents();
                for (int i = 0; i < subContents.size(); i++) {
                    NewQuestionsSubContents subContent = subContents.get(i);
                    Integer subContentTypeId = subContent.getSubContentTypeId();
                    if (Objects.equals(QuestionConstants.TuoShiJiSuanTi, subContentTypeId)) {
                        context.getAnswer().set(i, scoreResult.getSubScoreResults().get(i).getUserAnswer());
                    }
                }
            }
        }

        context.setScoreResult(scoreResult);
        scoreResult.getSubScoreResults().forEach(e -> {
            context.getSubScore().add(e.getScore());
            context.getStandardAnswer().add(e.getStandardAnswer());
            context.getSubGrasp().add(e.getIsRight());
        });

        context.getResult().put(context.getQuestionId(),
                MiscUtils.m(
                        "fullScore", context.getStandardScore(),
                        "score", context.getScoreResult().getTotalScore(),
                        "answers", context.getStandardAnswer(),
                        "userAnswers", context.getAnswer(),
                        "subMaster", context.getSubGrasp(),
                        "subScore", context.getSubScore(),
                        "master", context.getScoreResult().getIsRight()));
    }
}
