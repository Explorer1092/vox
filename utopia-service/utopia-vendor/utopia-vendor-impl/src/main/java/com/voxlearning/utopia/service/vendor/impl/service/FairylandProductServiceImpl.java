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

package com.voxlearning.utopia.service.vendor.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.alps.api.event.dsl.CallbackEvent;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.monitor.FlightController;
import com.voxlearning.utopia.service.vendor.api.FairylandProductService;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedFairylandProducts;
import com.voxlearning.utopia.service.vendor.buffer.FairylandProductBuffer;
import com.voxlearning.utopia.service.vendor.impl.buffer.MDBFairylandProductBuffer;
import com.voxlearning.utopia.service.vendor.impl.persistence.FairylandProductPersistence;
import com.voxlearning.utopia.service.vendor.impl.persistence.FairylandProductVersion;
import com.voxlearning.utopia.service.vendor.impl.persistence.MDBFairylandProductPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named("com.voxlearning.utopia.service.vendor.impl.service.FairylandProductServiceImpl")
@ExposeService(interfaceClass = FairylandProductService.class)
public class FairylandProductServiceImpl extends SpringContainerSupport implements FairylandProductService, FairylandProductBuffer.Aware {

    @Inject private FairylandProductPersistence fairylandProductPersistence;
    @Inject private FairylandProductVersion fairylandProductVersion;
    @Inject private MDBFairylandProductPersistence mdbFairylandProductPersistence;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        EventBus.publish(new CallbackEvent(() -> {
            if (RuntimeMode.isUnitTest()) {
                return;
            }
            VersionedFairylandProducts data = getFairylandProductBuffer().dump();
            logger.info("[FairylandProductBuffer] initialized: [{}]", data.getVersion());
        })).awaitUninterruptibly();
    }

    @Override
    public AlpsFuture<List<FairylandProduct>> loadAllFairylandProductsFromDB() {
        return new ValueWrapperFuture<>(fairylandProductPersistence.query());
    }

    @Override
    public AlpsFuture<FairylandProduct> loadFairylandProductFromDB(Long id) {
        return new ValueWrapperFuture<>(fairylandProductPersistence.load(id));
    }

    @Override
    public AlpsFuture<VersionedFairylandProducts> loadFairylandProductBufferData(long version) {
        FlightController.disableLog();
        FairylandProductBuffer buffer = getFairylandProductBuffer();
        if (version < buffer.getVersion()) {
            return new ValueWrapperFuture<>(buffer.dump());
        } else {
            return ValueWrapperFuture.nullInst();
        }
    }

    @Override
    public synchronized void reloadFairylandProductBuffer() {
        long actualVersion = fairylandProductVersion.current();
        FairylandProductBuffer buffer = getFairylandProductBuffer();
        long bufferVersion = buffer.getVersion();
        if (bufferVersion != actualVersion) {
            VersionedFairylandProducts data = new VersionedFairylandProducts();
            data.setVersion(actualVersion);
            data.setFairylandProducts(loadAllFairylandProductsFromDB().getUninterruptibly()
                    .stream()
                    .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                    .collect(Collectors.toList()));
            buffer.attach(data);
            logger.info("[FairylandProductBuffer] reloaded: [{}] -> [{}]", bufferVersion, actualVersion);
        }
    }

    private final LazyInitializationSupplier<FairylandProductBuffer> fairylandProductBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedFairylandProducts data = new VersionedFairylandProducts();
        data.setVersion(fairylandProductVersion.current());
        data.setFairylandProducts(loadAllFairylandProductsFromDB().getUninterruptibly()
                .stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .collect(Collectors.toList()));
        FairylandProductBuffer buffer = new MDBFairylandProductBuffer(mdbFairylandProductPersistence);
        buffer.attach(data);
        return buffer;
    });

    @Override
    public FairylandProductBuffer getFairylandProductBuffer() {
        return fairylandProductBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetFairylandProductBuffer() {
        fairylandProductBufferSupplier.reset();
    }
}
