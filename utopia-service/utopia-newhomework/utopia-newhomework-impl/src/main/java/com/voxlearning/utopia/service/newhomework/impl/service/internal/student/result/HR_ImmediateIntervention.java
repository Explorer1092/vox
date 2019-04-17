package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.ImmediateInterventionType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.InterventionPublisher;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.InterventionCommand;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.HOMEWORK_HINT_URL_PROD;
import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.HOMEWORK_HINT_URL_TEST;

/**
 * @Auther: majianxin
 * @Date: 2018/5/2
 * @Description: 即时干预
 */
@Named
public class HR_ImmediateIntervention extends SpringContainerSupport implements HomeworkResultTask {

    @Inject private InterventionPublisher interventionPublisher;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private StudentLoaderClient studentLoaderClient;

    @Override
    public void execute(HomeworkResultContext context) {

        //移动端才有即时干预
        if (context.getClientType() == null || !"mobile".equals(context.getClientType())) {
            return;
        }

        //不是即时干预作业类型
        if (!NewHomeworkConstants.IMMEDIATE_INTERVENTION_CONFIGTYPE.contains(context.getObjectiveConfigType())) {
            return;
        }

        if (MapUtils.isEmpty(context.getScoreResult())) {
            return;
        }
        QuestionScoreResult questionScoreResult = context.getScoreResult().values().iterator().next();

        //英语及时干预--英语重难点讲练测作业形式全走及时干预
        if (Subject.ENGLISH.equals(context.getSubject()) && ObjectiveConfigType.INTELLIGENT_TEACHING.equals(context.getObjectiveConfigType()) && context.getHintId() == null && !questionScoreResult.getIsRight()) {
            context.errorResponse("请认真看题目哦~");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_IMMEDIATE_INTERVENTION);
            context.setHintId(ImmediateInterventionType.READING.getHintId());
            context.setHintOptType(0);
//            context.setHintTag("");
            return;
        }

        //语文及时干预--字词讲练作业形式(字词训练)全走及时干预
        if (Subject.CHINESE.equals(context.getSubject())
                && !SafeConverter.toBoolean(context.getInterventionReSubmit())
                && !questionScoreResult.getIsRight()) {
            boolean hintIntervention = false;
            NewQuestion newQuestion = context.getUserAnswerQuestionMap().values().iterator().next();
            if (newQuestion != null) {
                // 判断大题上是否有干预信息
                if (CollectionUtils.isNotEmpty(newQuestion.getIntervene())) {
                    hintIntervention = true;
                }
                for (int index = 0; index < questionScoreResult.getSubScoreResults().size(); index++) {
                    NewQuestionsSubContents subContents = newQuestion.getContent().getSubContents().get(index);
                    SubScoreResult subScoreResult = questionScoreResult.getSubScoreResults().get(index);
                    if (subContents != null && CollectionUtils.isNotEmpty(subContents.getIntervene()) && subScoreResult != null && !SafeConverter.toBoolean(subScoreResult.getIsRight())) {
                        hintIntervention = true;
                        break;
                    }
                }
            }
            if (hintIntervention) {
                context.terminateTask();
                if (CollectionUtils.isNotEmpty(context.getStudentHomeworkAnswers())) {
                    for (StudentHomeworkAnswer answer : context.getStudentHomeworkAnswers()) {
                        String questionId = answer.getQuestionId();
                        context.getResult().put(
                                questionId,
                                MiscUtils.m(
                                        "fullScore", context.getStandardScore().get(questionId),
                                        "score", context.getScoreResult().get(questionId).getTotalScore(),
                                        "answers", context.getStandardAnswer().get(questionId),
                                        "userAnswers", answer.getAnswer(),
                                        "subMaster", context.getSubGrasp().get(questionId),
                                        "subScore", context.getSubScore().get(questionId),
                                        "master", context.getScoreResult().get(questionId).getIsRight(),
                                        "hintIntervention", true
                                )
                        );
                    }
                }
                return;
            }
        }

        if (Subject.CHINESE == context.getSubject()) {
            return;
        }

        //地区灰度
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(context.getUserId());
        boolean interventionGray = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail,
                "Immediate", "Intervention");
        if (!interventionGray) {
            return;
        }

        List<StudentHomeworkAnswer> studentHomeworkAnswers = context.getStudentHomeworkAnswers();
        List<List<String>> userAnswer = null;
        if (studentHomeworkAnswers != null && studentHomeworkAnswers.size() == 1) {
            userAnswer = studentHomeworkAnswers.get(0).getAnswer();
        }
        //干预后提交, 不再即时干预 || 不知道什么原因俊晨组反映会有英语流量过去,如果是英语到这一步直接跳出
        if (context.getHintId() != null || Subject.ENGLISH.equals(context.getSubject()) || questionScoreResult.getIsRight()) {
            if (Subject.MATH.equals(context.getSubject()) && context.getHintId() != null) {
                interventionAnswerPublish(context, questionScoreResult, userAnswer, "re-attempt"); //上报数据
            }
            return;
        }

        //查@冯俊晨接口命中干预
        AlpsHttpResponse response = getAnswerAnalysis(context.getUserId(), context.getHomeworkId(), questionScoreResult.getQuestionId(), userAnswer);
        if (response != null) {
            HomeworkHintResp homeworkHintResp = JsonUtils.fromJson(response.getResponseString(), HomeworkHintResp.class);
            if (homeworkHintResp != null && homeworkHintResp.getHintId() != null) {
                context.errorResponse(homeworkHintResp.getMsg());
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_IMMEDIATE_INTERVENTION);
                context.setHintId(homeworkHintResp.getHintId());
                context.setHintOptType(homeworkHintResp.getHintOptType());
                context.setHintTag(homeworkHintResp.getHintTag());
                interventionAnswerPublish(context, questionScoreResult, userAnswer, null); //上报数据
            }
        }
    }

    //查@冯俊晨接口命中干预
    private AlpsHttpResponse getAnswerAnalysis(Long userId, String homeworkId, String questionId, List<List<String>> userAnswers) {
        Map<String, Object> httpParams = new HashMap<>();
        httpParams.put("useId", userId);
        httpParams.put("questionId", questionId);
        httpParams.put("userAnswers", JsonUtils.toJson(userAnswers));
        String requestUrl = RuntimeMode.current().le(Mode.TEST) ? HOMEWORK_HINT_URL_TEST : HOMEWORK_HINT_URL_PROD;
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                .post(requestUrl)
                .json(httpParams)
                .contentType("application/json").socketTimeout(3 * 1000)
                .execute();

        if (response == null || response.getStatusCode() != 200) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "mod1", homeworkId,
                    "mod2", questionId,
                    "op", "student homework intervention"
            ));
            logger.error("调用:{}失败, httpParams:{}, response: {}",
                    requestUrl,
                    httpParams,
                    response != null ? response.getResponseString() : "");
            return null;
        }
        return response;
    }

    /**
     * 发消息上报做题数据
     */
    private void interventionAnswerPublish(HomeworkResultContext context, QuestionScoreResult questionScoreResult, List<List<String>> userAnswer, String tag) {
        InterventionCommand command = new InterventionCommand();
        command.setActor(context.getUserId());
        command.setObject(questionScoreResult.getQuestionId());
        command.setVerb("answer");
        command.setResult(questionScoreResult.getIsRight());
        command.setContext(new InterventionCommand.Context(context.getHomeworkId(), context.getObjectiveConfigType().name(), tag));
        Date timestamp = context.getTimestamp();
        if (timestamp == null) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getHomeworkId(),
                    "mod2", questionScoreResult.getQuestionId(),
                    "hintId", context.getHintId(),
                    "interventionAnswer", context.getInterventionAnswer(),
                    "objectiveConfigType", context.getObjectiveConfigType(),
                    "duration", context.getDuration(),
                    "op", "student homework intervention timestamp is null"
            ));
        }
        if (timestamp == null && context.getDuration() != null) {
            timestamp = new Date(System.currentTimeMillis() - context.getDuration());
        }
        if (timestamp != null) {
            command.setTimestamp(DateUtils.dateToString(timestamp));
        }
        command.setDuration(context.getDuration());
        command.setAttachments(new InterventionCommand.Attachments(userAnswer));
        interventionPublisher.getInterventionProducer().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(command)));
    }

    @Getter
    @Setter
    private static class HomeworkHintResp {
        private String msg;//前端展示的提示信息	富文本（可调颜色和大小），不可以是图片，一般在25个汉字以内
        private Integer hintOptType;//干预类型	 0=下一个，无交互；1=是/否选择
        private String hintTag;//信息标签	用于区分干预类型
        private Integer hintId;//过程（错误）类型	1=读题，2=推理列式，3=计算画图，4=输入
    }

}

