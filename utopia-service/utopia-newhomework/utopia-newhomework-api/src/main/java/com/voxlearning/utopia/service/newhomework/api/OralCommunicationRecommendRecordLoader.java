package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.OralCommunicationRecommendRecord;

import java.util.concurrent.TimeUnit;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/1/3
 * \* Time: 7:56 PM
 * \* Description:
 * \
 */
@ServiceVersion(version = "20190103")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface OralCommunicationRecommendRecordLoader extends IPingable {

    @Idempotent
    OralCommunicationRecommendRecord loadOralCommunicationRecommendRecord(Subject subject, Long teacherId);

    void updateOralCommunicationRecommendRecord(OralCommunicationRecommendRecord oralCommunicationRecommendRecord);
}
