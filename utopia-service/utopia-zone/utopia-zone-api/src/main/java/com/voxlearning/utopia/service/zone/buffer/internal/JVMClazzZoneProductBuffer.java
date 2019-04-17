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
import com.voxlearning.utopia.service.zone.api.constant.ClazzZoneProductSpecies;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneProduct;
import com.voxlearning.utopia.service.zone.buffer.ClazzZoneProductBuffer;
import com.voxlearning.utopia.service.zone.data.VersionedClazzZoneProductData;
import com.voxlearning.utopia.service.zone.mdb.MDBClazzZoneProduct;

import java.util.*;
import java.util.stream.Collectors;

@ThreadSafe
final public class JVMClazzZoneProductBuffer implements ClazzZoneProductBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();
    private List<MDBClazzZoneProduct> originalList;
    private long version;

    @Override
    public void attach(VersionedClazzZoneProductData data) {
        Assertions.notNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            originalList = new LinkedList<>();
            if (data.getClazzZoneProductList() != null) {
                data.getClazzZoneProductList().stream()
                        .filter(Objects::nonNull)
                        .map(ClazzZoneProduct::transform)
                        .forEach(originalList::add);
            }
            version = data.getVersion();
        });
    }

    @Override
    public VersionedClazzZoneProductData dump() {
        return locker.withinReadLock(() -> {
            VersionedClazzZoneProductData data = new VersionedClazzZoneProductData();
            data.setVersion(version);
            data.setClazzZoneProductList(originalList.stream()
                    .sorted(Comparator.comparing(MDBClazzZoneProduct::getId))
                    .map(MDBClazzZoneProduct::transform)
                    .collect(Collectors.toList()));
            return data;
        });
    }

    @Override
    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }

    @Override
    public ClazzZoneProduct load(Long id) {
        if (id == null) {
            return null;
        }
        return locker.withinReadLock(() -> {
            MDBClazzZoneProduct product = originalList.stream()
                    .filter(e -> Objects.equals(e.getId(), id))
                    .findFirst()
                    .orElse(null);
            return product == null ? null : product.transform();
        });
    }

    @Override
    public List<ClazzZoneProduct> findBySpecies(ClazzZoneProductSpecies species) {
        if (species == null) {
            return Collections.emptyList();
        }
        return locker.withinReadLock(() -> originalList.stream()
                .filter(e -> Objects.equals(species.name(), e.getSpecies()))
                .sorted(Comparator.comparing(MDBClazzZoneProduct::getId))
                .map(MDBClazzZoneProduct::transform)
                .collect(Collectors.toList()));
    }
}
