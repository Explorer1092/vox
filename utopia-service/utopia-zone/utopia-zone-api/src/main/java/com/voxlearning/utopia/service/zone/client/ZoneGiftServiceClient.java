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
import com.voxlearning.utopia.service.zone.api.ZoneGiftService;
import com.voxlearning.utopia.service.zone.buffer.GiftBuffer;
import com.voxlearning.utopia.service.zone.buffer.internal.JVMGiftBuffer;
import com.voxlearning.utopia.service.zone.data.VersionedGiftData;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class ZoneGiftServiceClient implements GiftBuffer.Aware, InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZoneGiftServiceClient.class);

    @Getter
    @ImportService(interfaceClass = ZoneGiftService.class)
    private ZoneGiftService zoneGiftService;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (RuntimeMode.isProduction()) {
            EventBus.publish(new CallbackEvent(this::getGiftBuffer));
        }
    }

    private class ReloadGiftBuffer extends MinuteTimerEventListener {
        @Override
        protected void processEvent(TimerEvent event, AlpsEventContext context) {
            if (RuntimeMode.isUnitTest()) {
                return;
            }
            GiftBuffer buffer = getGiftBuffer();
            long version = buffer.getVersion();
            VersionedGiftData data = zoneGiftService.loadGiftBufferData(version).getUninterruptibly();
            if (data != null) {
                buffer.attach(data);
                LOGGER.info("[GiftBuffer] reloaded: [{}] -> [{}]", version, data.getVersion());
            }
        }
    }

    private final ReloadGiftBuffer reloadGiftBuffer = new ReloadGiftBuffer();

    private final LazyInitializationSupplier<GiftBuffer> giftBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedGiftData data = zoneGiftService.loadGiftBufferData(-1).getUninterruptibly();
        assert data != null;
        GiftBuffer buffer = new JVMGiftBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadGiftBuffer);
        LOGGER.info("[GiftBuffer] initialized: [{}]", data.getVersion());
        return buffer;
    });

    @Override
    public GiftBuffer getGiftBuffer() {
        return giftBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetGiftBuffer() {
        giftBufferSupplier.reset();
    }

    @Override
    public void destroy() throws Exception {
        EventBus.unsubscribe(reloadGiftBuffer);
    }
}
