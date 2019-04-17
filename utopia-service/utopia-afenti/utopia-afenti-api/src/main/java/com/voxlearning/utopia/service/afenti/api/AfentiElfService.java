package com.voxlearning.utopia.service.afenti.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;

import java.util.concurrent.TimeUnit;

/**
 * @author Ruib
 * @since 2016/6/28
 */
@ServiceVersion(version = "20171102")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 0)
public interface AfentiElfService extends IPingable {
    @Deprecated
    MapMessage fetchElf(Long studentId, Subject subject);

    MapMessage fetchElf(Long studentId, Subject subject, AfentiWrongQuestionStateType stateType);

    MapMessage fetchPageElf(Long studentId, Subject subject, AfentiWrongQuestionStateType stateType, AfentiWrongQuestionSource source, Integer page,Integer pageSize);

    @Deprecated
    MapMessage fetchElfIndex(Long studentId, Subject subject);

    MapMessage fetchElfIndexV2(Long studentId, Subject subject);

    MapMessage processElfResult(ElfResultContext ctx);
}
