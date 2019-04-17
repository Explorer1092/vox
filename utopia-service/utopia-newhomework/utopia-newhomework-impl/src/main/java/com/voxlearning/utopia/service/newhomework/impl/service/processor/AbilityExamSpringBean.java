package com.voxlearning.utopia.service.newhomework.impl.service.processor;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.newhomework.impl.dao.bonus.AbilityExamAnswerDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.bonus.AbilityExamBasicDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.bonus.AbilityExamQuestionDao;
import com.voxlearning.utopia.service.newhomework.impl.queue.AvengerQueueProducer;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.question.consumer.ScoreCalculationLoaderClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;

/**
 * @author lei.liu
 * @since 2018/11/01
 */
abstract public class AbilityExamSpringBean extends SpringContainerSupport {

    // 我们自己的
    @Inject protected AbilityExamBasicDao abilityExamBasicDao;
    @Inject protected AbilityExamQuestionDao abilityExamQuestionDao;
    @Inject protected AbilityExamAnswerDao abilityExamAnswerDao;

    // 平台部分的
    @Inject protected StudentLoaderClient studentLoaderClient;

    // 内容库部分的
    @Inject protected QuestionLoaderClient questionLoaderClient;
    @Inject protected ScoreCalculationLoaderClient scoreCalculationLoaderClient;

    // 上报部分的
    @Inject protected AvengerQueueProducer producer;

    // 发放学豆
    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;
    @Inject protected UserLoaderClient userLoaderClient;

    // 目前给任务专用
    protected static String currentBaseUrl() {
        Mode mode = RuntimeMode.current();
        switch (mode) {
            case DEVELOPMENT:
                return "https://www.test.17zuoye.net";
            case TEST:
                return "https://www.test.17zuoye.net";
            case STAGING:
                return "https://www.staging.17zuoye.net";
            case PRODUCTION:
                return "https://www.17zuoye.com";
            default:
                return "https://www.test.17zuoye.net";
        }
    }
}
