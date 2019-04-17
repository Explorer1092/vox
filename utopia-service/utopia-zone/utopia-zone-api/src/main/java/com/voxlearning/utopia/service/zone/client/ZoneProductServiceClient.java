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

package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.event.AlpsEventContext;
import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.alps.api.event.dsl.CallbackEvent;
import com.voxlearning.alps.api.event.dsl.MinuteTimerEventListener;
import com.voxlearning.alps.api.event.dsl.TimerEvent;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.zone.api.ZoneProductService;
import com.voxlearning.utopia.service.zone.buffer.ClazzZoneProductBuffer;
import com.voxlearning.utopia.service.zone.buffer.internal.JVMClazzZoneProductBuffer;
import com.voxlearning.utopia.service.zone.data.VersionedClazzZoneProductData;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class ZoneProductServiceClient implements ClazzZoneProductBuffer.Aware, InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZoneProductServiceClient.class);

    @Getter
    @ImportService(interfaceClass = ZoneProductService.class)
    private ZoneProductService zoneProductService;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (RuntimeMode.isProduction()) {
            EventBus.publish(new CallbackEvent(this::getClazzZoneProductBuffer));
        }
    }

    private class ReloadClazzZoneProductBuffer extends MinuteTimerEventListener {
        @Override
        protected void processEvent(TimerEvent event, AlpsEventContext context) {
            if (RuntimeMode.isUnitTest()) {
                return;
            }
            ClazzZoneProductBuffer buffer = getClazzZoneProductBuffer();
            long version = buffer.getVersion();
            VersionedClazzZoneProductData data = zoneProductService.loadClazzZoneProductBufferData(version).getUninterruptibly();
            if (data != null) {
                buffer.attach(data);
                LOGGER.info("[ClazzZoneProductBuffer] reloaded: [{}] -> [{}]", version, data.getVersion());
            }
        }
    }

    private final ReloadClazzZoneProductBuffer reloadClazzZoneProductBuffer = new ReloadClazzZoneProductBuffer();

    private final LazyInitializationSupplier<ClazzZoneProductBuffer> clazzZoneProductBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedClazzZoneProductData data = zoneProductService.loadClazzZoneProductBufferData(-1).getUninterruptibly();
        assert data != null;
        ClazzZoneProductBuffer buffer = new JVMClazzZoneProductBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadClazzZoneProductBuffer);
        LOGGER.info("[ClazzZoneProductBuffer] initialized: [{}]", data.getVersion());
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

    @Override
    public void destroy() throws Exception {
        EventBus.unsubscribe(reloadClazzZoneProductBuffer);
    }
}
