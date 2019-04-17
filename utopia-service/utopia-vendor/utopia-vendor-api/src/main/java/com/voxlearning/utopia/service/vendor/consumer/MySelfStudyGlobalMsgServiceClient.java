package com.voxlearning.utopia.service.vendor.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.event.AlpsEventContext;
import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.alps.api.event.dsl.CallbackEvent;
import com.voxlearning.alps.api.event.dsl.MinuteTimerEventListener;
import com.voxlearning.alps.api.event.dsl.TimerEvent;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyGlobalMsgService;
import com.voxlearning.utopia.service.vendor.api.entity.MySelfStudyEntryGlobalMsg;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedMySelfStudyGlobalMsgMap;
import com.voxlearning.utopia.service.vendor.buffer.MySelfStudyEntryLabelBuffer;
import com.voxlearning.utopia.service.vendor.buffer.internal.JVMMySelfStudyEntryLabelBuffer;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * @author jiangpeng
 * @since 2017-06-14 下午5:37
 **/
public class MySelfStudyGlobalMsgServiceClient implements MySelfStudyGlobalMsgService, MySelfStudyEntryLabelBuffer.Aware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MySelfStudyGlobalMsgServiceClient.class);

    @ImportService(interfaceClass = MySelfStudyGlobalMsgService.class)
    private MySelfStudyGlobalMsgService mySelfStudyGlobalMsgService;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!RuntimeModeLoader.getInstance().isUnitTest()) {
            EventBus.publish(new CallbackEvent(this::getMySelfStudyEntryLabelBuffer));
        }
    }

    private class ReloadMySelfStudyEntryLabelBuffer extends MinuteTimerEventListener {
        @Override
        protected void processEvent(TimerEvent event, AlpsEventContext context) {
            if (RuntimeModeLoader.getInstance().isUnitTest()) {
                return;
            }
            MySelfStudyEntryLabelBuffer b = getMySelfStudyEntryLabelBuffer();
            long version = b.getVersion();
            VersionedMySelfStudyGlobalMsgMap data = loadVersionedMySelfStudyEntryLabelMap(version);
            if (data != null) {
                b.attach(data);
                LOGGER.info("[MySelfStudyEntryLabelBuffer] reloaded:  [{}] -> [{}]", version, data.getVersion());
            }
        }
    }

    private final ReloadMySelfStudyEntryLabelBuffer reloadMySelfStudyEntryLabelBuffer = new ReloadMySelfStudyEntryLabelBuffer();

    private final LazyInitializationSupplier<MySelfStudyEntryLabelBuffer> mySelfStudyEntryLabelBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedMySelfStudyGlobalMsgMap data = loadVersionedMySelfStudyEntryLabelMap(0);
        JVMMySelfStudyEntryLabelBuffer buffer = new JVMMySelfStudyEntryLabelBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadMySelfStudyEntryLabelBuffer);
        LOGGER.info("[MySelfStudyEntryLabelBuffer] initialized: [{}]", data.getVersion());
        return buffer;
    });

    @Override
    public MySelfStudyEntryLabelBuffer getMySelfStudyEntryLabelBuffer() {
        return mySelfStudyEntryLabelBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetMySelfStudyEntryLabelBuffer() {
        mySelfStudyEntryLabelBufferSupplier.reset();
    }

    @Override
    public VersionedMySelfStudyGlobalMsgMap loadVersionedMySelfStudyEntryLabelMap(long version) {
        return mySelfStudyGlobalMsgService.loadVersionedMySelfStudyEntryLabelMap(version);
    }

    @Override
    public Map<SelfStudyType, MySelfStudyEntryGlobalMsg> getMySelfStudyEntryGlobalMsgMap() {
        return getMySelfStudyEntryLabelBuffer().getMySelfStudyEntryReminderMap();
    }
}
