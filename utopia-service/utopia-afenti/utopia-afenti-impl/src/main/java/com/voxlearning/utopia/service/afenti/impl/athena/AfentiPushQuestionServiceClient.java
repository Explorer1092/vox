package com.voxlearning.utopia.service.afenti.impl.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.athena.api.psr.loader.PsrLoader;
import com.voxlearning.athena.api.recom.loader.UtopiaPsrLoader;
import lombok.Getter;

import javax.inject.Named;

/**
 * Created by Summer on 2017/8/17.
 */
@Named("com.voxlearning.utopia.service.afenti.impl.athena.AfentiPushQuestionServiceClient")
public class AfentiPushQuestionServiceClient {
    @Getter
    @ImportService(interfaceClass = PsrLoader.class)
    private PsrLoader PsrLoader;

    @Getter
    @ImportService(interfaceClass = UtopiaPsrLoader.class)
    private UtopiaPsrLoader utopiaPsrLoader;
}
