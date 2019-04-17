package com.voxlearning.utopia.service.piclisten.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramCheckService;
import lombok.Getter;

/**
 * @author RA
 */
public class MiniProgramCheckServiceClient {

    @Getter
    @ImportService(interfaceClass = MiniProgramCheckService.class)
    private MiniProgramCheckService remoteReference;
}
