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
import com.voxlearning.utopia.service.vendor.api.FairylandProductService;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedFairylandProducts;
import com.voxlearning.utopia.service.vendor.buffer.FairylandProductBuffer;
import com.voxlearning.utopia.service.vendor.buffer.internal.JVMFairylandProductBuffer;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class FairylandProductServiceClient implements FairylandProductBuffer.Aware, InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(FairylandProductServiceClient.class);

    @Getter
    @ImportService(interfaceClass = FairylandProductService.class)
    private FairylandProductService fairylandProductService;

    private class ReloadFairylandProductBuffer extends MinuteTimerEventListener {
        @Override
        protected void processEvent(TimerEvent event, AlpsEventContext context) {
            if (RuntimeModeLoader.getInstance().isUnitTest()) {
                return;
            }
            FairylandProductBuffer buffer = getFairylandProductBuffer();
            long version = buffer.getVersion();
            VersionedFairylandProducts data = fairylandProductService.loadFairylandProductBufferData(version).getUninterruptibly();
            if (data != null) {
                buffer.attach(data);
                LOGGER.debug("[FairylandProductBuffer] reloaded: [{}] -> [{}]", version, data.getVersion());
            }
        }
    }

    private final ReloadFairylandProductBuffer reloadFairylandProductBuffer = new ReloadFairylandProductBuffer();

    private final LazyInitializationSupplier<FairylandProductBuffer> fairylandProductBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedFairylandProducts data = fairylandProductService.loadFairylandProductBufferData(-1).getUninterruptibly();
        assert data != null;
        FairylandProductBuffer buffer = new JVMFairylandProductBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadFairylandProductBuffer);
        LOGGER.debug("[FairylandProductBuffer] initialized: [{}]", data.getVersion());
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

    @Override
    public void destroy() throws Exception {
        EventBus.unsubscribe(reloadFairylandProductBuffer);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (RuntimeMode.isProduction()) {
            EventBus.publish(new CallbackEvent(this::getFairylandProductBuffer));
        }
    }
}
