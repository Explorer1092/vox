package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkProcessMapper;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Big Data dubbo proxy service.
 *
 * @author xuesong.zhang
 * @since 2016/8/29
 */
@ServiceVersion(version = "20160829")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface BDHomeworkService {

    /**
     * 给永磊用的接口
     *
     * @param processId   做题详情
     * @param schoolLevel 学段
     * @return HomeworkProcessMapper
     */
    @Deprecated
    List<HomeworkProcessMapper> getProcessResultMapper(Collection<String> processId, SchoolLevel schoolLevel);

    List<HomeworkProcessMapper> getProcessResultMapperWithGoal(Collection<String> processId, SchoolLevel schoolLevel, String goal);
}
