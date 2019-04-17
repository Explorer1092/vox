package com.voxlearning.utopia.service.piclisten.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramReadService;
import lombok.Getter;

/**
 * @author RA
 */
public class MiniProgramReadServiceClient {

    @Getter
    @ImportService(interfaceClass = MiniProgramReadService.class)
    private MiniProgramReadService remoteReference;
}
