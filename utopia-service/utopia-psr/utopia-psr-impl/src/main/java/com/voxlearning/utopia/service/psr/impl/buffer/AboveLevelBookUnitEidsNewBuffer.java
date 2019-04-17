/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.psr.impl.buffer;

import com.voxlearning.alps.api.event.AlpsEventContext;
import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.alps.api.event.dsl.MinuteTimerEventListener;
import com.voxlearning.alps.api.event.dsl.TimerEvent;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.psr.entity.AboveLevelBookUnitEidsNew;
import com.voxlearning.utopia.service.psr.impl.persistence.AboveLevelBookUnitEidsNewPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 现在数据库中量不大，每30分钟自动刷一下缓存。
 * 如果未来有变化，务必要记得复查这里是否需要修改。
 */
@Named("com.voxlearning.utopia.service.psr.impl.buffer.AboveLevelBookUnitEidsNewBuffer")
public class AboveLevelBookUnitEidsNewBuffer extends SpringContainerSupport {

    @Inject private AboveLevelBookUnitEidsNewPersistence aboveLevelBookUnitEidsNewPersistence;

    private final AtomicReference<Map<String, AboveLevelBookUnitEidsNew>> reference = new AtomicReference<>();

    private final MinuteTimerEventListener listener = new MinuteTimerEventListener() {
        @Override
        protected long mod() {
            return 43200;
        }

        @Override
        protected void processEvent(TimerEvent event, AlpsEventContext context) {
            reload();
        }
    };

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        reload();
        EventBus.subscribe(listener);
    }

    private void reload() {
        Map<String, AboveLevelBookUnitEidsNew> map = aboveLevelBookUnitEidsNewPersistence.query().stream()
                .filter(e -> e.getBookid() != null)
                .collect(Collectors.groupingBy(AboveLevelBookUnitEidsNew::getBookid))
                .values()
                .stream()
                .map(e -> e.iterator().next())
                .collect(Collectors.toMap(AboveLevelBookUnitEidsNew::getBookid, Function.identity()));
        logger.info("[{}] AboveLevelBookUnitEidsNew(s) loaded into buffer", map.size());
        reference.set(map);
    }

    public AboveLevelBookUnitEidsNew findByBookId(String bookId) {
        if (bookId == null) {
            return null;
        }
        Map<String, AboveLevelBookUnitEidsNew> map = reference.get();
        return map.get(bookId);
    }
}
