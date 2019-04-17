package com.voxlearning.utopia.service.mizar.api.loader;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarNotifyMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yuechen.Wang on 2016/12/05.
 */
@ServiceVersion(version = "20161205")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarNotifyLoader extends IPingable {

    List<MizarNotifyMapper> loadAllUserNotify(String userId);

}
