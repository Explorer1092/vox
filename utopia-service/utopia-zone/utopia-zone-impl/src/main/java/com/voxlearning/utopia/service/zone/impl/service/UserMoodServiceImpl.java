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
import com.voxlearning.utopia.service.zone.api.UserMoodService;
import com.voxlearning.utopia.service.zone.api.entity.UserMood;
import com.voxlearning.utopia.service.zone.buffer.UserMoodBuffer;
import com.voxlearning.utopia.service.zone.data.VersionedUserMoodData;
import com.voxlearning.utopia.service.zone.impl.buffer.MDBUserMoodBuffer;
import com.voxlearning.utopia.service.zone.impl.persistence.MDBUserMoodPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.UserMoodPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.UserMoodVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.zone.impl.service.UserMoodServiceImpl")
@ExposeService(interfaceClass = UserMoodService.class)
public class UserMoodServiceImpl extends SpringContainerSupport implements UserMoodService, UserMoodBuffer.Aware {

    @Inject private MDBUserMoodPersistence mdbUserMoodPersistence;
    @Inject private UserMoodPersistence userMoodPersistence;
    @Inject private UserMoodVersion userMoodVersion;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        EventBus.publish(new CallbackEvent(() -> {
            if (RuntimeMode.isUnitTest()) {
                return;
            }
            VersionedUserMoodData data = getUserMoodBuffer().dump();
            logger.info("[UserMoodBuffer] initialized: [{}]", data.getVersion());
        })).awaitUninterruptibly();
    }

    @Override
    public AlpsFuture<List<UserMood>> loadAllUserMoodsFromDB() {
        return new ValueWrapperFuture<>(userMoodPersistence.query());
    }

    @Override
    public AlpsFuture<VersionedUserMoodData> loadUserMoodBufferData(long version) {
        FlightController.disableLog();
        UserMoodBuffer buffer = getUserMoodBuffer();
        if (version < buffer.getVersion()) {
            return new ValueWrapperFuture<>(buffer.dump());
        } else {
            return new ValueWrapperFuture<>(null);
        }
    }

    @Override
    public synchronized void reloadUserMoodBuffer() {
        long actualVersion = userMoodVersion.current();
        UserMoodBuffer buffer = getUserMoodBuffer();
        long bufferVersion = buffer.getVersion();
        if (bufferVersion != actualVersion) {
            VersionedUserMoodData data = new VersionedUserMoodData();
            data.setVersion(actualVersion);
            data.setUserMoodList(loadAllUserMoodsFromDB().getUninterruptibly());
            buffer.attach(data);
            logger.info("[UserMoodBuffer] reloaded: [{}] -> [{}]", bufferVersion, actualVersion);
        }
    }

    private final LazyInitializationSupplier<UserMoodBuffer> userMoodBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedUserMoodData data = new VersionedUserMoodData();
        data.setVersion(userMoodVersion.current());
        data.setUserMoodList(loadAllUserMoodsFromDB().getUninterruptibly());
        UserMoodBuffer buffer = new MDBUserMoodBuffer(mdbUserMoodPersistence);
        buffer.attach(data);
        return buffer;
    });

    @Override
    public UserMoodBuffer getUserMoodBuffer() {
        return userMoodBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetUserMoodBuffer() {
        userMoodBufferSupplier.reset();
    }
}
