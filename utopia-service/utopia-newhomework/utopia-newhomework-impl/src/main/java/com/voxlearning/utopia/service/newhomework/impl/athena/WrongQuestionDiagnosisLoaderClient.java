package com.voxlearning.utopia.service.newhomework.impl.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.athena.api.cuotizhenduan.loader.CuotizhenduanLoader;
import lombok.Getter;

import javax.inject.Named;

/**
 * @Description: 错题诊断
 * @author: Mr_VanGogh
 * @date: 2018/6/15 下午5:14
 */
@Named("com.voxlearning.utopia.service.newhomework.impl.athena.WrongQuestionDiagnosisLoaderClient")
public class WrongQuestionDiagnosisLoaderClient {

    @Getter
    @ImportService(interfaceClass = CuotizhenduanLoader.class)
    private CuotizhenduanLoader cuotizhenduanLoader;

}
