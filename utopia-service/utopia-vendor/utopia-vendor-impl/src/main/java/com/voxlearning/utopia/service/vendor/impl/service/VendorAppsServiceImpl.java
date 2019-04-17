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
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.monitor.FlightController;
import com.voxlearning.utopia.service.vendor.api.VendorAppsService;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedVendorAppsList;
import com.voxlearning.utopia.service.vendor.buffer.VendorAppsBuffer;
import com.voxlearning.utopia.service.vendor.impl.buffer.MDBVendorAppsBuffer;
import com.voxlearning.utopia.service.vendor.impl.persistence.MDBVendorAppsPersistence;
import com.voxlearning.utopia.service.vendor.impl.persistence.VendorAppsPersistence;
import com.voxlearning.utopia.service.vendor.impl.persistence.VendorAppsVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.vendor.impl.service.VendorAppsServiceImpl")
@ExposeService(interfaceClass = VendorAppsService.class)
public class VendorAppsServiceImpl extends SpringContainerSupport implements VendorAppsService, VendorAppsBuffer.Aware {

    @Inject private MDBVendorAppsPersistence mdbVendorAppsPersistence;
    @Inject private VendorAppsVersion vendorAppsVersion;
    @Inject private VendorAppsPersistence vendorAppsPersistence;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        EventBus.publish(new CallbackEvent(() -> {
            if (RuntimeMode.isUnitTest()) {
                return;
            }
            VersionedVendorAppsList data = getVendorAppsBuffer().dump();
            logger.info("[VendorAppsBuffer] initialized: [{}]", data.getVersion());
        })).awaitUninterruptibly();
    }

    @Override
    public AlpsFuture<VersionedVendorAppsList> loadVersionedVendorAppsList(long version) {
        FlightController.disableLog();
        VersionedVendorAppsList data = getVendorAppsBuffer().dump();
        return (version < data.getVersion()) ? new ValueWrapperFuture<>(data) : new ValueWrapperFuture<>(null);
    }

    @Override
    public AlpsFuture<List<VendorApps>> loadAllVendorAppsFromDB() {
        return new ValueWrapperFuture<>(vendorAppsPersistence.query());
    }

    @Override
    public synchronized void reloadVendorAppsBuffer() {
        long actualVersion = vendorAppsVersion.current();
        VendorAppsBuffer buffer = getVendorAppsBuffer();
        long bufferVersion = buffer.getVersion();
        if (bufferVersion != actualVersion) {
            VersionedVendorAppsList data = new VersionedVendorAppsList();
            data.setVersion(actualVersion);
            data.setVendorAppsList(loadAllVendorAppsFromDB().getUninterruptibly());
            buffer.attach(data);
            logger.info("[VendorAppsBuffer] reloaded: [{}] -> [{}]", bufferVersion, actualVersion);
        }
    }

    private final LazyInitializationSupplier<VendorAppsBuffer> vendorAppsBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedVendorAppsList data = new VersionedVendorAppsList();
        data.setVersion(vendorAppsVersion.current());
        data.setVendorAppsList(loadAllVendorAppsFromDB().getUninterruptibly());
        VendorAppsBuffer buffer = new MDBVendorAppsBuffer(mdbVendorAppsPersistence);
        buffer.attach(data);
        return buffer;
    });

    @Override
    public VendorAppsBuffer getVendorAppsBuffer() {
        return vendorAppsBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetVendorAppsBuffer() {
        vendorAppsBufferSupplier.reset();
    }
}
