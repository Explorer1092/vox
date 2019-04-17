package com.voxlearning.utopia.service.vendor.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.vendor.api.ThirdPartLoginService;
import lombok.Getter;

/**
 * Created by zhouwei on 2018/8/2
 **/
public class ThirdPartLoginServiceClient {

    @Getter
    @ImportService(interfaceClass = ThirdPartLoginService.class)
    private ThirdPartLoginService thirdPartLoginService;

}
