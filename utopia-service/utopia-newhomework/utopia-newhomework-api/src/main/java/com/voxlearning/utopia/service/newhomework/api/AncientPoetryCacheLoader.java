package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.newhomework.api.mapper.AncientPoetryResultCacheMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/20
 */
@ServiceVersion(version = "20190228")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface AncientPoetryCacheLoader {

    List<AncientPoetryResultCacheMapper> loadAncientPoetryResultCacheMapper(List<String> activityIds, List<Long> studentIds);

    AncientPoetryResultCacheMapper loadAncientPoetryResultCacheMapper(String activityId, Long studentId);
}
