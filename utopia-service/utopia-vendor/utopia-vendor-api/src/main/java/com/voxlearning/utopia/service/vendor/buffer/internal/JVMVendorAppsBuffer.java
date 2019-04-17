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
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedVendorAppsList;
import com.voxlearning.utopia.service.vendor.buffer.VendorAppsBuffer;
import com.voxlearning.utopia.service.vendor.mdb.MDBVendorApps;

import java.util.*;
import java.util.stream.Collectors;

@ThreadSafe
public class JVMVendorAppsBuffer implements VendorAppsBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();
    private long version;
    private List<MDBVendorApps> originalList;
    private Map<Long, MDBVendorApps> idMap;
    private Map<String, MDBVendorApps> akMap;

    @Override
    public void attach(VersionedVendorAppsList data) {
        Objects.requireNonNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            version = data.getVersion();
            originalList = new LinkedList<>();
            idMap = new LinkedHashMap<>();
            akMap = new LinkedHashMap<>();

            if (data.getVendorAppsList() != null) {
                data.getVendorAppsList().stream()
                        .map(VendorApps::transform)
                        .forEach(originalList::add);
            }
            originalList.forEach(e -> idMap.put(e.getId(), e));
            originalList.stream()
                    .filter(e -> e.getAppKey() != null)
                    .forEach(e -> akMap.put(e.getAppKey(), e));
        });
    }

    @Override
    public VersionedVendorAppsList dump() {
        return locker.withinReadLock(() -> {
            VersionedVendorAppsList data = new VersionedVendorAppsList();
            data.setVersion(version);
            data.setVendorAppsList(originalList.stream()
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
            MDBVendorApps t = idMap.get(id);
            return t == null ? null : t.transform();
        });
    }

    @Override
    public VendorApps loadByAk(String ak) {
        if (ak == null) {
            return null;
        }
        return locker.withinReadLock(() -> {
            MDBVendorApps t = akMap.get(ak);
            return t == null ? null : t.transform();
        });
    }
}
