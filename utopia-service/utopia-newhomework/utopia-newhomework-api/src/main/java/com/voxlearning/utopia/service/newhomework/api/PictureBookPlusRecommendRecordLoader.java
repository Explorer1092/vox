package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusRecommendRecord;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180518")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface PictureBookPlusRecommendRecordLoader extends IPingable {
    @Idempotent
    PictureBookPlusRecommendRecord loadPictureBookRecommendRecord(Subject subject, Long teacherId);

    void updatePictureBookRecommendRecord(PictureBookPlusRecommendRecord pictureBookRecommendRecord);
}
