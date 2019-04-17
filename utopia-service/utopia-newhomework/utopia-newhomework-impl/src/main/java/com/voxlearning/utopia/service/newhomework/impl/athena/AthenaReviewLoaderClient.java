package com.voxlearning.utopia.service.newhomework.impl.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.athena.api.recom.loader.AthenaReviewLoader;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2017/11/8
 */
@Named("com.voxlearning.utopia.service.newhomework.impl.athena.AthenaReviewLoaderClient")
public class AthenaReviewLoaderClient {
    @Getter
    @ImportService(interfaceClass = AthenaReviewLoader.class)
    private AthenaReviewLoader athenaReviewLoader;
}
