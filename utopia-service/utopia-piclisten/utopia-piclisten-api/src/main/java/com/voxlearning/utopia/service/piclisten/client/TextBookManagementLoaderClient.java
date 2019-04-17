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

package com.voxlearning.utopia.service.piclisten.client;

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
import com.voxlearning.utopia.service.piclisten.api.TextBookManagementLoader;
import com.voxlearning.utopia.service.piclisten.buffer.TextBookManagementBuffer;
import com.voxlearning.utopia.service.piclisten.buffer.internal.JVMTextBookManagementBuffer;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedTextBookManagementList;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by jiang wei on 2017/4/5.
 */
public class TextBookManagementLoaderClient implements InitializingBean, DisposableBean,
        TextBookManagementLoader, TextBookManagementBuffer.Aware {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextBookManagementLoaderClient.class);

    @ImportService(interfaceClass = TextBookManagementLoader.class)
    private TextBookManagementLoader textBookManagementLoader;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (RuntimeMode.isProduction()) {
            EventBus.publish(new CallbackEvent(this::getTextBookManagementBuffer));
        }
    }

    @Override
    public void destroy() throws Exception {
        EventBus.unsubscribe(reloadTextBookManagementBuffer);
    }

    private class ReloadTextBookManagementBuffer extends MinuteTimerEventListener {
        @Override
        protected void processEvent(TimerEvent event, AlpsEventContext context) {
            if (RuntimeModeLoader.getInstance().isUnitTest()) {
                return;
            }
            TextBookManagementBuffer b = getTextBookManagementBuffer();
            long version = b.getVersion();
            VersionedTextBookManagementList data = loadVersionedTextBookManagementList(version);
            if (data != null) {
                b.attach(data);
                LOGGER.debug("[TextBookManagement] reloaded: [{}] -> [{}]", version, data.getVersion());
            }
        }
    }

    private final ReloadTextBookManagementBuffer reloadTextBookManagementBuffer = new ReloadTextBookManagementBuffer();

    private final LazyInitializationSupplier<TextBookManagementBuffer> textBookManagementBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedTextBookManagementList data = loadVersionedTextBookManagementList(0);
        JVMTextBookManagementBuffer buffer = new JVMTextBookManagementBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadTextBookManagementBuffer);
        LOGGER.debug("[TextBookManagementBuffer] initialized: [{}]", data.getVersion());
        return buffer;
    });

    @Override
    public TextBookManagementBuffer getTextBookManagementBuffer() {
        return textBookManagementBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetTextBookManagementBuffer() {
        textBookManagementBufferSupplier.reset();
    }

    @Override
    public VersionedTextBookManagementList loadVersionedTextBookManagementList(long version) {
        return textBookManagementLoader.loadVersionedTextBookManagementList(version);
    }

    @Override
    public Map<String, TextBookManagement> getTextBookByIds(Collection<String> ids) {
        return getTextBookManagementBuffer().loadByIds(ids);
    }


    @Override
    public List<TextBookMapper> getPublisherList() {
        return getTextBookManagementBuffer().getTextBookMapperList();
    }

    @Override
    public Map<Integer, List<TextBookManagement>> getClazzLevelMap() {
        return getTextBookManagementBuffer().getClazzLevelMap();
    }

    @Override
    public List<TextBookManagement> getTextBookManagementList() {
        return getTextBookManagementBuffer().getTextBookManagementList();
    }
}
