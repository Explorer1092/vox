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
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedVendorAppsList;
import com.voxlearning.utopia.service.vendor.buffer.VendorAppsBuffer;
import com.voxlearning.utopia.service.vendor.impl.persistence.MDBVendorAppsPersistence;
import com.voxlearning.utopia.service.vendor.mdb.MDBVendorApps;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ThreadSafe
final public class MDBVendorAppsBuffer implements VendorAppsBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();
    private final MDBVendorAppsPersistence mdbVendorAppsPersistence;
    private long version;

    public MDBVendorAppsBuffer(MDBVendorAppsPersistence mdbVendorAppsPersistence) {
        this.mdbVendorAppsPersistence = Assertions.notNull(mdbVendorAppsPersistence);
    }

    @Override
    public void attach(VersionedVendorAppsList data) {
        Assertions.notNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            mdbVendorAppsPersistence.$remove(new Criteria());
            if (data.getVendorAppsList() != null) {
                List<MDBVendorApps> transformed = data.getVendorAppsList().stream()
                        .filter(Objects::nonNull)
                        .map(VendorApps::transform)
                        .collect(Collectors.toList());
                mdbVendorAppsPersistence.inserts(transformed);
            }
            version = data.getVersion();
        });
    }

    @Override
    public VersionedVendorAppsList dump() {
        return locker.withinReadLock(() -> {
            VersionedVendorAppsList data = new VersionedVendorAppsList();
            data.setVersion(version);
            data.setVendorAppsList(mdbVendorAppsPersistence.queryAll()
                    .stream()
                    .sorted((o1, o2) -> {
                        int r1 = SafeConverter.toInt(o1.getRank());
                        int r2 = SafeConverter.toInt(o2.getRank());
                        return Integer.compare(r2, r1);
                    })
                    .map(MDBVendorApps::transform)
                    .collect(Collectors.toList()));
            return data;
        });
    }

    @Override
    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }

    @Override
    public VendorApps loadById(Long id) {
        if (id == null) {
            return null;
        }
        return locker.withinReadLock(() -> {
            MDBVendorApps t = mdbVendorAppsPersistence.load(id);
            return t == null ? null : t.transform();
        });
    }

    @Override
    public VendorApps loadByAk(String ak) {
        if (ak == null) {
            return null;
        }
        return locker.withinReadLock(() -> {
            MDBVendorApps t = mdbVendorAppsPersistence.loadByAppKey(ak);
            return t == null ? null : t.transform();
        });
    }
}
