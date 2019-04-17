package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.vendor.api.entity.TermReport;

import java.util.concurrent.TimeUnit;

/**
 * @author malong
 * @since 2017/6/19
 */
@ServiceVersion(version = "20170619")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface TermReportLoader extends IPingable {

    @Idempotent
    @CacheMethod(type = TermReport.class)
    TermReport getTermReport(@CacheParameter(value = "PID") Long parentId, @CacheParameter(value = "SID") Long studentId);
}
