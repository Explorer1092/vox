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
import com.voxlearning.utopia.service.vendor.api.VendorResgContentService;
import com.voxlearning.utopia.service.vendor.api.entity.VendorResgContent;
import com.voxlearning.utopia.service.vendor.buffer.VendorResgContentBuffer;
import com.voxlearning.utopia.service.vendor.buffer.VersionedVendorResgContentList;
import com.voxlearning.utopia.service.vendor.impl.buffer.MDBVendorResgContentBuffer;
import com.voxlearning.utopia.service.vendor.impl.dao.VendorResgContentPersistence;
import com.voxlearning.utopia.service.vendor.impl.persistence.MDBVendorResgContentPersistence;
import com.voxlearning.utopia.service.vendor.impl.persistence.VendorResgContentVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.vendor.impl.service.VendorResgContentServiceImpl")
@ExposeService(interfaceClass = VendorResgContentService.class)
public class VendorResgContentServiceImpl extends SpringContainerSupport implements VendorResgContentService, VendorResgContentBuffer.Aware {

    @Inject private MDBVendorResgContentPersistence mdbVendorResgContentPersistence;
    @Inject private VendorResgContentVersion vendorResgContentVersion;
    @Inject private VendorResgContentPersistence vendorResgContentPersistence;

    private final LazyInitializationSupplier<VendorResgContentBuffer> vendorResgContentBufferSupplier
            = new LazyInitializationSupplier<>(() -> {
        VersionedVendorResgContentList data = new VersionedVendorResgContentList();
        data.setVersion(vendorResgContentVersion.current());
        data.setContentList(loadAllVendorResgContentsFromDB().getUninterruptibly());
        VendorResgContentBuffer buffer = new MDBVendorResgContentBuffer(mdbVendorResgContentPersistence);
        buffer.attach(data);
        return buffer;
    });

    @Override
    public VendorResgContentBuffer getVendorResgContentBuffer() {
        return vendorResgContentBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetVendorResgContentBuffer() {
        vendorResgContentBufferSupplier.reset();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        EventBus.publish(new CallbackEvent(() -> {
            if (RuntimeMode.isUnitTest()) {
                return;
            }
            VersionedVendorResgContentList data = getVendorResgContentBuffer().dump();
            logger.info("[VendorResgContentBuffer] initialized: [{}]", data.getVersion());
        })).awaitUninterruptibly();
    }

    @Override
    public AlpsFuture<VersionedVendorResgContentList> loadVersionedVendorResgContentList(long version) {
        FlightController.disableLog();
        VersionedVendorResgContentList data = getVendorResgContentBuffer().dump();
        return (version < data.getVersion()) ? new ValueWrapperFuture<>(data) : new ValueWrapperFuture<>(null);
    }

    @Override
    public AlpsFuture<List<VendorResgContent>> loadAllVendorResgContentsFromDB() {
        return new ValueWrapperFuture<>(vendorResgContentPersistence.query());
    }

    @Override
    public synchronized void reloadVendorResgContentBuffer() {
        long actualVersion = vendorResgContentVersion.current();
        VendorResgContentBuffer buffer = getVendorResgContentBuffer();
        long bufferVersion = buffer.getVersion();
        if (bufferVersion != actualVersion) {
            VersionedVendorResgContentList data = new VersionedVendorResgContentList();
            data.setVersion(actualVersion);
            data.setContentList(loadAllVendorResgContentsFromDB().getUninterruptibly());
            buffer.attach(data);
            logger.info("[VendorResgContentBuffer] reloaded [{}] -> [{}]", bufferVersion, actualVersion);
        }
    }
}
