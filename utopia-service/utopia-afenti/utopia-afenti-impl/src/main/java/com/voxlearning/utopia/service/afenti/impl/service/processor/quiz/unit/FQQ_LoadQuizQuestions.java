package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.unit;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchQuizQuestionContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.question.consumer.AfentiQuestionsLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Named
public class FQQ_LoadQuizQuestions extends SpringContainerSupport implements IAfentiTask<FetchQuizQuestionContext> {
    @Inject private AfentiQuestionsLoaderClient afentiQuestionsLoaderClient;

    @Override
    public void execute(FetchQuizQuestionContext context) {
        String unitId = context.getUnitId();

        // 根据单元id获取单元测试的题目，如果题目存在，则显示单元测试标志
        Map<String, String> qid_kpid_map = afentiQuestionsLoaderClient
                .loadQuestionsMapByBookCatalogIds(Collections.singleton(unitId)).getOrDefault(unitId, null);

        if (MapUtils.isEmpty(qid_kpid_map)) {
            logger.error("FQQ_LoadQuizQuestions Cannot load afenti unit quiz question for user {}, subject {}, unit {}",
                    context.getStudent().getId(), context.getSubject(), unitId);
            context.errorResponse();
            return;
        }

        context.getQid_kpid_map().putAll(qid_kpid_map);
        context.setQc(qid_kpid_map.keySet().size());
        context.setKpc(new HashSet<>(qid_kpid_map.values()).size());
    }
}
