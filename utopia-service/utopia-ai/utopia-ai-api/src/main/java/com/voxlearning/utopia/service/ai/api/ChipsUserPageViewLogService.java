package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.constant.PageViewType;
import com.voxlearning.utopia.service.ai.entity.ChipsUserPageViewLog;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author guangqing
 * @since 2019/3/19
 */
@ServiceVersion(version = "20190325")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsUserPageViewLogService {

    List<ChipsUserPageViewLog> loadChipsUserPageViewLogByType(Collection<Long> userCol, PageViewType type);

    MapMessage upsertChipsUserPageViewLog(ChipsUserPageViewLog viewLog);

    ChipsUserPageViewLog loadChipsUserPageViewLogById(String id);

    Map<String, ChipsUserPageViewLog> loadChipsUserPageViewLogByIds(Collection<String> ids);
}
