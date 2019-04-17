package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190110")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface DPHomeworkService extends IPingable {
    NewHomework loadNewHomework(String homeworkId);
}
