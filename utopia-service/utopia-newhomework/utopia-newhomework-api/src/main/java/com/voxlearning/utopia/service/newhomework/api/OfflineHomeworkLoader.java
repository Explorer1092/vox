package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomework;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author guoqiang.li
 * @since 2016/9/7
 */
@ServiceVersion(version = "20161212")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface OfflineHomeworkLoader extends IPingable {
    @Idempotent
    @CacheMethod(
            type = OfflineHomework.class,
            writeCache = false
    )
    OfflineHomework loadOfflineHomework(String id);

    @Idempotent
    @CacheMethod(
            type = OfflineHomework.class,
            writeCache = false
    )
    Map<String, OfflineHomework> loadOfflineHomeworks(@CacheParameter(multiple = true) Collection<String> ids);

    @Idempotent
    @CacheMethod(
            type = OfflineHomework.class,
            writeCache = false
    )
    Map<String, OfflineHomework> loadByNewHomeworkIds(@CacheParameter(value = "NHID", multiple = true) Collection<String> newHomeworkIds);

    @Idempotent
    @CacheMethod(
            type = OfflineHomework.class,
            writeCache = false
    )
    Map<Long, List<OfflineHomework>> loadGroupOfflineHomeworks(Collection<Long> groupIds);

    @Idempotent
    @Deprecated
    Page<OfflineHomework> loadGroupOfflineHomeworks(Collection<Long> groupIds, Date startDate, Date endDate, Pageable pageable);

    @Idempotent
    MapMessage loadOfflineHomeworkDetail(String offlineHomeworkId);
}
