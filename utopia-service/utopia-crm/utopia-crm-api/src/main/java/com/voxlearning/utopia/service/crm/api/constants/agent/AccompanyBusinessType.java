package com.voxlearning.utopia.service.crm.api.constants.agent;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 陪同类型
 *
 * @author song.wang
 * @date 2018/12/14
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AccompanyBusinessType {

    SCHOOL("进校"),
    MEETING("组会"),
    RESOURCE_EXTENSION("资源拓维");

    private final String desc;

    private final static Map<String, AccompanyBusinessType> NAME_MAP = new LinkedHashMap<>();
    static {
        for(AccompanyBusinessType accompanyBusinessType : AccompanyBusinessType.values()){
            NAME_MAP.put(accompanyBusinessType.name(), accompanyBusinessType);
        }
    }

    public static AccompanyBusinessType nameOf(String name) {
        if(StringUtils.isBlank(name)){
            return null;
        }
        return NAME_MAP.get(StringUtils.trim(name));
    }
}
