package com.voxlearning.utopia.service.psr.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.psr.newhomework.loader.SimilarQuestionLoader;
import lombok.Getter;

public class SimilarQuestionLoaderClient {

    @ImportService(interfaceClass = SimilarQuestionLoader.class)
    @Getter
    private SimilarQuestionLoader similarQuestionLoader;
}
