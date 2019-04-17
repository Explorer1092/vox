package com.voxlearning.utopia.service.newhomework.impl.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.athena.api.search.loader.RecommendOralCommuncationLoader;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.newhomework.impl.athena.RecommendOralCommunicationLoaderClient")
public class RecommendOralCommunicationLoaderClient {

    @Getter
    @ImportService(interfaceClass = RecommendOralCommuncationLoader.class)
    private RecommendOralCommuncationLoader recommendOralCommuncationLoader;
}
