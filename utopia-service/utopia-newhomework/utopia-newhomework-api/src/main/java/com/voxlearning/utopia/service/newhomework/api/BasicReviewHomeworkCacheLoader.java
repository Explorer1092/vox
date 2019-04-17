package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkCacheMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180612")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@CyclopsMonitor("utopia")
public interface BasicReviewHomeworkCacheLoader extends IPingable {
    BasicReviewHomeworkCacheMapper loadBasicReviewHomeworkCacheMapper(String packageId, Long studentId);
    
    void removeBasicReviewHomeworkCacheMapper(String packageId, Long studentId);

    Map<Long, BasicReviewHomeworkCacheMapper> loadBasicReviewHomeworkCacheMapper(String packageId, Collection<Long> studentIds);

    BasicReviewHomeworkCacheMapper addOrModifyBasicReviewHomeworkCacheMapper(NewHomeworkResult newHomeworkResult, String packageId, Long studentId);

    List<BasicReviewHomeworkCacheMapper> loadBasicReviewHomeworkCacheMappers(Long clazzGroupId);
}
