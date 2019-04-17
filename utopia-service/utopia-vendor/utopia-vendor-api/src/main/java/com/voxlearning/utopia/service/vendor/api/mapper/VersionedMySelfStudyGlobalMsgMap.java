package com.voxlearning.utopia.service.vendor.api.mapper;

import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.vendor.api.entity.MySelfStudyEntryGlobalMsg;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2017-06-14 下午4:15
 **/
@Data
@NoArgsConstructor
public class VersionedMySelfStudyGlobalMsgMap implements Serializable {
    private static final long serialVersionUID = -1204850428435709792L;

    private long version;

    private Map<SelfStudyType, MySelfStudyEntryGlobalMsg> mySelfStudyEntryReminderMap;

    public VersionedMySelfStudyGlobalMsgMap(long version, Map<SelfStudyType, MySelfStudyEntryGlobalMsg> mySelfStudyEngryLabelMap) {
        this.version = version;
        this.mySelfStudyEntryReminderMap = mySelfStudyEngryLabelMap;
    }
}
