package com.voxlearning.utopia.service.crm.api.constants.agent;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 签到的业务类型
 *
 * @author song.wang
 * @date 2018/12/6
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SignInBusinessType {

    SCHOOL("进校"),
    RESEARCHER("拜访教研员"),
    RESOURCE_EXTENSION("资源拓维"),
    MEETING("组会"),
    ACCOMPANY("陪同"),
    LIVE_ENROLLMENT("直播招生");


    private final String desc;

    private final static Map<String, SignInBusinessType> NAME_MAP = new LinkedHashMap<>();
    static {
        for(SignInBusinessType signInBusinessType : SignInBusinessType.values()){
            NAME_MAP.put(signInBusinessType.name(), signInBusinessType);
        }
    }

    public static SignInBusinessType nameOf(String name) {
        if(StringUtils.isBlank(name)){
            return null;
        }
        return NAME_MAP.get(StringUtils.trim(name));
    }
}
