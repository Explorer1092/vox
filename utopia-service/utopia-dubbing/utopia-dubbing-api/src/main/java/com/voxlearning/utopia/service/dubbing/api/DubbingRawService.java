package com.voxlearning.utopia.service.dubbing.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingRaw;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiang wei on 2017/10/12.
 */
@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DubbingRawService extends IPingable {

    void upsertDubbingRaw(DubbingRaw dubbingRaw);

    List<DubbingRaw> exportDubbingRaw();

    DubbingRaw loadById(String id);
}
