package com.voxlearning.utopia.service.newhomework.impl.service.processor.answer;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.AbilityExamAnswerContext;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.QuestionDataAnswer;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.QuestionResult;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 计算题目是否正确
 *
 * @author lei.liu
 * @version 18-11-1
 */
@Named
public class AbilityExamAnswer_CalculateScore extends AbstractAbilityExamAnswerChainProcessor {
    @Override
    protected void doProcess(AbilityExamAnswerContext context) {
        if (context.getAbilityExamBasic().fetchFinished()) {
            return;
        }
        Map<String, QuestionResult> scoreResultMap = new HashMap<>();
        QuestionDataAnswer userAnswer = context.getAnswer();

        String questionId = userAnswer.getQuestionId();
        UserAnswerMapper uam = new UserAnswerMapper(questionId, 100D, userAnswer.getAnswer(), true);
        QuestionScoreResult scoreResult = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);

        if (scoreResult == null) {
            // 打分错误上报
            NewQuestion newQuestion = questionLoaderClient.loadQuestionIncludeDisabled(questionId);
            Map<String, String> info = new HashMap<>();
            info.put("questionId", questionId);
            info.put("errorCode", ErrorCodeConstants.ERROR_CODE_QUESTION_SUBMIT_ANSWERS_ERROR);
            info.put("userId", SafeConverter.toString(context.getUserId()));
            info.put("objectiveConfigType", context.getType().name());
            info.put("userAnswer", JsonUtils.toJson(userAnswer.getAnswer()));
            if (newQuestion != null) {
                info.put("standardAnswer", JsonUtils.toJson(newQuestion.getAnswers()));
            }
            Mode mode = RuntimeMode.current();
            info.put("env", mode.name());
            LogCollector.info("question-error-log", info);

            logger.error("Cannot calculate NewQuestion {} score", questionId);

            scoreResultMap.put(questionId, new QuestionResult(questionId, -1D, true, new ArrayList<>()));
        } else {
            scoreResultMap.put(questionId, new QuestionResult(questionId, scoreResult.getTotalScore(), scoreResult.getIsRight(), scoreResult.getSubScoreResults().stream().map(SubScoreResult::getIsRight).collect(Collectors.toList())));
        }

        context.setQuestionResultMap(scoreResultMap);

    }
}
