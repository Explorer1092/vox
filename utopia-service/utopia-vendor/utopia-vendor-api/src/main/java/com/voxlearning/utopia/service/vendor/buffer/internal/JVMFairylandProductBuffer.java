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
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedFairylandProducts;
import com.voxlearning.utopia.service.vendor.buffer.FairylandProductBuffer;
import com.voxlearning.utopia.service.vendor.mdb.MDBFairylandProduct;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ThreadSafe
final public class JVMFairylandProductBuffer implements FairylandProductBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();
    private List<MDBFairylandProduct> originalList;
    private long version;

    @Override
    public void attach(VersionedFairylandProducts data) {
        Assertions.notNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            originalList = new LinkedList<>();
            if (data.getFairylandProducts() != null) {
                data.getFairylandProducts().stream()
                        .filter(Objects::nonNull)
                        .map(FairylandProduct::transform)
                        .forEach(originalList::add);
            }
            version = data.getVersion();
        });
    }

    @Override
    public VersionedFairylandProducts dump() {
        return locker.withinReadLock(() -> {
            VersionedFairylandProducts data = new VersionedFairylandProducts();
            data.setVersion(version);
            data.setFairylandProducts(originalList.stream()
                    .sorted((o1, o2) -> {
                        int r1 = SafeConverter.toInt(o1.getRank());
                        int r2 = SafeConverter.toInt(o2.getRank());
                        return Integer.compare(r2, r1);
                    })
                    .map(MDBFairylandProduct::transform)
                    .collect(Collectors.toList()));
            return data;
        });
    }

    @Override
    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }
}
