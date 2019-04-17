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
import com.voxlearning.utopia.service.vendor.api.entity.VendorResgContent;
import com.voxlearning.utopia.service.vendor.buffer.VendorResgContentBuffer;
import com.voxlearning.utopia.service.vendor.buffer.VersionedVendorResgContentList;
import com.voxlearning.utopia.service.vendor.mdb.MDBVendorResgContent;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ThreadSafe
final public class JVMVendorResgContentBuffer implements VendorResgContentBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();
    private long version;
    private List<MDBVendorResgContent> originalList;

    @Override
    public void attach(VersionedVendorResgContentList data) {
        Assertions.notNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            originalList = new LinkedList<>();
            if (data.getContentList() != null) {
                data.getContentList().stream()
                        .filter(Objects::nonNull)
                        .map(VendorResgContent::transform)
                        .forEach(originalList::add);
            }
            this.version = data.getVersion();
        });
    }

    @Override
    public VersionedVendorResgContentList dump() {
        return locker.withinReadLock(() -> {
            VersionedVendorResgContentList data = new VersionedVendorResgContentList();
            data.setVersion(version);
            data.setContentList(originalList.stream()
                    .sorted(Comparator.comparing(MDBVendorResgContent::getId))
                    .map(MDBVendorResgContent::transform)
                    .collect(Collectors.toList()));
            return data;
        });
    }

    @Override
    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }
}
