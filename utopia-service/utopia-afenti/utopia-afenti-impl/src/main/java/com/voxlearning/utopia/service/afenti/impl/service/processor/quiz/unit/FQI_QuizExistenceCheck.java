package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.unit;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchQuizInfoContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.question.consumer.AfentiQuestionsLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Named
public class FQI_QuizExistenceCheck extends SpringContainerSupport implements IAfentiTask<FetchQuizInfoContext> {
    @Inject private AfentiQuestionsLoaderClient afentiQuestionsLoaderClient;

    @Override
    public void execute(FetchQuizInfoContext context) {
        String unitId = context.getUnitId();

        // 根据单元id获取单元测试的题目，如果题目存在，则显示单元测试标志
        Map<String, String> qid_kpid_map = afentiQuestionsLoaderClient
                .loadQuestionsMapByBookCatalogIds(Collections.singleton(unitId)).getOrDefault(unitId, null);

        if (MapUtils.isNotEmpty(qid_kpid_map)) {
            context.getResult().put("exist", true);
        } else {
            context.getResult().put("exist", false);
            context.terminateTask();
        }
    }
}
