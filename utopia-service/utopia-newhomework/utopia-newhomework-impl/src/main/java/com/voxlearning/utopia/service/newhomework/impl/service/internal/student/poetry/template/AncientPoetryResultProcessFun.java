package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry.template;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMissionResult;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryProcessResult;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.question.consumer.ScoreCalculationLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@Named
public class AncientPoetryResultProcessFun extends AncientPoetryResultProcessTemplate {
    @Inject
    private ScoreCalculationLoaderClient scoreCalculationLoaderClient;
    @Inject
    private QuestionLoaderClient questionLoaderClient;

    @Override
    public ModelType getProcessResultModel() {
        return ModelType.FUN;
    }

    @Override
    public void processResult(AncientPoetryProcessContext context) {
        calculateScore(context);
        funProcessResult(context);
    }

    private void calculateScore(AncientPoetryProcessContext context) {
        if (context.getQuestionId() == null || context.getAnswer() == null) {
            context.terminateTask();
            logger.error("AncientPoetryResultProcessFun params does mot contains questionId or answers, studentId:{}, activityId", context.getStudentId(), context.getActivityId());
            return;
        }

        NewQuestion newQuestion = questionLoaderClient.loadQuestion(context.getQuestionId());
        if (newQuestion == null) {
            context.terminateTask();
            logger.error("AncientPoetryResultProcessFun newQuestion does mot exist. questionId:{}", context.getQuestionId());
            return;
        }
        context.setDocId(newQuestion.getDocId());
        build(context);
    }

    private void build(AncientPoetryProcessContext context) {

        String questionId = context.getQuestionId();
        UserAnswerMapper uam = new UserAnswerMapper(questionId, 100D, context.getAnswer());
        // 下面是为了输出日志用的
        uam.setUserAgent(context.getUserAgent());
        uam.setUserId(context.getStudentId());
        uam.setHomeworkId(context.getActivityId());
        uam.setHomeworkType(context.getModelType().name());
        QuestionScoreResult scoreResult = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
        if (scoreResult == null) {
            logger.error("Cannot calculate newQuestion {} score", questionId);
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CALCULATE_SCORE);
            return;
        }
        List<List<String>> standardAnswer = new ArrayList<>();
        List<List<Boolean>> subGrasp = new ArrayList<>();
        scoreResult.getSubScoreResults().forEach(e -> {
            standardAnswer.add(e.getStandardAnswer());
            subGrasp.add(e.getIsRight());
        });
        context.setScoreResult(scoreResult);
        context.setStandardAnswer(standardAnswer);
        context.setSubGrasp(subGrasp);
        context.setGrasp(scoreResult.getIsRight());
    }

    private void funProcessResult(AncientPoetryProcessContext context) {
        // QuestionProcessResult
        AncientPoetryProcessResult.QuestionProcessResult processResult = new AncientPoetryProcessResult.QuestionProcessResult();
        processResult.setCreateAt(context.getCurrentDate());
        Boolean isRight = context.getGrasp();
        processResult.setGrasp(isRight);
        processResult.setSubGrasp(context.getSubGrasp());
        processResult.setAnswers(context.getAnswer());
        processResult.setDuration(NewHomeworkUtils.processDuration(context.getDurationMilliseconds()));
        processResult.setClientType(context.getClientType());
        processResult.setClientName(context.getClientName());

        // AncientPoetryProcessResult
        AncientPoetryMissionResult missionResult = context.getMissionResult();
        LinkedHashMap<String, AncientPoetryProcessResult> answers = missionResult.getAnswers() == null ? new LinkedHashMap<>() : missionResult.getAnswers();
        String processResultId = AncientPoetryProcessResult.generateId(context.getActivityId(), context.getMissionId(), context.getDocId(), context.getStudentId());
        AncientPoetryProcessResult baseProcessResult = answers.get(processResultId);
        if (!context.isCorrect()) {
            if (baseProcessResult != null) {
                context.errorResponse("不允许重复作答");
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
                return;
            }
            baseProcessResult = new AncientPoetryProcessResult();
            baseProcessResult.setId(processResultId);
            baseProcessResult.setDocId(context.getDocId());
            baseProcessResult.setQuestionId(context.getQuestionId());
            baseProcessResult.setProcessResult(processResult);
            context.setAddStar(isRight ? 1D : 0D);
            context.setAddDuration(processResult.getDuration());
            missionResult.setStar(SafeConverter.toDouble(missionResult.getStar()) + context.getAddStar());
        } else {
            if (baseProcessResult == null) {
                context.errorResponse("还未作答");
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK_PROCESS_RESULT);
                return;
            }
            if (baseProcessResult.getCorrectProcessResult() != null) {
                context.errorResponse("该题已订正");
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
                return;
            }
            processResult.setParentId(context.getParentId());// 完成此题订正的家长ID
            baseProcessResult.setCorrectProcessResult(processResult);
            context.setAddStar(isRight ? 0.5D : 0D);
            missionResult.setStar(SafeConverter.toDouble(missionResult.getStar()) + context.getAddStar());
        }
        context.getResult().put(context.getQuestionId(), MapUtils.m(
                "answers", context.getStandardAnswer(),
                "userAnswers", context.getAnswer(),
                "subMaster", context.getSubGrasp(),
                "master", isRight));

        if (MapUtils.isEmpty(missionResult.getAnswers())) {
            missionResult.setAnswers(new LinkedHashMap<>());
        }
        missionResult.getAnswers().put(processResultId, baseProcessResult);
        int funQuestionSize = context.getMission().getModels().get(ModelType.FUN).getFunContent().getQuestionIds().size();
        if (missionResult.getAnswers().size() == funQuestionSize && !context.isCorrect()) { // 所有题都做完&&不是订正错题
            List<Date> modelFinishAt = missionResult.getModelFinishAt();
            if (modelFinishAt.size() == 3) {
                modelFinishAt.add(context.getCurrentDate());
            }
            missionResult.setFinishAt(context.getCurrentDate());
        }
    }
}
