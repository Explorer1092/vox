package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.ReadingDubbingRecommend;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180409")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface ReadingReportService extends IPingable {

    //保存推荐
    MapMessage submitReadingDubbingData(Teacher teacher, String hid, ObjectiveConfigType type, String pictureId, List<ReadingDubbingRecommend.ReadingDubbing> readingDubbings, String recommendComment);
}
