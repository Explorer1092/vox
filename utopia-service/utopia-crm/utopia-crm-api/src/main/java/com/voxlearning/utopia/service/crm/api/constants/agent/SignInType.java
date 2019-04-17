package com.voxlearning.utopia.service.crm.api.constants.agent;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 签到类型
 *
 * @author song.wang
 * @date 2018/12/6
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SignInType {

    GPS("位置签到"),
    PHOTO("照片签到");

    private final String desc;

    private final static Map<String, SignInType> NAME_MAP = new LinkedHashMap<>();
    static {
        for(SignInType signInType : SignInType.values()){
            NAME_MAP.put(signInType.name(), signInType);
        }
    }

    public static SignInType nameOf(String name) {
        if(StringUtils.isBlank(name)){
            return null;
        }
        return NAME_MAP.get(StringUtils.trim(name));
    }
}
