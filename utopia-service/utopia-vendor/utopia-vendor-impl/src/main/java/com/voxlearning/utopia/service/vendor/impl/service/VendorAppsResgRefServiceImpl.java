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
import com.voxlearning.utopia.service.vendor.api.VendorAppsResgRefService;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsResgRef;
import com.voxlearning.utopia.service.vendor.buffer.VendorAppsResgRefBuffer;
import com.voxlearning.utopia.service.vendor.buffer.VersionedVendorAppsResgRefList;
import com.voxlearning.utopia.service.vendor.impl.buffer.MDBVendorAppsResgRefBuffer;
import com.voxlearning.utopia.service.vendor.impl.persistence.MDBVendorAppsResgRefPersistence;
import com.voxlearning.utopia.service.vendor.impl.persistence.VendorAppsResgRefPersistence;
import com.voxlearning.utopia.service.vendor.impl.version.VendorAppsResgRefVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.vendor.impl.service.VendorAppsResgRefServiceImpl")
@ExposeService(interfaceClass = VendorAppsResgRefService.class)
public class VendorAppsResgRefServiceImpl extends SpringContainerSupport implements VendorAppsResgRefService, VendorAppsResgRefBuffer.Aware {

    @Inject private MDBVendorAppsResgRefPersistence mdbVendorAppsResgRefPersistence;
    @Inject private VendorAppsResgRefPersistence vendorAppsResgRefPersistence;
    @Inject private VendorAppsResgRefVersion vendorAppsResgRefVersion;

    private final LazyInitializationSupplier<MDBVendorAppsResgRefBuffer> vendorAppsResgRefBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedVendorAppsResgRefList data = new VersionedVendorAppsResgRefList();
        data.setVersion(vendorAppsResgRefVersion.current());
        data.setRefList(loadAllVendorAppsResgRefsFromDB().getUninterruptibly());
        MDBVendorAppsResgRefBuffer buffer = new MDBVendorAppsResgRefBuffer(mdbVendorAppsResgRefPersistence);
        buffer.attach(data);
        return buffer;
    });

    @Override
    public VendorAppsResgRefBuffer getVendorAppsResgRefBuffer() {
        return vendorAppsResgRefBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetVendorAppsResgRefBuffer() {
        vendorAppsResgRefBufferSupplier.reset();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        EventBus.publish(new CallbackEvent(() -> {
            if (RuntimeMode.isUnitTest()) {
                return;
            }
            VersionedVendorAppsResgRefList data = getVendorAppsResgRefBuffer().dump();
            logger.info("[VendorAppsResgRefBuffer] initialized: [{}]", data.getVersion());
        })).awaitUninterruptibly();
    }

    @Override
    public AlpsFuture<List<VendorAppsResgRef>> loadAllVendorAppsResgRefsFromBuffer() {
        return new ValueWrapperFuture<>(getVendorAppsResgRefBuffer().dump().getRefList());
    }

    @Override
    public AlpsFuture<List<VendorAppsResgRef>> findVendorAppsResgRefsByAppKeyFromBuffer(String appKey) {
        return new ValueWrapperFuture<>(getVendorAppsResgRefBuffer().findByAppKey(appKey));
    }

    @Override
    public AlpsFuture<List<VendorAppsResgRef>> loadAllVendorAppsResgRefsFromDB() {
        return new ValueWrapperFuture<>(vendorAppsResgRefPersistence.query());
    }

    @Override
    public synchronized void reloadVendorAppsResgRefBuffer() {
        long actualVersion = vendorAppsResgRefVersion.current();
        VendorAppsResgRefBuffer buffer = getVendorAppsResgRefBuffer();
        long bufferVersion = buffer.getVersion();
        if (bufferVersion != actualVersion) {
            VersionedVendorAppsResgRefList data = new VersionedVendorAppsResgRefList();
            data.setVersion(actualVersion);
            data.setRefList(loadAllVendorAppsResgRefsFromDB().getUninterruptibly());
            buffer.attach(data);
            logger.info("[VendorAppsResgRefBuffer] reloaded: [{}] -> [{}]", bufferVersion, actualVersion);
        }
    }
}
