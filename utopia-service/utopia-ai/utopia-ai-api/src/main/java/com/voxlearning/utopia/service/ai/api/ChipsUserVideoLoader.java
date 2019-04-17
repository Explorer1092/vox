package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.entity.AIUserVideo;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190326")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsUserVideoLoader extends IPingable {

    AIUserVideo loadById(String id);

    List<AIUserVideo> loadByUnitId(String unitId, AIUserVideo.ExamineStatus examineStatus);

    List<AIUserVideo> loadByUserId(Long userId);

    /**
     * for crm
     */
    List<AIUserVideo> loadByDateRange(Date startDate, Date endDate);

    Set<Long> loadVideoBlackList();



    MapMessage loadVideoBlackListForCrm();
}
