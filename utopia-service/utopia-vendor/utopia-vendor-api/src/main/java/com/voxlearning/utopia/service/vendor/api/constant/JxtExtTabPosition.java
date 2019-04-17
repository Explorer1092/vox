package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2016-8-2
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum JxtExtTabPosition {
    INDEX(1,"家长通首页"),
    PERSONAL_CENTER_STUDENT(2,"家长通个人中心-学生相关"),
    PERSONAL_CENTER_MARKET(3,"家长通个人中心-运营相关"),
    UNKNOWN(100,"未知");

    private final int position;
    private final String desc;

    public static final Map<Integer,JxtExtTabPosition> maps;

    static {
        maps = new HashMap<>();
        for(JxtExtTabPosition pos: JxtExtTabPosition.values()){
            if(!maps.containsKey(pos.getPosition())){
                maps.put(pos.getPosition(),pos);
            }
        }
    }

    public static JxtExtTabPosition parseWithUnknown(Integer position){
        if(maps.containsKey(position)){
            return maps.get(position);
        }
        return JxtExtTabPosition.UNKNOWN;
    }
}
