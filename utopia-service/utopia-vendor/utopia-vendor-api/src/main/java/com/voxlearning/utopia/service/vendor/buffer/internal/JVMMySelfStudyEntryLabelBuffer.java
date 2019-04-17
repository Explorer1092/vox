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

package com.voxlearning.utopia.service.vendor.buffer.internal;

import com.voxlearning.alps.annotation.common.ThreadSafe;
import com.voxlearning.alps.core.concurrent.ReentrantReadWriteLocker;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.vendor.api.entity.MySelfStudyEntryGlobalMsg;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedMySelfStudyGlobalMsgMap;
import com.voxlearning.utopia.service.vendor.buffer.MySelfStudyEntryLabelBuffer;

import java.util.Map;
import java.util.Objects;

/**
 * @author jiangpeng
 * @since 2017-06-14 下午4:23
 **/
@ThreadSafe
public class JVMMySelfStudyEntryLabelBuffer implements MySelfStudyEntryLabelBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();

    private long version;

    private Map<SelfStudyType, MySelfStudyEntryGlobalMsg> mySelfStudyEntryReminderMap;

    @Override
    public void attach(VersionedMySelfStudyGlobalMsgMap data) {
        Objects.requireNonNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            version = data.getVersion();
            mySelfStudyEntryReminderMap = data.getMySelfStudyEntryReminderMap();
        });
    }

    @Override
    public VersionedMySelfStudyGlobalMsgMap dump() {
        return locker.withinReadLock(() -> {
            VersionedMySelfStudyGlobalMsgMap data = new VersionedMySelfStudyGlobalMsgMap();
            data.setVersion(version);
            data.setMySelfStudyEntryReminderMap(mySelfStudyEntryReminderMap);
            return data;
        });
    }

    @Override
    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }

    @Override
    public Map<SelfStudyType, MySelfStudyEntryGlobalMsg> getMySelfStudyEntryReminderMap() {
        return locker.withinReadLock(() -> mySelfStudyEntryReminderMap);
    }


}
