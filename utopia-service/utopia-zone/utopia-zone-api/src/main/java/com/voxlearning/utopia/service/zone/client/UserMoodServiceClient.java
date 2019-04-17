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
import com.voxlearning.utopia.service.zone.api.UserMoodService;
import com.voxlearning.utopia.service.zone.buffer.UserMoodBuffer;
import com.voxlearning.utopia.service.zone.buffer.internal.JVMUserMoodBuffer;
import com.voxlearning.utopia.service.zone.data.VersionedUserMoodData;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class UserMoodServiceClient implements UserMoodBuffer.Aware, InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserMoodServiceClient.class);

    @Getter
    @ImportService(interfaceClass = UserMoodService.class)
    private UserMoodService userMoodService;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (RuntimeMode.isProduction()) {
            EventBus.publish(new CallbackEvent(this::getUserMoodBuffer));
        }
    }

    private class ReloadUserMoodBuffer extends MinuteTimerEventListener {
        @Override
        protected void processEvent(TimerEvent event, AlpsEventContext context) {
            if (RuntimeMode.isUnitTest()) {
                return;
            }
            UserMoodBuffer buffer = getUserMoodBuffer();
            long version = buffer.getVersion();
            VersionedUserMoodData data = userMoodService.loadUserMoodBufferData(version).getUninterruptibly();
            if (data != null) {
                buffer.attach(data);
                LOGGER.info("[UserMoodBuffer] reloaded: [{}] -> [{}]", version, data.getVersion());
            }
        }
    }

    private final ReloadUserMoodBuffer reloadUserMoodBuffer = new ReloadUserMoodBuffer();

    private final LazyInitializationSupplier<UserMoodBuffer> userMoodBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedUserMoodData data = userMoodService.loadUserMoodBufferData(-1).getUninterruptibly();
        assert data != null;
        UserMoodBuffer buffer = new JVMUserMoodBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadUserMoodBuffer);
        LOGGER.info("[UserMoodBuffer] initialized: [{}]", data.getVersion());
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

    @Override
    public void destroy() throws Exception {
        EventBus.unsubscribe(reloadUserMoodBuffer);
    }
}
