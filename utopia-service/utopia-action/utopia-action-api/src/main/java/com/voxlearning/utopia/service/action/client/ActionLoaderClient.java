package com.voxlearning.utopia.service.action.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.action.api.ActionLoader;
import lombok.Getter;

public class ActionLoaderClient {

    @Getter
    @ImportService(interfaceClass = ActionLoader.class)
    private ActionLoader remoteReference;
}
