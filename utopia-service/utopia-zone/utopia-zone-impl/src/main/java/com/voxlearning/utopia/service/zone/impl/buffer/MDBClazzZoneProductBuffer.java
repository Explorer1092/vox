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
import com.voxlearning.utopia.service.zone.api.constant.ClazzZoneProductSpecies;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneProduct;
import com.voxlearning.utopia.service.zone.buffer.ClazzZoneProductBuffer;
import com.voxlearning.utopia.service.zone.data.VersionedClazzZoneProductData;
import com.voxlearning.utopia.service.zone.impl.persistence.MDBClazzZoneProductPersistence;
import com.voxlearning.utopia.service.zone.mdb.MDBClazzZoneProduct;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ThreadSafe
final public class MDBClazzZoneProductBuffer implements ClazzZoneProductBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();
    private final MDBClazzZoneProductPersistence mdbClazzZoneProductPersistence;
    private long version;

    public MDBClazzZoneProductBuffer(MDBClazzZoneProductPersistence mdbClazzZoneProductPersistence) {
        this.mdbClazzZoneProductPersistence = Assertions.notNull(mdbClazzZoneProductPersistence);
    }

    @Override
    public void attach(VersionedClazzZoneProductData data) {
        Assertions.notNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            mdbClazzZoneProductPersistence.$remove(new Criteria());
            if (data.getClazzZoneProductList() != null) {
                List<MDBClazzZoneProduct> transformed = data.getClazzZoneProductList().stream()
                        .filter(Objects::nonNull)
                        .map(ClazzZoneProduct::transform)
                        .collect(Collectors.toList());
                mdbClazzZoneProductPersistence.inserts(transformed);
            }
            version = data.getVersion();
        });
    }

    @Override
    public VersionedClazzZoneProductData dump() {
        return locker.withinReadLock(() -> {
            VersionedClazzZoneProductData data = new VersionedClazzZoneProductData();
            data.setVersion(version);
            data.setClazzZoneProductList(mdbClazzZoneProductPersistence.queryAll()
                    .stream()
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
            MDBClazzZoneProduct product = mdbClazzZoneProductPersistence.load(id);
            return product == null ? null : product.transform();
        });
    }

    @Override
    public List<ClazzZoneProduct> findBySpecies(ClazzZoneProductSpecies species) {
        if (species == null) {
            return Collections.emptyList();
        }
        return locker.withinReadLock(() -> mdbClazzZoneProductPersistence.findBySpecies(species.name())
                .stream()
                .map(MDBClazzZoneProduct::transform)
                .collect(Collectors.toList()));
    }
}
