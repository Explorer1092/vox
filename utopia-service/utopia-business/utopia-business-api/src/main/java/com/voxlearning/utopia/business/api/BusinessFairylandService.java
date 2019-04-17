package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Ruib
 * @since 2019/1/2
 */
@ServiceVersion(version = "20190102")
@ServiceTimeout(timeout = 20, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface BusinessFairylandService extends IPingable {

    List<Map<String, Object>> fetchStudentMobileAvailableApps(Long studentId, String version, String ast);
}
