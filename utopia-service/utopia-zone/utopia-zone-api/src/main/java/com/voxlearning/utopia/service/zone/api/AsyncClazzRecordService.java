package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.ClazzRecordCardMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Async clazz record service
 * Created by alex on 2017/3/1.
 */
@ServiceVersion(version = "20170428")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AsyncClazzRecordService extends IPingable {

    // 学霸之星 卡片
    @Async
    AlpsFuture<ClazzRecordCardMapper> loadStudyMasterCard(Long userId, NewHomework.Location homework);

    // 专注之星 卡片
    @Async
    AlpsFuture<ClazzRecordCardMapper> loadFocusCard(Long userId, NewHomework.Location location);

    // 装扮之星 卡片
    @Async
    AlpsFuture<ClazzRecordCardMapper> loadFashionCard(Long clazzId, List<Long> classmates);

    // 装扮之星 Top3
    @Async
    AlpsFuture<List<ClazzRecordCardMapper>> loadTop3FashionList(Long clazzId, List<Long> classmates);

    // 装扮之星 WeekTop
    @Async
    AlpsFuture<ClazzRecordCardMapper> loadWeekTopFashionCard(Long clazzId, List<Long> classmates);

    // 满分之星 卡片
    @Async
    AlpsFuture<ClazzRecordCardMapper> loadFullMarksCard(Collection<Long> groupIds, Long clazzId, List<Long> classmates);

    // 友爱之星 卡片
    @Async
    AlpsFuture<ClazzRecordCardMapper> loadFriendshipCard(Long clazzId, List<Long> classmates);

}
