/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanPushExamHistoryDao;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/21
 */
@Named
public class CR_UpdatePushExamHistory extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {

    @Inject private AfentiLearningPlanPushExamHistoryDao afentiLearningPlanPushExamHistoryDao;

    @Override
    public void execute(CastleResultContext context) {
        // 获取当前题目的AfentiLearningPlanPushExamHistory
        AfentiLearningPlanPushExamHistory history = context.getHistories()
                .stream()
                .filter(h -> StringUtils.equals(h.getExamId(), context.getQuestionId()))
                .findFirst()
                .orElse(null);

        if (history == null) {
            logger.error("CR_UpdatePushExamHistory Cannot update PushExamHistory. context is {}", JsonUtils.toJson(context));
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
