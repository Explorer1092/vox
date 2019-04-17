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

package com.voxlearning.utopia.service.zone.buffer.internal;

import com.voxlearning.alps.annotation.common.ThreadSafe;
import com.voxlearning.alps.core.concurrent.ReentrantReadWriteLocker;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.zone.api.entity.UserMood;
import com.voxlearning.utopia.service.zone.buffer.UserMoodBuffer;
import com.voxlearning.utopia.service.zone.data.VersionedUserMoodData;
import com.voxlearning.utopia.service.zone.mdb.MDBUserMood;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ThreadSafe
final public class JVMUserMoodBuffer implements UserMoodBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();
    private List<MDBUserMood> originalList;
    private long version;

    @Override
    public void attach(VersionedUserMoodData data) {
        Assertions.notNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            originalList = new LinkedList<>();
            if (data.getUserMoodList() != null) {
                data.getUserMoodList().stream()
                        .filter(Objects::nonNull)
                        .map(UserMood::transform)
                        .forEach(originalList::add);
            }
            version = data.getVersion();
        });
    }

    @Override
    public VersionedUserMoodData dump() {
        return locker.withinReadLock(() -> {
            VersionedUserMoodData data = new VersionedUserMoodData();
            data.setVersion(version);
            data.setUserMoodList(originalList.stream()
                    .sorted((o1, o2) -> {
                        long c1 = SafeConverter.toLong(o1.getCreateDatetime());
                        long c2 = SafeConverter.toLong(o2.getCreateDatetime());
                        return Long.compare(c2, c1);
                    })
                    .map(MDBUserMood::transform)
                    .collect(Collectors.toList()));
            return data;
        });
    }

    @Override
    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }

    @Override
    public UserMood load(Long id) {
        return locker.withinReadLock(() -> {
            MDBUserMood mood = originalList.stream()
                    .filter(e -> Objects.equals(id, e.getId()))
                    .findFirst()
                    .orElse(null);
            return mood == null ? null : mood.transform();
        });
    }
}
