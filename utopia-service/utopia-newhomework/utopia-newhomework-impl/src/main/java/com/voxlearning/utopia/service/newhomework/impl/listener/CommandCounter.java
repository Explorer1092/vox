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

package com.voxlearning.utopia.service.newhomework.impl.listener;

import com.voxlearning.alps.annotation.common.Singleton;
import com.voxlearning.alps.annotation.common.ThreadSafe;
import com.voxlearning.alps.core.concurrent.ReentrantReadWriteLocker;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
@ThreadSafe
final public class CommandCounter {

    @Getter private static final CommandCounter instance = new CommandCounter();

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();
    private final Map<String, AtomicLong> buffer = new LinkedHashMap<>();

    private CommandCounter() {
        buffer.put("LegacyCommand", new AtomicLong(0));
        buffer.put("SaveJournalNewHomeworkProcessResultCommand", new AtomicLong(0));
        buffer.put("UpdateTotalAssignmentRecordCommand", new AtomicLong(0));
        buffer.put("Unrecognized", new AtomicLong(0));
        buffer.put("JournalStudentHomework", new AtomicLong(0));
        buffer.put("SaveHomeworkResultHBaseCommand", new AtomicLong(0));
        buffer.put("SaveHomeworkResultAnswerHBaseCommand", new AtomicLong(0));
        buffer.put("SaveHomeworkProcessResultHBaseCommand", new AtomicLong(0));
        buffer.put("SaveHomeworkSyllableCommand", new AtomicLong(0));
        buffer.put("SaveSelfStudyWordIncreaseHomeworkCommand", new AtomicLong(0));

    }

    public void increase(String command) {
        if (command == null) {
            return;
        }
        get(command).incrementAndGet();
    }

    private AtomicLong get(String command) {
        AtomicLong buffered = locker.withinReadLock(() -> buffer.get(command));
        if (buffered != null) {
            return buffered;
        }
        return locker.withinWriteLock(() -> {
            AtomicLong counter = buffer.get(command);
            if (counter != null) {
                return counter;
            }
            counter = new AtomicLong(0);
            buffer.put(command, counter);
            return counter;
        });
    }

    public String[] getCommands() {
        return locker.withinReadLock(() -> {
            Set<String> set = buffer.keySet();
            return set.toArray(new String[set.size()]);
        });
    }

    public long fetchCount(String command) {
        if (command == null) {
            return 0;
        }
        return locker.withinReadLock(() -> {
            AtomicLong c = buffer.get(command);
            return c == null ? 0 : c.get();
        });
    }
}
