package com.voxlearning.washington.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.athena.api.recom.loader.UtopiaPsrLoader;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2018/2/1
 */
@Named("com.voxlearning.washington.athena.PsrServiceClient")
public class PsrServiceClient {

    @Getter
    @ImportService(interfaceClass = UtopiaPsrLoader.class)
    private UtopiaPsrLoader utopiaPsrLoader;
}
