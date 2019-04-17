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

package com.voxlearning.utopia.service.action.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.action.api.ActionService;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.impl.queue.ActionQueueProducer;
import com.voxlearning.utopia.service.action.impl.support.ActionCacheSystem;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.EnumMap;

/**
 * Default {@link ActionService} implementation.
 *
 * @author Xiaohai Zhang
 * @since Aug 3, 2016
 */
@Named
@ExposeService(interfaceClass = ActionService.class)
public class ActionServiceImpl extends ApplicationObjectSupport implements ActionService, InitializingBean {

    private final EnumMap<ActionEventType, ActionEventHandler> handlers = new EnumMap<>(ActionEventType.class);

    @Inject private ActionQueueProducer actionQueueProducer;
    @Inject private ActionCacheSystem actionCacheSystem;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), ActionEventHandler.class, false, true)
                .values().forEach(e -> handlers.put(e.getEventType(), e));
    }

    @Override
    public void sendEvent(ActionEvent event) {
        if (event == null) {
            return;
        }
        actionQueueProducer.getProducer().produce(event.toMessage());
    }

    @Override
    public void handleActionEvent(ActionEvent event) {
        if (event == null) return;
        if (event.getUserId() == null) return;
        if (event.getType() == null) return;
        if (event.getTimestamp() == 0) event.setTimestamp(System.currentTimeMillis());
        if (event.getAttributes() == null) event.setAttributes(Collections.emptyMap());
        handlers.getOrDefault(event.getType(), ActionEventHandler.NOP).handle(event);
    }

    @Override
    public AlpsFuture<Long> increaseFinishSelfLearningCount(Long userId) {
        Long count = actionCacheSystem.CBS.flushable.incr("ACTION_EVENT_FINISH_SELF_LEARNING_COUNT_" + userId,
                1, 1, DateUtils.getCurrentToDayEndSecond());
        return new ValueWrapperFuture<>(count);
    }

    @Override
    public AlpsFuture<Long> decreaseDayRangeCount(Long userId, ActionEventType type, String day) {
        String key = CacheKeyGenerator.generateCacheKey("ActionEventDayRangeCounter",
                new String[]{"userId", "type", "day"},
                new Object[]{userId, type, day});
        int expiration = DateUtils.getCurrentToDayEndSecond() + 86400;
        Long l = actionCacheSystem.CBS.storage.decr(key, 1, 0, expiration);
        return new ValueWrapperFuture<>(l);
    }

}
