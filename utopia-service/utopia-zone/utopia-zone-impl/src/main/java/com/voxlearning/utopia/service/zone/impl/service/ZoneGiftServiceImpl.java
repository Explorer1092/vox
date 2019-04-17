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
import com.voxlearning.utopia.service.zone.api.ZoneGiftService;
import com.voxlearning.utopia.service.zone.api.entity.Gift;
import com.voxlearning.utopia.service.zone.buffer.GiftBuffer;
import com.voxlearning.utopia.service.zone.data.VersionedGiftData;
import com.voxlearning.utopia.service.zone.impl.buffer.MDBGiftBuffer;
import com.voxlearning.utopia.service.zone.impl.persistence.GiftPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.GiftVersion;
import com.voxlearning.utopia.service.zone.impl.persistence.MDBGiftPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.zone.impl.service.ZoneGiftServiceImpl")
@ExposeService(interfaceClass = ZoneGiftService.class)
public class ZoneGiftServiceImpl extends SpringContainerSupport implements ZoneGiftService, GiftBuffer.Aware {

    @Inject private GiftPersistence giftPersistence;
    @Inject private GiftVersion giftVersion;
    @Inject private MDBGiftPersistence mdbGiftPersistence;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        EventBus.publish(new CallbackEvent(() -> {
            if (RuntimeMode.isUnitTest()) {
                return;
            }
            VersionedGiftData data = getGiftBuffer().dump();
            logger.info("[GiftBuffer] initialized: [{}]", data.getVersion());
        })).awaitUninterruptibly();
    }

    @Override
    public AlpsFuture<List<Gift>> loadAllGiftsFromDB() {
        return new ValueWrapperFuture<>(giftPersistence.query());
    }

    @Override
    public AlpsFuture<Gift> loadGiftFromDB(Long id) {
        return new ValueWrapperFuture<>(giftPersistence.load(id));
    }

    @Override
    public AlpsFuture<VersionedGiftData> loadGiftBufferData(long version) {
        FlightController.disableLog();
        GiftBuffer buffer = getGiftBuffer();
        if (version < buffer.getVersion()) {
            return new ValueWrapperFuture<>(buffer.dump());
        } else {
            return new ValueWrapperFuture<>(null);
        }
    }

    @Override
    public synchronized void reloadGiftBuffer() {
        long actualVersion = giftVersion.current();
        GiftBuffer buffer = getGiftBuffer();
        long bufferVersion = buffer.getVersion();
        if (bufferVersion != actualVersion) {
            VersionedGiftData data = new VersionedGiftData();
            data.setVersion(actualVersion);
            data.setGiftList(loadAllGiftsFromDB().getUninterruptibly());
            buffer.attach(data);
            logger.info("[GiftBuffer] reloaded: [{}] -> [{}]", bufferVersion, actualVersion);
        }
    }

    private final LazyInitializationSupplier<GiftBuffer> giftBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedGiftData data = new VersionedGiftData();
        data.setVersion(giftVersion.current());
        data.setGiftList(loadAllGiftsFromDB().getUninterruptibly());
        GiftBuffer buffer = new MDBGiftBuffer(mdbGiftPersistence);
        buffer.attach(data);
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
}
