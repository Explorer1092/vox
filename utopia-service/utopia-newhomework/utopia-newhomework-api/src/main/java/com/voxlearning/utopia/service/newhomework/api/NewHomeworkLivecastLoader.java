package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.UsTalkHomeworkData;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xuesong.zhang
 * @since 2016/9/12
 */
@ServiceVersion(version = "20160912")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface NewHomeworkLivecastLoader extends IPingable {


//    @Deprecated
//    @Idempotent
//    Map<String, NewHomework> loadNewHomeworksIncludeDisabled(Collection<String> ids);
//
//    @Deprecated
//    @Idempotent
//    NewHomeworkResult loadNewHomeworkResult(NewHomework.Location location, Long userId);
//
//    @Deprecated
//    @Idempotent
//    Map<Long, NewHomeworkResult> loadNewHomeworkResult(NewHomework.Location location, Collection<Long> userIds, boolean needAnswer);
//
//    @Deprecated
//    @Idempotent
//    List<NewHomeworkResult> loadNewHomeworkResult(Collection<NewHomework.Location> locations, Long userId);
//
//    @Idempotent
//    @Deprecated
//    Map<String, NewHomeworkProcessResult> loadNewHomeworkProcessResult(Collection<String> ids);
//
//    @Deprecated
//    @Idempotent
//    Map<String, NewHomeworkProcessResult> loadNewHomeworkProcessResult(String homeworkId, Collection<String> ids);
//
//
//    @Deprecated
//    @Idempotent
//    List<Map<String, Object>> loadNewHomeworkQuestionResult(Long studentId, String homeworkId, String categoryId, String lessonId);

    // ############ 上面是旧的ustalk的，华丽的分割线，严格分开一下吧 ############ //
    @Idempotent
    MapMessage getHomeworkContent(Long teacherId, String unitId, String bookId);

    @Idempotent
    Map<Long, String> loadCategoryName(Collection<Long> practiceIds);
    // ############ 上面是新的ustalk的，华丽的分割线，严格分开一下吧 ############ //

    @Idempotent
    List<Map<String, Object>> loadUstalkHomeworkQuestionResult(Long studentId, String homeworkId, String categoryId, String lessonId);

    @Idempotent
    @CacheMethod(type = LiveCastHomework.class, writeCache = false)
    Map<String, LiveCastHomework> loadLiveCastHomeworkIncludeDisabled(@CacheParameter(multiple = true) Collection<String> ids);

    @Idempotent
    default LiveCastHomework loadLiveCastHomeworkIncludeDisabled(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        Map<String, LiveCastHomework> map = loadLiveCastHomeworkIncludeDisabled(Collections.singleton(id));
        if (MapUtils.isNotEmpty(map)) {
            return map.get(id);
        } else {
            return null;
        }
    }

    @Idempotent
    LiveCastHomeworkResult loadLiveCastHomeworkResult(LiveCastHomework.Location location, Long userId);

    @Idempotent
    Map<Long, LiveCastHomeworkResult> loadLiveCastHomeworkResult(LiveCastHomework.Location location, Collection<Long> userIds);

    @Idempotent
    List<LiveCastHomeworkResult> loadLiveCastHomeworkResult(Collection<LiveCastHomework.Location> locations, Long userId);

    @Idempotent
    @CacheMethod(type = LiveCastHomeworkProcessResult.class, writeCache = false)
    Map<String, LiveCastHomeworkProcessResult> loadLiveCastHomeworkProcessResult(@CacheParameter(multiple = true) Collection<String> ids);


    // 用于导数据的
    @Idempotent
    List<UsTalkHomeworkData> findAllUsTalkHomeworkData();
}
