package com.voxlearning.utopia.service.piclisten.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramGroupService;
import lombok.Getter;

/**
 * @author RA
 */
public class MiniProgramGroupServiceClient {

    @Getter
    @ImportService(interfaceClass = MiniProgramGroupService.class)
    private MiniProgramGroupService remoteReference;
}
