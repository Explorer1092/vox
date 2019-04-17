package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarNotify;
import com.voxlearning.utopia.service.mizar.api.service.MizarNotifyService;
import lombok.Getter;

import java.util.Collection;

/**
 * Created by yuechen.wang on 16/12/05.
 */
public class MizarNotifyServiceClient {

    @Getter
    @ImportService(interfaceClass = MizarNotifyService.class)
    private MizarNotifyService remoteReference;

    public MapMessage sendNotify(MizarNotify notify, Collection<String> users) {
        return remoteReference.sendNotify(notify, users);
    }

    public MapMessage readNotify(String refId) {
        return remoteReference.readNotify(refId);
    }

    public MapMessage removeNotify(String refId) {
        return remoteReference.removeNotify(refId);
    }

}
