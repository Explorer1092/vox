package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.vendor.api.entity.MySelfStudyEntryGlobalMsg;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedMySelfStudyGlobalMsgMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangpeng
 * @since 2017-06-14 下午4:49
 **/
@ServiceVersion(version = "2017-10-19")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MySelfStudyGlobalMsgService extends IPingable {

    Map<SelfStudyType, MySelfStudyEntryGlobalMsg> getMySelfStudyEntryGlobalMsgMap();

    default MySelfStudyEntryGlobalMsg getEntryReminder(SelfStudyType selfStudyType){
        Map<SelfStudyType, MySelfStudyEntryGlobalMsg> mySelfStudyEntryReminderMap = getMySelfStudyEntryGlobalMsgMap();
        if (MapUtils.isEmpty(mySelfStudyEntryReminderMap))
            return null;
        return mySelfStudyEntryReminderMap.get(selfStudyType);
    }

    VersionedMySelfStudyGlobalMsgMap loadVersionedMySelfStudyEntryLabelMap(long version);
}
