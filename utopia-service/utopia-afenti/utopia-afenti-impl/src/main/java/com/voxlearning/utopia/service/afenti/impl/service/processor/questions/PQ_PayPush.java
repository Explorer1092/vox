package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.PushQuestionContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.consumer.support.IntegralHistoryBuilderFactory;

import javax.inject.Named;

import static com.voxlearning.alps.annotation.meta.Subject.*;

/**
 * @author Ruib
 * @since 2016/7/19
 */
@Named
public class PQ_PayPush extends SpringContainerSupport implements IAfentiTask<PushQuestionContext> {

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    @Override
    public void execute(PushQuestionContext context) {
        //预习的不扣除学豆
        if (AfentiLearningType.preparation == context.getLearningType()) return;

        int delta = calculate(context.getCount(), context.getSubject());
        if (delta <= 0) return;

        String text;
        IntegralType type;
        switch (context.getSubject()) {
            case ENGLISH: {
                text = "阿分题英语";
                type = IntegralType.AFENTI_EXAM_UNLOCK_RANK;
                break;
            }
            case MATH: {
                text = "阿分题数学";
                type = IntegralType.AFENTI_MATH_UNLOCK_RANK;
                break;
            }
            case CHINESE: {
                text = "阿分题语文";
                type = IntegralType.AFENTI_CHINESE_UNLOCK_RANK;
                break;
            }
            default:
                return;
        }

        IntegralHistory history = IntegralHistoryBuilderFactory.newBuilder(context.getStudentId(), type)
                .withIntegral(-delta)
                .withComment("开启" + text + "关卡扣除学豆")
                .build();
        try {
            userIntegralService.changeIntegral(history);
        } catch (Exception ex) {
            logger.error("PQ_PayPush error. Failed to change integral for user {}", context.getStudentId(), ex);
        }
    }

    // 判断扣除学豆
    private int calculate(long count, Subject subject) {
        int delta = 0;
        if (subject == ENGLISH) {
            if (count == 1) {
                delta = 20;
            } else if (count == 2) {
                delta = 30;
            }
        } else if (subject == CHINESE) {
            if (count >= 2) {
                delta = 30;
            }
        } else if (subject == MATH) {
            if (count == 1) {
                delta = 10;
            } else if (count == 2) {
                delta = 20;
            } else if (count == 3) {
                delta = 30;
            } else if (count == 4) {
                delta = 30;
            }
        }
        return delta;
    }
}
