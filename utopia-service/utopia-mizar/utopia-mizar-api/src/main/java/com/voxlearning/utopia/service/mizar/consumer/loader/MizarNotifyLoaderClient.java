package com.voxlearning.utopia.service.mizar.consumer.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.mizar.api.loader.MizarNotifyLoader;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarNotifyMapper;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Created by Yuechen.Wang on 2016/12/05.
 */
public class MizarNotifyLoaderClient {

    @Getter
    @ImportService(interfaceClass = MizarNotifyLoader.class)
    private MizarNotifyLoader remoteReference;


    public List<MizarNotifyMapper> loadUserAllNotify(String userId) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptyList();
        }
        return remoteReference.loadAllUserNotify(userId);
    }

}
