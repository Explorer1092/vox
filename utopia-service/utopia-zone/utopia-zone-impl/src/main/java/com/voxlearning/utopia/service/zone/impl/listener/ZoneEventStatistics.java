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

package com.voxlearning.utopia.service.zone.impl.listener;

import com.voxlearning.alps.annotation.common.Singleton;
import com.voxlearning.utopia.queue.zone.ZoneEventType;

import java.util.EnumMap;
import java.util.concurrent.atomic.LongAdder;

@Singleton
final public class ZoneEventStatistics {

    public static final ZoneEventStatistics INSTANCE = new ZoneEventStatistics();

    private final EnumMap<ZoneEventType, LongAdder> buffer;

    private ZoneEventStatistics() {
        buffer = new EnumMap<>(ZoneEventType.class);
        for (ZoneEventType type : ZoneEventType.values()) {
            buffer.put(type, new LongAdder());
        }
    }

    void increment(ZoneEventType type) {
        if (type != null) {
            buffer.get(type).increment();
        }
    }

    public long sum(ZoneEventType type) {
        if (type == null) {
            return 0;
        }
        return buffer.get(type).sum();
    }
}
