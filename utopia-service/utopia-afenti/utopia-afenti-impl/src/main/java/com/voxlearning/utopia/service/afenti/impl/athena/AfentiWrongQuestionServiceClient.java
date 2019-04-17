package com.voxlearning.utopia.service.afenti.impl.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.athena.api.afenti.AfentiWrongQuestionService;
import lombok.Getter;

import javax.inject.Named;
import java.util.concurrent.TimeUnit;

/**
 * Created by Summer on 2017/7/3.
 */

@Named("com.voxlearning.utopia.service.afenti.impl.athena.AfentiWrongQuestionServiceClient")
public class AfentiWrongQuestionServiceClient {

    @Getter
    @ImportService(interfaceClass = AfentiWrongQuestionService.class)
    @ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
    private AfentiWrongQuestionService afentiWrongQuestionService;
}
