package com.voxlearning.utopia.service.newhomework.impl.service.processor.answer;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.AbilityExamAnswerContext;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.AvengerQuestionAnswerResult;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.QuestionDataAnswer;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.QuestionResult;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamBasic;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.support.IntegralHistoryBuilderFactory;

import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;

/**
 * 保存做题结果
 *
 * @author lei.liu
 * @version 18-11-1
 */
@Named
public class AbilityExamAnswer_SaveQuestionAnswer extends AbstractAbilityExamAnswerChainProcessor {
    @Override
    protected void doProcess(AbilityExamAnswerContext context) {

        AbilityExamBasic basic = context.getAbilityExamBasic();

        if (basic.fetchFinished()) {
            return;
        }

        QuestionDataAnswer answer = context.getAnswer();
        if (context.getQuestionResultMap() == null || answer.getQuestionId() == null) {
            return;
        }
        QuestionResult questionResult = context.getQuestionResultMap().get(answer.getQuestionId());
        if (questionResult == null) {
            return;
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(context.getUserId());

        // 输出数据
        context.getResultMap().put(answer.getQuestionId(), MapUtils.m(
                "fullScore", questionResult,
                "score", questionResult.getTotalScore(),
                "answers", questionResult,
                "userAnswers", answer.getAnswer(),
                "subMaster", questionResult.getSubRight(),
                "subScore", questionResult,
                "master", questionResult.getIsRight()
        ));

        AbilityExamAnswer abilityExamAnswer = AbilityExamAnswer.newInstance(context.getUserId(), answer.getQuestionId(), basic.getPaperId(), studentDetail.getClazzLevelAsInteger(), questionResult.getIsRight(), questionResult.getSubRight(), context.getAnswer());
        abilityExamAnswerDao.upsert(abilityExamAnswer);

        AbilityExamQuestion abilityExamQuestion = abilityExamQuestionDao.load(String.valueOf(context.getUserId()));

        if (abilityExamQuestion == null) {
            abilityExamQuestion = new AbilityExamQuestion();
            abilityExamQuestion.setId(String.valueOf(context.getUserId()));
            abilityExamQuestion.setFinishedQuestions(new HashMap<>());
        }
        abilityExamQuestion.getFinishedQuestions().put(answer.getQuestionId(), abilityExamAnswer.getGrasp());

        // 全部完成
        if (basic.getQuestionIds().size() == abilityExamQuestion.getFinishedQuestions().size()) {
            Date now = new Date();
            basic.setCompletedTime(now);
            abilityExamBasicDao.upsert(basic);
            abilityExamQuestion.setFinishedDate(now);

            // 发放学豆，固定一个
            IntegralHistory history = IntegralHistoryBuilderFactory.newBuilder(context.getUserId(), IntegralType.VRSS_QUESTIONNAIRE_REWARD)
                    .withIntegral(1)
                    .withComment("价值研究与战略支持项目学豆发放")
                    .build();
            try {
                User user = userLoaderClient.loadUser(context.getUserId());
                userIntegralService.changeIntegral(user, history);
            } catch (Exception e) {
                logger.error("AbilityExamAnswer_SaveQuestionAnswer error. history:{}", history, e);
            }
        }
        abilityExamQuestionDao.upsert(abilityExamQuestion);

        try {
            // 拼装上报数据
            AvengerQuestionAnswerResult avenger = AvengerQuestionAnswerResult.newInstance(abilityExamAnswer);
            producer.getJournalHomeworkProcessResultProducer().produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(avenger)));
        } catch (Exception e) {
            logger.warn("reportBigData error, adid:{}", context.getAbilityExamBasic().getId());
        }
    }
}
