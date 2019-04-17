package com.voxlearning.utopia.service.vendor.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyGlobalMsgService;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastIndexRefinedLessons;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastIndexRemind;
import com.voxlearning.utopia.service.vendor.api.entity.MySelfStudyEntryGlobalMsg;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedMySelfStudyGlobalMsgMap;
import com.voxlearning.utopia.service.vendor.buffer.MySelfStudyEntryLabelBuffer;
import com.voxlearning.utopia.service.vendor.buffer.internal.JVMMySelfStudyEntryLabelBuffer;
import com.voxlearning.utopia.service.vendor.impl.dao.MySelfStudyEntryReminderDao;
import com.voxlearning.utopia.service.vendor.impl.version.MySelfStudyGlobalMsgVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2017-06-21 上午11:46
 **/
@ExposeService(interfaceClass = MySelfStudyGlobalMsgService.class)
@Named
public class MySelfStudyGlobalMsgServiceImpl extends SpringContainerSupport implements MySelfStudyGlobalMsgService, MySelfStudyEntryLabelBuffer.Aware {

    @Inject
    private MySelfStudyEntryReminderDao mySelfStudyEntryReminderDao;

    @Inject
    private MySelfStudyGlobalMsgVersion mySelfStudyGlobalMsgVersion;

    @Override
    protected void afterPropertiesSetCallback() throws Exception {
        super.afterPropertiesSetCallback();
        long version = getMySelfStudyEntryLabelBuffer().getVersion();
        logger.info("[MySelfStudyEntryLabelBuffer] initialized: [{}]", version);
    }

    private final LazyInitializationSupplier<MySelfStudyEntryLabelBuffer> mySelfStudyEntryLabelBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedMySelfStudyGlobalMsgMap data = loadVersionedMySelfStudyGlobalMsgMap();
        JVMMySelfStudyEntryLabelBuffer buffer = new JVMMySelfStudyEntryLabelBuffer();
        buffer.attach(data);
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
    public Map<SelfStudyType, MySelfStudyEntryGlobalMsg> getMySelfStudyEntryGlobalMsgMap() {
        return getMySelfStudyEntryLabelBuffer().getMySelfStudyEntryReminderMap();
    }

    @Override
    public VersionedMySelfStudyGlobalMsgMap loadVersionedMySelfStudyEntryLabelMap(long version) {
        FlightRecorder.closeLog();
        VersionedMySelfStudyGlobalMsgMap data = getMySelfStudyEntryLabelBuffer().dump();
        return (version == 0 || version < data.getVersion()) ? data : null;
    }

    public void $upsetMySelfStudyGlobalMsg(SelfStudyType selfStudyType, String msg) {
        if (selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN)
            return;
        MySelfStudyEntryGlobalMsg selfStudyEntryGlobalMsg = new MySelfStudyEntryGlobalMsg();
        selfStudyEntryGlobalMsg.setId(selfStudyType.name());
        selfStudyEntryGlobalMsg.setText(msg);
        selfStudyEntryGlobalMsg.setReminderId(SafeConverter.toString(System.currentTimeMillis()));
        //做staging和线上的分开处理
        MySelfStudyEntryGlobalMsg upsert = mySelfStudyEntryReminderDao.upsert(selfStudyEntryGlobalMsg);
        if (upsert != null)
            mySelfStudyGlobalMsgVersion.increase();
    }


    public VersionedMySelfStudyGlobalMsgMap loadVersionedMySelfStudyGlobalMsgMap() {
        return new VersionedMySelfStudyGlobalMsgMap(mySelfStudyGlobalMsgVersion.currentVersion(), $loadMySelfStudyGlobalMsgMap());
    }

    private Map<SelfStudyType, MySelfStudyEntryGlobalMsg> $loadMySelfStudyGlobalMsgMap() {
        List<MySelfStudyEntryGlobalMsg> all = mySelfStudyEntryReminderDao.getAll();
        Map<SelfStudyType, MySelfStudyEntryGlobalMsg> map = new HashMap<>();
        all.forEach(t -> {
            SelfStudyType selfStudyType = SelfStudyType.of(t.getId());
            if (selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN)
                return;
            map.put(selfStudyType, t);
        });
        return map;
    }

    public void $updateLiveCastIndexRemind(LiveCastIndexRemind liveCastIndexRemind) {
        MySelfStudyEntryGlobalMsg selfStudyEntryGlobalMsg = new MySelfStudyEntryGlobalMsg();
        selfStudyEntryGlobalMsg.setId(SelfStudyType.LIVECAST.name());
        selfStudyEntryGlobalMsg.setIndexRemind(liveCastIndexRemind);
        MySelfStudyEntryGlobalMsg upsert = mySelfStudyEntryReminderDao.upsert(selfStudyEntryGlobalMsg);
        if (upsert != null)
            mySelfStudyGlobalMsgVersion.increase();
    }

    public void $updateLiveCastRefinedLessons(LiveCastIndexRefinedLessons liveCastIndexRefinedLessons) {
        MySelfStudyEntryGlobalMsg selfStudyEntryGlobalMsg = new MySelfStudyEntryGlobalMsg();
        selfStudyEntryGlobalMsg.setId(SelfStudyType.LIVECAST.name());
        selfStudyEntryGlobalMsg.setRefinedLessons(liveCastIndexRefinedLessons);
        MySelfStudyEntryGlobalMsg upsert = mySelfStudyEntryReminderDao.upsert(selfStudyEntryGlobalMsg);
        if (upsert != null)
            mySelfStudyGlobalMsgVersion.increase();
    }
}
