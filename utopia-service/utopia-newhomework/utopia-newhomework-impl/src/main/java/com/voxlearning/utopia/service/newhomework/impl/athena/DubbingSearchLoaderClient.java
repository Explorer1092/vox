package com.voxlearning.utopia.service.newhomework.impl.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.athena.api.search.loader.DubbingSearchLoader;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.newhomework.impl.athena.DubbingSearchLoaderClient")
public class DubbingSearchLoaderClient {

    @Getter
    @ImportService(interfaceClass = DubbingSearchLoader.class)
    private DubbingSearchLoader dubbingSearchLoader;
}
