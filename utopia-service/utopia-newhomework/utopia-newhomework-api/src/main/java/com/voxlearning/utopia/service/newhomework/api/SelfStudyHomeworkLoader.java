package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.SelfStudyWordIncreaseMapper;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/2/6
 */
@ServiceVersion(version = "20180925")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface SelfStudyHomeworkLoader extends IPingable {

    @Idempotent
    Map<String, SelfStudyHomework> loadSelfStudyHomeworkIncludeDisabled(Collection<String> ids);

    @Idempotent
    default SelfStudyHomework loadSelfStudyHomeworkIncludeDisabled(String id) {
        if (StringUtils.isBlank(id) || !NewHomeworkUtils.isSelfStudyHomeworkId(id)) {
            return null;
        }
        Map<String, SelfStudyHomework> map = loadSelfStudyHomeworkIncludeDisabled(Collections.singleton(id));
        if (MapUtils.isNotEmpty(map)) {
            return map.get(id);
        } else {
            return null;
        }
    }

    @Idempotent
    default Map<String, SelfStudyHomework> loadSelfStudyHomework(Collection<String> ids) {
        return loadSelfStudyHomeworkIncludeDisabled(ids)
                .values()
                .stream()
                .filter(o -> !o.isDisabledTrue())
                .collect(Collectors.toMap(SelfStudyHomework::getId, Function.identity()));
    }

    @Idempotent
    default SelfStudyHomework loadSelfStudyHomework(String id) {
        SelfStudyHomework homework = loadSelfStudyHomeworkIncludeDisabled(id);
        if (homework != null && !homework.isDisabledTrue()) {
            return homework;
        } else {
            return null;
        }
    }

    @Idempotent
    Map<String, SelfStudyHomeworkResult> loadSelfStudyHomeworkResult(Collection<String> ids);

    @Idempotent
    default SelfStudyHomeworkResult loadSelfStudyHomeworkResult(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        Map<String, SelfStudyHomeworkResult> map = loadSelfStudyHomeworkResult(Collections.singleton(id));
        if (MapUtils.isNotEmpty(map)) {
            return map.get(id);
        } else {
            return null;
        }
    }

    @Idempotent
    List<SelfStudyWordIncreaseMapper> findSelfStudyWordIncreaseMapper();

    @Idempotent
    Set<String> loadSelfStudyHomeworkIds(String newHomeworkId, List<Long> userIds);

    @Idempotent
    Map<String, SelfStudyHomework> loadSelfStudyHomeworkIds(List<String> newHomeworkIds, Long userId);

    @Idempotent
    Map<String, SelfStudyHomeworkReport> loadSelfStudyHomeworkReport(Collection<String> ids);

    @Idempotent
    Map<String, SelfStudyAccomplishment> loadSelfStudyAccomplishment(Collection<String> ids);
}
