package com.voxlearning.utopia.service.newhomework.impl.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.athena.api.questions.loader.PictureBookLoader;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2017/8/31
 */
@Named("com.voxlearning.utopia.service.newhomework.impl.athena.AthenaPictureBookLoaderClient")
public class AthenaPictureBookLoaderClient {

    @Getter
    @ImportService(interfaceClass = PictureBookLoader.class)
    private PictureBookLoader pictureBookLoader;
}
