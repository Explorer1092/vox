package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.VideoSummaryResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by tanguohong on 2016/11/25.
 */
@ServiceVersion(version = "20170525")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface VideoHomeworkService extends IPingable{

    /**
     * 视频摘要信息
     * @return List
     */
    @Idempotent
    List<VideoSummaryResult> getVideoSummaryInfo(String homeworkId, Long studentId);

    /**
     * 假期作业视频摘要信息
     */
    @Idempotent
    List<VideoSummaryResult> getVacationVideoSummaryInfo(String homeworkId, Long studentId);
}
