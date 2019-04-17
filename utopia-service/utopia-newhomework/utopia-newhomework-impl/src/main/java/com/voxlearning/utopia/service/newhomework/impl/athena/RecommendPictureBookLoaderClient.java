package com.voxlearning.utopia.service.newhomework.impl.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.athena.api.search.loader.RecommendPictureBookLoader;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.newhomework.impl.athena.RecommendPictureBookLoaderClient")
public class RecommendPictureBookLoaderClient {

    @Getter
    @ImportService(interfaceClass = RecommendPictureBookLoader.class)
    private RecommendPictureBookLoader recommendPictureBookLoader;
}
