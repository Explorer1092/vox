package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;

import java.util.concurrent.TimeUnit;

/**
 * @author Ruib
 * @since 2017/4/12
 */
@ServiceVersion(version = "2017.04.12")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DPZoneService {

    MapMessage createClazzJournal(Long studentId, ClazzJournalType type, ClazzJournalCategory category, String json);

    /**
     * @see #createClazzJournal(Long, String, String, String, String, String)
     */
    @Deprecated
    MapMessage createClazzJournalDaily(Long studentId, String journalType, String category, String json, String unique);

    MapMessage createClazzJournal(Long studentId, String journalType, String journalCategory, String json, String journalDuplicationPolicy, String unique);
}
