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

package com.voxlearning.utopia.service.zone.impl.buffer;

import com.voxlearning.alps.annotation.common.ThreadSafe;
import com.voxlearning.alps.core.concurrent.ReentrantReadWriteLocker;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.utopia.service.zone.api.entity.Gift;
import com.voxlearning.utopia.service.zone.buffer.GiftBuffer;
import com.voxlearning.utopia.service.zone.data.VersionedGiftData;
import com.voxlearning.utopia.service.zone.impl.persistence.MDBGiftPersistence;
import com.voxlearning.utopia.service.zone.mdb.MDBGift;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ThreadSafe
final public class MDBGiftBuffer implements GiftBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();
    private final MDBGiftPersistence mdbGiftPersistence;
    private long version;

    public MDBGiftBuffer(MDBGiftPersistence mdbGiftPersistence) {
        this.mdbGiftPersistence = Assertions.notNull(mdbGiftPersistence);
    }

    @Override
    public void attach(VersionedGiftData data) {
        Assertions.notNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            mdbGiftPersistence.$remove(new Criteria());
            if (data.getGiftList() != null) {
                List<MDBGift> transformed = data.getGiftList().stream()
                        .filter(Objects::nonNull)
                        .map(Gift::transform)
                        .collect(Collectors.toList());
                mdbGiftPersistence.inserts(transformed);
            }
            version = data.getVersion();
        });
    }

    @Override
    public VersionedGiftData dump() {
        return locker.withinReadLock(() -> {
            VersionedGiftData data = new VersionedGiftData();
            data.setVersion(version);
            data.setGiftList(mdbGiftPersistence.queryAll()
                    .stream()
                    .sorted(Comparator.comparing(MDBGift::getId))
                    .map(MDBGift::transform)
                    .collect(Collectors.toList()));
            return data;
        });
    }

    @Override
    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }
}
