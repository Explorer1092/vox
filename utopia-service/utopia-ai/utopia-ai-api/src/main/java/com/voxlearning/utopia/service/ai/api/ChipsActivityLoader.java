package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.data.ChipsMiniProgramQRBO;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190311")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsActivityLoader extends IPingable {
    MapMessage loadLeadPageUser();

    MapMessage loadChipsMiniProgramPageable(int page);
}
