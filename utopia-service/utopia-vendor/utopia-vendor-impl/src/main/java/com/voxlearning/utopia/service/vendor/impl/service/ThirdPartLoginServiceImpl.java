package com.voxlearning.utopia.service.vendor.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.vendor.api.ThirdPartLoginService;
import com.voxlearning.utopia.service.vendor.impl.service.thirdpartapi.ThirdPart;
import com.voxlearning.utopia.service.vendor.impl.service.thirdpartapi.daite.DaiteLoginApi;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by zhouwei on 2018/8/2
 **/
@Named
@ExposeService(interfaceClass = ThirdPartLoginService.class)
public class ThirdPartLoginServiceImpl implements ThirdPartLoginService {

    @Inject private DaiteLoginApi daiteLoginApi;

    @Override
    public MapMessage checkLogin(String source, String token) {
        if (StringUtils.isEmpty(source) || StringUtils.isEmpty(token)) {
            return MapMessage.errorMessage("source and token must is not empty");
        }
        List<String> appKeys = Arrays.asList(ThirdPart.values()).stream().map(ThirdPart::getAppKey).collect(Collectors.toList());
        if (!appKeys.contains(source)) {
            return MapMessage.errorMessage("the source is not valid");
        }
        if (Objects.equals(ThirdPart.DAITE.getAppKey(), source)) {
            return daiteLoginApi.checkLogin(token);
        }
        return MapMessage.errorMessage("the " + source + " is not impl");
    }

}
