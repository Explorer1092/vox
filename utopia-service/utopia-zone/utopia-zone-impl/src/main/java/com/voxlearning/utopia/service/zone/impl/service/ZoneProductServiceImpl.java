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

package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.alps.api.event.dsl.CallbackEvent;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.monitor.FlightController;
import com.voxlearning.utopia.service.zone.api.ZoneProductService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneProduct;
import com.voxlearning.utopia.service.zone.buffer.ClazzZoneProductBuffer;
import com.voxlearning.utopia.service.zone.data.VersionedClazzZoneProductData;
import com.voxlearning.utopia.service.zone.impl.buffer.MDBClazzZoneProductBuffer;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzZoneProductPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzZoneProductVersion;
import com.voxlearning.utopia.service.zone.impl.persistence.MDBClazzZoneProductPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.zone.impl.service.ZoneProductServiceImpl")
@ExposeService(interfaceClass = ZoneProductService.class)
public class ZoneProductServiceImpl extends SpringContainerSupport implements ZoneProductService, ClazzZoneProductBuffer.Aware {

    @Inject private ClazzZoneProductPersistence clazzZoneProductPersistence;
    @Inject private ClazzZoneProductVersion clazzZoneProductVersion;
    @Inject private MDBClazzZoneProductPersistence mdbClazzZoneProductPersistence;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        EventBus.publish(new CallbackEvent(() -> {
            if (RuntimeMode.isUnitTest()) {
                return;
            }
            VersionedClazzZoneProductData data = getClazzZoneProductBuffer().dump();
            logger.info("[ClazzZoneProductBuffer] initialized: [{}]", data.getVersion());
        })).awaitUninterruptibly();
    }

    @Override
    public AlpsFuture<List<ClazzZoneProduct>> loadAllClazzZoneProductsFromDB() {
        return new ValueWrapperFuture<>(clazzZoneProductPersistence.query());
    }

    @Override
    public AlpsFuture<ClazzZoneProduct> loadClazzZoneProductFromDB(Long id) {
        return new ValueWrapperFuture<>(clazzZoneProductPersistence.load(id));
    }

    @Override
    public AlpsFuture<VersionedClazzZoneProductData> loadClazzZoneProductBufferData(long version) {
        FlightController.disableLog();
        ClazzZoneProductBuffer buffer = getClazzZoneProductBuffer();
        if (version < buffer.getVersion()) {
            return new ValueWrapperFuture<>(buffer.dump());
        } else {
            return new ValueWrapperFuture<>(null);
        }
    }

    @Override
    public synchronized void reloadClazzZoneProductBuffer() {
        long actualVersion = clazzZoneProductVersion.current();
        ClazzZoneProductBuffer buffer = getClazzZoneProductBuffer();
        long bufferVersion = buffer.getVersion();
        if (bufferVersion != actualVersion) {
            VersionedClazzZoneProductData data = new VersionedClazzZoneProductData();
            data.setVersion(actualVersion);
            data.setClazzZoneProductList(loadAllClazzZoneProductsFromDB().getUninterruptibly());
            buffer.attach(data);
            logger.info("[ClazzZoneProductBuffer] reloaded: [{}] -> [{}]", bufferVersion, actualVersion);
        }
    }

    private final LazyInitializationSupplier<ClazzZoneProductBuffer> clazzZoneProductBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedClazzZoneProductData data = new VersionedClazzZoneProductData();
        data.setVersion(clazzZoneProductVersion.current());
        data.setClazzZoneProductList(loadAllClazzZoneProductsFromDB().getUninterruptibly());
        ClazzZoneProductBuffer buffer = new MDBClazzZoneProductBuffer(mdbClazzZoneProductPersistence);
        buffer.attach(data);
        return buffer;
    });

    @Override
    public ClazzZoneProductBuffer getClazzZoneProductBuffer() {
        return clazzZoneProductBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetClazzZoneProductBuffer() {
        clazzZoneProductBufferSupplier.reset();
    }
}
