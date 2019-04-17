package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.DubbingRecommend;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/8/25
 * \* Time: 下午3:13
 * \* Description:
 * \
 */
@ServiceVersion(version = "20180825")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface DubbingScoreRecommendLoader extends IPingable {
    Map<String, DubbingRecommend> loadByHomeworkIds(Collection<String> homeworkIds);
}
