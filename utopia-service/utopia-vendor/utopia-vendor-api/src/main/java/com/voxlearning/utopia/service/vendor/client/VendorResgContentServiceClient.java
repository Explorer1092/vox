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
import com.voxlearning.alps.api.event.dsl.CallbackEvent;
import com.voxlearning.alps.api.event.dsl.MinuteTimerEventListener;
import com.voxlearning.alps.api.event.dsl.TimerEvent;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.utopia.service.vendor.api.VendorResgContentService;
import com.voxlearning.utopia.service.vendor.buffer.VendorResgContentBuffer;
import com.voxlearning.utopia.service.vendor.buffer.VersionedVendorResgContentList;
import com.voxlearning.utopia.service.vendor.buffer.internal.JVMVendorResgContentBuffer;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class VendorResgContentServiceClient implements InitializingBean, DisposableBean, VendorResgContentBuffer.Aware {

    private static final Logger LOGGER = LoggerFactory.getLogger(VendorResgContentServiceClient.class);

    @Getter
    @ImportService(interfaceClass = VendorResgContentService.class)
    private VendorResgContentService vendorResgContentService;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (RuntimeMode.isProduction()) {
            EventBus.publish(new CallbackEvent(this::getVendorResgContentBuffer));
        }
    }

    @Override
    public void destroy() throws Exception {
        EventBus.unsubscribe(reloadVendorResgContentBuffer);
    }

    private class ReloadVendorResgContentBuffer extends MinuteTimerEventListener {
        @Override
        protected void processEvent(TimerEvent timerEvent, AlpsEventContext alpsEventContext) {
            if (RuntimeModeLoader.getInstance().isUnitTest()) {
                return;
            }
            VendorResgContentBuffer buffer = getVendorResgContentBuffer();
            long version = buffer.getVersion();
            VersionedVendorResgContentList data = vendorResgContentService.loadVersionedVendorResgContentList(version).getUninterruptibly();
            if (data != null) {
                buffer.attach(data);
                LOGGER.debug("[VendorResgContentBuffer] reloaded: [{}] -> [{}]", version, data.getVersion());
            }
        }
    }

    private final ReloadVendorResgContentBuffer reloadVendorResgContentBuffer = new ReloadVendorResgContentBuffer();

    private final LazyInitializationSupplier<VendorResgContentBuffer> vendorResgContentBufferSupplier
            = new LazyInitializationSupplier<>(() -> {
        VersionedVendorResgContentList data = vendorResgContentService
                .loadVersionedVendorResgContentList(-1)
                .getUninterruptibly();
        assert data != null;
        VendorResgContentBuffer buffer = new JVMVendorResgContentBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadVendorResgContentBuffer);
        LOGGER.debug("[VendorResgContentBuffer] initialized: [{}]", data.getVersion());
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
}
