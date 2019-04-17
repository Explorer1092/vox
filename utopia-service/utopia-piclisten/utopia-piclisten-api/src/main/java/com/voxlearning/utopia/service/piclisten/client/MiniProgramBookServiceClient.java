package com.voxlearning.utopia.service.piclisten.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramBookService;
import lombok.Getter;

/**
 * @author RA
 */
public class MiniProgramBookServiceClient {

    @Getter
    @ImportService(interfaceClass = MiniProgramBookService.class)
    private MiniProgramBookService remoteReference;
}
