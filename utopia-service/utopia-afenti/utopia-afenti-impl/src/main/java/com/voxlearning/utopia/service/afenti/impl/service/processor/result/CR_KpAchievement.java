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
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQueueMessageType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.queue.AfentiQueueProducer;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/8/1
 */
@Named
public class CR_KpAchievement extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Inject private AfentiQueueProducer afentiQueueProducer;

    @Override
    public void execute(CastleResultContext context) {
        // 获取当前题目的AfentiLearningPlanPushExamHistory
        AfentiLearningPlanPushExamHistory history = context.getHistories()
                .stream()
                .filter(h -> StringUtils.equals(h.getExamId(), context.getQuestionId()))
                .findFirst()
                .orElse(null);
        String kp = StringUtils.defaultString(history.getKnowledgePoint());
        Long studentId = context.getStudent().getId();
        if (StringUtils.isBlank(kp)) {
            return;
        }
        if (!asyncAfentiCacheService.AfentiKnowledgePointCacheManager_sended(studentId, context.getSubject(), kp).take()) {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("T", AfentiQueueMessageType.KP_COUNT_ACHIEVEMENT);
            message.put("U", context.getStudent().getId());
            message.put("S", context.getSubject());
            afentiQueueProducer.getProducer().produce(Message.newMessage().withStringBody(JsonUtils.toJson(message)));
            asyncAfentiCacheService.AfentiKnowledgePointCacheManager_record(studentId, context.getSubject(), kp)
                    .awaitUninterruptibly();
        }
    }
}
