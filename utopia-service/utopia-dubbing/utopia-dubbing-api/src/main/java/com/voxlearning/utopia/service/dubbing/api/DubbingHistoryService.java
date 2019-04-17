package com.voxlearning.utopia.service.dubbing.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingHistory;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author shiwei.liao
 * @since 2017-8-23
 */
@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DubbingHistoryService extends IPingable {


    void saveDubbingHistory(DubbingHistory history);


    void disabledDubbingHistory(Collection<String> id);
}
