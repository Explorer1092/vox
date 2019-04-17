package com.voxlearning.utopia.service.afenti.impl.service.processor.review.result;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanPushExamHistoryDao;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class RR_UpdatePushExamHistory extends SpringContainerSupport implements IAfentiTask<ReviewResultContext> {
    @Inject
    private AfentiLearningPlanPushExamHistoryDao afentiLearningPlanPushExamHistoryDao;

    @Override
    public void execute(ReviewResultContext context) {
        // 获取当前题目的AfentiLearningPlanPushExamHistory
        AfentiLearningPlanPushExamHistory history = context.getHistories()
                .stream()
                .filter(h -> StringUtils.equals(h.getExamId(), context.getQuestionId()))
                .findFirst()
                .orElse(null);

        if (history == null) {
            logger.error("RR_UpdatePushExamHistory Cannot update PushExamHistory. context is {}", JsonUtils.toJson(context));
            context.errorResponse();
            return;
        }

        if (Boolean.TRUE.equals(context.getMaster())) {
            history.setRightNum(1);
        } else {
            history.increaseErrorNum();
        }

        if (afentiLearningPlanPushExamHistoryDao.updateRightAndErrorNums(history)) {
            // 写回到histories中
            Map<Long, AfentiLearningPlanPushExamHistory> map = context.getHistories().stream()
                    .collect(Collectors.toMap(AfentiLearningPlanPushExamHistory::getId, Function.identity()));
            map.put(history.getId(), history);
            context.getHistories().clear();
            context.getHistories().addAll(map.values());
        }
    }
}
