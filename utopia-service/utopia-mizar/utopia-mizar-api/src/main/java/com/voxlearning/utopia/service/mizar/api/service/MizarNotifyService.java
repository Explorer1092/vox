package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarNotify;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yuechen.Wang on 16/12/05.
 */

@ServiceVersion(version = "20161205")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarNotifyService {

    MapMessage sendNotify(MizarNotify notify, Collection<String> users);

    MapMessage readNotify(String refId);

    MapMessage removeNotify(String refId);

}
