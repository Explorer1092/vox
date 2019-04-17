package com.voxlearning.utopia.service.newhomework.impl.athena;


import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.athena.api.search.loader.PictureBookSearchLoader;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.newhomework.impl.athena.PictureBookSearchLoaderClient")
public class PictureBookSearchLoaderClient {

    @Getter
    @ImportService(interfaceClass = PictureBookSearchLoader.class)
    private PictureBookSearchLoader pictureBookSearchLoader;
}
