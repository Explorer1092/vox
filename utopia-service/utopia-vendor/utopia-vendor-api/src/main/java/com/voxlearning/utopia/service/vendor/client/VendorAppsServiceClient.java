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

package com.voxlearning.utopia.service.vendor.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.event.AlpsEventContext;
import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.alps.api.event.dsl.MinuteTimerEventListener;
import com.voxlearning.alps.api.event.dsl.TimerEvent;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.utopia.service.vendor.api.VendorAppsService;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedVendorAppsList;
import com.voxlearning.utopia.service.vendor.buffer.VendorAppsBuffer;
import com.voxlearning.utopia.service.vendor.buffer.internal.JVMVendorAppsBuffer;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;

public class VendorAppsServiceClient implements VendorAppsBuffer.Aware, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(VendorAppsServiceClient.class);

    @Getter
    @ImportService(interfaceClass = VendorAppsService.class)
    private VendorAppsService vendorAppsService;

    // ========================================================================
    // VendorAppsBufferAware
    // ========================================================================

    private class ReloadVendorAppsBuffer extends MinuteTimerEventListener {
        @Override
        protected void processEvent(TimerEvent event, AlpsEventContext context) {
            if (RuntimeModeLoader.getInstance().isUnitTest()) {
                return;
            }
            VendorAppsBuffer buffer = getVendorAppsBuffer();
            long bufferVersion = buffer.getVersion();
            VersionedVendorAppsList data = vendorAppsService.loadVersionedVendorAppsList(bufferVersion).getUninterruptibly();
            if (data != null) {
                buffer.attach(data);
                LOGGER.debug("[VendorAppsBuffer] reloaded: [{}] -> [{}]", bufferVersion, data.getVersion());
            }
        }
    }

    private final ReloadVendorAppsBuffer reloadVendorAppsBuffer = new ReloadVendorAppsBuffer();

    private final LazyInitializationSupplier<JVMVendorAppsBuffer> vendorAppsBufferSupplier = new LazyInitializationSupplier<JVMVendorAppsBuffer>(() -> {
        VersionedVendorAppsList data = vendorAppsService.loadVersionedVendorAppsList(-1).getUninterruptibly();
        assert data != null;
        JVMVendorAppsBuffer buffer = new JVMVendorAppsBuffer();
        buffer.attach(data);
        LOGGER.debug("[VendorAppsBuffer] initialized: [{}]", data.getVersion());
        EventBus.subscribe(reloadVendorAppsBuffer);
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

    @Override
    public void destroy() throws Exception {
        EventBus.unsubscribe(reloadVendorAppsBuffer);
    }
}
