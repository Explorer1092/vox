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

package com.voxlearning.utopia.service.vendor.impl.buffer;

import com.voxlearning.alps.annotation.common.ThreadSafe;
import com.voxlearning.alps.core.concurrent.ReentrantReadWriteLocker;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsResgRef;
import com.voxlearning.utopia.service.vendor.buffer.VendorAppsResgRefBuffer;
import com.voxlearning.utopia.service.vendor.buffer.VersionedVendorAppsResgRefList;
import com.voxlearning.utopia.service.vendor.impl.persistence.MDBVendorAppsResgRefPersistence;
import com.voxlearning.utopia.service.vendor.mdb.MDBVendorAppsResgRef;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ThreadSafe
final public class MDBVendorAppsResgRefBuffer implements VendorAppsResgRefBuffer {

    private final MDBVendorAppsResgRefPersistence mdbVendorAppsResgRefPersistence;

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();

    private long version;

    public MDBVendorAppsResgRefBuffer(MDBVendorAppsResgRefPersistence mdbVendorAppsResgRefPersistence) {
        this.mdbVendorAppsResgRefPersistence = Assertions.notNull(mdbVendorAppsResgRefPersistence);
    }

    @Override
    public void attach(VersionedVendorAppsResgRefList data) {
        Assertions.notNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            this.mdbVendorAppsResgRefPersistence.purge();
            if (data.getRefList() != null) {
                List<MDBVendorAppsResgRef> transformed = data.getRefList().stream()
                        .filter(Objects::nonNull)
                        .map(VendorAppsResgRef::transform)
                        .collect(Collectors.toList());
                mdbVendorAppsResgRefPersistence.inserts(transformed);
            }
            this.version = data.getVersion();
        });
    }

    @Override
    public VersionedVendorAppsResgRefList dump() {
        return locker.withinReadLock(() -> {
            VersionedVendorAppsResgRefList data = new VersionedVendorAppsResgRefList();
            List<VendorAppsResgRef> refList = mdbVendorAppsResgRefPersistence.queryAll()
                    .stream()
                    .map(MDBVendorAppsResgRef::transform)
                    .collect(Collectors.toList());
            data.setVersion(version);
            data.setRefList(refList);
            return data;
        });
    }

    @Override
    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }

    @Override
    public List<VendorAppsResgRef> findByAppKey(String appKey) {
        if (appKey == null) {
            return Collections.emptyList();
        }
        return locker.withinReadLock(() -> {
            List<MDBVendorAppsResgRef> refList = mdbVendorAppsResgRefPersistence.findByAppKey(appKey);
            return refList.stream()
                    .map(MDBVendorAppsResgRef::transform)
                    .collect(Collectors.toList());
        });
    }
}
