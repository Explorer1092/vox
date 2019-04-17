package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Mizar Service
 * Created by alex on 2016/9/18.
 */
@ServiceVersion(version = "1.1.DEV")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarSystemConfigService {
    MapMessage addSysPath(String functionName, String pathName, String desc, List<String> roleGroups);
    MapMessage updateSysPath(String sysPathId, String functionName, String pathName, String desc, List<String> roleGroups);
    MapMessage deleteSysPath(String sysPathId);
}
