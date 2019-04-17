package com.voxlearning.utopia.service.mizar.api.loader;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.sys.MizarSysPath;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Mizar System Config Loader
 * Created by alex on 2016/9/18.
 */
@ServiceVersion(version = "1.1.DEV")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarSystemConfigLoader extends IPingable {

    List<MizarSysPath> loadAllSysPath();

    MizarSysPath loadSysPath(String sysPathId);

    List<String> loadRolePath(Collection<String> roleGroups);
}
