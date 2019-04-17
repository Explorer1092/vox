package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.integral.api.constants.CreditType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.CreditHistory;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.support.CreditHistoryBuilderFactory;
import com.voxlearning.utopia.service.newhomework.api.constant.MentalArithmeticPrecision;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkResultServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.DoHomeworkProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import org.springframework.dao.DuplicateKeyException;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/15
 */
@Named
public class FH_UpdateHomeworkResult extends SpringContainerSupport implements FinishHomeworkTask {
    @Inject private NewHomeworkResultServiceImpl newHomeworkResultService;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private DoHomeworkProcessor doHomeworkProcessor;

    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    @Override
    public void execute(FinishHomeworkContext context) {
        NewHomeworkResult modified = newHomeworkResultService.finishHomework(
                context.getHomework(),
                context.getUserId(),
                context.getObjectiveConfigType(),
                context.getPracticeScore(),
                context.getPracticeDureation(),
                context.isPracticeFinished(),
                context.isHomeworkFinished(),
                context.getHomework().getIncludeSubjective(),
                context.getHomework().isHomeworkTerminated(),
                context.getOcrMentalAnswerIds(),
                context.getOcrMentalQuestionCount(),
                context.getOcrMentalCorrectQuestionCount(),
                context.getOcrDictationAnswerIds(),
                context.getOcrDictationQuestionCount(),
                context.getOcrDictationCorrectQuestionCount()
        );

        // 如果修改成功并且完成了作业则需要重新对contest.result赋值并且发放学豆
        if (modified != null && context.isHomeworkFinished()) {
            int integral = newHomeworkResultLoader.homeworkIntegral(context.getHomework().isHomeworkTerminated(), modified); // 学生获得的积分
            //看看是否有奖励活动
            integral = doHomeworkProcessor.generateFinishHomeworkActivityIntegral(integral, context.getHomework(), null);

            // 奖励学分(口算训练答对题数的百分比)
            Integer credit = 0;
            List<NewHomeworkPracticeContent> practices = context.getHomework().getPractices();
            if (CollectionUtils.isNotEmpty(practices)) {
                for (NewHomeworkPracticeContent practiceContent : practices) {
                    if (practiceContent.getType() != null
                            && practiceContent.getTimeLimit() != null
                            && practiceContent.getType().equals(ObjectiveConfigType.MENTAL_ARITHMETIC)
                            && practiceContent.getTimeLimit().getTime() != 0
                            && SafeConverter.toBoolean(practiceContent.getMentalAward())) {
                        NewHomeworkResultAnswer resultAnswer = modified.getPractices().get(ObjectiveConfigType.MENTAL_ARITHMETIC);
                        if (resultAnswer != null) {
                            Double score = SafeConverter.toDouble(resultAnswer.getScore());
                            if (score >= MentalArithmeticPrecision.THIRTY_PERCENT.getPrecision()
                                    && score < MentalArithmeticPrecision.SEVENTY_PERCENT.getPrecision()) {
                                credit = 1;
                                context.setMentalArithmeticProgressReward(true);
                            } else if (score >= MentalArithmeticPrecision.SEVENTY_PERCENT.getPrecision()
                                    && score < MentalArithmeticPrecision.ONE_HUNDRED_PERCENT.getPrecision()) {
                                context.setMentalArithmeticProgressReward(true);
                                credit = 2;
                            } else if (score >= MentalArithmeticPrecision.ONE_HUNDRED_PERCENT.getPrecision()) {
                                context.setMentalArithmeticProgressReward(true);
                                credit = 3;
                            }

                        }
                        break;
                    }
                }
            }

            String comment; // 评述
            IntegralType integralType; // 积分类型

            if (context.getHomework().isHomeworkTerminated()) {
                integralType = IntegralType.学生补作作业;
                comment = "补做作业获得学豆";
            } else {
                integralType = IntegralType.学生完成作业;
                comment = "按时完成老师布置的作业，获得学豆";
            }

            try {
                //奖励学豆
                if (integral > 0) {
                    IntegralHistory integralHistory = new IntegralHistory(context.getUserId(), integralType, integral);
                    integralHistory.setComment(comment);
                    integralHistory.setUniqueKey(context.getHomeworkId());
                    userIntegralService.changeIntegral(integralHistory);
                }

                // 奖励学分
                if (credit > 0) {
                    CreditHistory hs = CreditHistoryBuilderFactory.newBuilder(context.getUserId(), CreditType.homework)
                            .withAmount(credit)
                            .withComment("完成作业获得自学积分")
                            .build();
                    userIntegralService.changeCredit(hs);
                }
            } catch (Exception ex) {
                if (ex instanceof DuplicateKeyException) {
                    logger.warn("Failed to change integral duplicated userId{} homeworkId{}", context.getUserId(), context.getHomeworkId());
                } else {
                    logger.error("Failed to change integral userId{} homeworkId{}", context.getUserId(), context.getHomeworkId());
                }
            }
            //保存完成作业获取的学豆、能量、学分
            newHomeworkResultService.saveFinishHomeworkReward(context.getHomework(), context.getUserId(), integral, null, credit);
            context.setResult(modified);
        }
        if (!context.isHomeworkFinished()) context.terminateTask();
    }
}
