package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author guoqiang.li
 * @since 2017/11/8
 */
@ServiceVersion(version = "20171108")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface BasicReviewHomeworkLoader {

    @CacheMethod(type = BasicReviewHomeworkPackage.class, writeCache = false)
    BasicReviewHomeworkPackage load(String packageId);

    @CacheMethod(type = BasicReviewHomeworkPackage.class, writeCache = false)
    Map<Long, List<BasicReviewHomeworkPackage>> loadBasicReviewHomeworkPackageByClazzGroupIds(@CacheParameter(value = "CG", multiple = true) Collection<Long> groupId);

    MapMessage loadStudentDayPackages(String packageId, Long studentId);
}
