package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.buffer.VersionedBufferData;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishPageContentConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author guangqing
 * @since 2018/9/10
 */
@ServiceVersion(version = "20181116")
@ServiceTimeout(timeout =30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface AiChipsEnglishConfigService extends IPingable {

    MapMessage addChipsEnglishPageContentConfig(String name, String value, String memo);

    MapMessage updateChipsEnglishPageContentConfig(String id, String name, String value, String memo);

    boolean deleteChipsEnglishPageContentConfig(String id);

    VersionedBufferData<List<ChipsEnglishPageContentConfig>> loadChipsEnglishConfigBufferData(Long version);

    ChipsEnglishPageContentConfig loadChipsEnglishConfigById(String id);

    List<ChipsEnglishPageContentConfig> loadAllChipsConfig4Crm();
}
