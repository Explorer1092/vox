package com.voxlearning.utopia.service.vendor.impl.service.thirdpartapi;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * 这里维护所有17需要调用第三方的API的app_key以及秘钥信息
 *
 * Created by zhouwei on 2018/8/2
 **/
public enum ThirdPart {

    DAITE("Daite" ,"戴特" , "http://api.iclass30.com","http://apitest.iclass30.com"),

    CNEDU("Cnedu" ,"中央电教馆" , "http://api.iclass30.com","http://apitest.iclass30.com");

    @Getter
    @Setter
    private String appKey;

    @Getter
    @Setter
    private String platform;

    @Setter
    @Getter
    private String onlineDomain;

    @Setter
    @Getter
    private String testDomain;

    private ThirdPart(String appKey, String platform, String onlineDomain, String testDomain) {
        this.appKey = appKey;
        this.platform = platform;
        this.onlineDomain = onlineDomain;
        this.testDomain = testDomain;
    }

}
