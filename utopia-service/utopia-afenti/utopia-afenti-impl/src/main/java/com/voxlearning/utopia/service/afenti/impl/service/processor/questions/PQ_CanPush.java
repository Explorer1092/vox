package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.PushQuestionContext;
import com.voxlearning.utopia.service.afenti.api.entity.UserAfentiStats;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.voxlearning.alps.annotation.common.Mode.PRODUCTION;
import static com.voxlearning.alps.annotation.common.Mode.STAGING;
import static com.voxlearning.alps.annotation.meta.Subject.*;
import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.*;

/**
 * @author ruib
 * @since 16/7/17
 */
@Named
public class PQ_CanPush extends SpringContainerSupport implements IAfentiTask<PushQuestionContext> {
    @Inject private AfentiLoaderImpl afentiLoader;
    @Inject private StudentLoaderClient studentLoaderClient;

    @Override
    public void execute(PushQuestionContext context) {
        StudentDetail student = studentLoaderClient.loadStudentDetail(context.getStudentId());
        if (null == student || null == student.getClazz()) {
            context.setErrorCode(DEFAULT.getCode());
            context.errorResponse(DEFAULT.getInfo());
            return;
        }
        // 非会员不允许闯关 -- 预习
        if (context.getLearningType() == AfentiLearningType.preparation && !context.isAuthorized()) {
            context.setErrorCode(PUSH_QUESTION_BALANCE_NOT_ENOUGH_FOR_PREPARATION.getCode());
            context.errorResponse(PUSH_QUESTION_BALANCE_NOT_ENOUGH_FOR_PREPARATION.getInfo());
            return;
        }
        context.setStudent(student);
        long balance = student.getUserIntegral() == null ? 0L : student.getUserIntegral().getUsable();

        UserAfentiStats stats = afentiLoader.loadUserAfentiStats(context.getStudentId());
        if (stats == null) {
            context.setCount(0);
            return; // 没找到，那就推吧
        }

        // 今天推送了几关
        long count = getPushCount(stats, context.getLearningType(), context.getSubject());

        // staging这两个账号不限制每天新开关卡数
        List<Long> superAccount = Arrays.asList(30029L, 30030L, 30031L, 380383927L, 333919256L, 333919344L, 333916865L, 333919334L);
        // 如果当天已经推送超过规定次数，不允许推送
        if (RuntimeMode.current() == PRODUCTION ||
                !(RuntimeMode.current().le(STAGING) && superAccount.contains(context.getStudentId()))) {
            if (count >= getLimitCount(context.getSubject())) {
                context.setErrorCode(PUSH_QUESTION_OVER_THREE_LIMITATION.getCode());
                context.errorResponse(PUSH_QUESTION_OVER_THREE_LIMITATION.getInfo());
                return;
            }
        }

        if (context.getLearningType() == AfentiLearningType.castle) {
            // 如果用户没有付费，只有3次试用机会
            List<String> candidates = new ArrayList<>();
            for (String each : stats.getStats().values()) {
                String[] elements = StringUtils.split(each, "|");
                switch (context.getSubject()) {
                    case ENGLISH: {
                        if (elements.length == 1)
                            candidates.add(each);
                        break;
                    }
                    case MATH: {
                        if (elements.length == 2 && StringUtils.equals(elements[1], MATH.name()))
                            candidates.add(each);
                        break;
                    }
                    case CHINESE: {
                        if (elements.length == 2 && StringUtils.equals(elements[1], CHINESE.name()))
                            candidates.add(each);
                        break;
                    }
                    default:
                }
            }
            // 未付费用户有三次试用机会
            if (!context.isAuthorized() && candidates.size() >= 3) {
                context.setErrorCode(PUSH_QUESTION_OVER_TRIAL_LIMITATION.getCode());
                context.errorResponse(PUSH_QUESTION_OVER_TRIAL_LIMITATION.getInfo());
                return;
            }
        }

        // 如果超过一次，需要看余额是否足够 分学科处理
        if (context.getSubject() == ENGLISH) {
            if ((count == 1 && balance < 20) || (count == 2 && balance < 30)) {
                context.setErrorCode(PUSH_QUESTION_BALANCE_NOT_ENOUGH.getCode());
                context.errorResponse(PUSH_QUESTION_BALANCE_NOT_ENOUGH.getInfo());
                return;
            }
        } else if (context.getSubject() == CHINESE) {
            if ((count == 2 && balance < 30) || (count == 3 && balance < 30)) {
                context.setErrorCode(PUSH_QUESTION_BALANCE_NOT_ENOUGH.getCode());
                context.errorResponse(PUSH_QUESTION_BALANCE_NOT_ENOUGH.getInfo());
                return;
            }
        } else if (context.getSubject() == MATH) {
            if ((count == 1 && balance < 10) || (count == 2 && balance < 20)
                    || (count == 3 && balance < 30) || (count == 4 && balance < 30)) {
                context.setErrorCode(PUSH_QUESTION_BALANCE_NOT_ENOUGH.getCode());
                context.errorResponse(PUSH_QUESTION_BALANCE_NOT_ENOUGH.getInfo());
                return;
            }
        }
        context.setCount(count);
    }

    // 每天闯关次数限制
    private long getLimitCount(Subject subject) {
        if (subject == ENGLISH) {
            return 3;
        }
        if (subject == MATH) {
            return 5;
        }
        if (subject == CHINESE) {
            return 4;
        }
        return 0;
    }

    private long getPushCount(UserAfentiStats stats, AfentiLearningType learningType, Subject subject) {
        String pushDate = DateUtils.dateToString(new Date(), FORMAT_SQL_DATE);
        String value = subject == ENGLISH ? pushDate :
                StringUtils.join(Arrays.asList(pushDate, subject), "|");
        if (learningType == AfentiLearningType.preparation) {
            value = subject == ENGLISH ? StringUtils.join(Arrays.asList(pushDate, learningType.name()), "|") :
                    StringUtils.join(Arrays.asList(pushDate, subject, learningType.name()), "|");
        }
        String finalValue = value;
        return stats.getStats().values().stream().filter(v -> StringUtils.equals(v, finalValue)).count();
    }
}
