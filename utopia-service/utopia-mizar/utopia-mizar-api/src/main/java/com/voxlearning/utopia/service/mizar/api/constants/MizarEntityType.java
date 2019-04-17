package com.voxlearning.utopia.service.mizar.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * mizar 实体类型
 * @author xiang.lv
 * @date 2016/10／10   14:24
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MizarEntityType {
    SHOP("shop","机构"),
    BRAND("brand","品牌"),
    GOODS("goods","课程"),
    FAMILY_ACTIVITY("family_activity", "亲子活动"),
    ;
    @Getter
    private final String code;
    @Getter
    private final String name;

    private final static Map<String, MizarEntityType> MIZAR_ENTITY_TYPE = new LinkedHashMap<>();

    static {
        for (MizarEntityType type : MizarEntityType.values()) {
            MIZAR_ENTITY_TYPE.put(type.getCode(), type);
        }
    }

    public static Map<String, MizarEntityType> getAllMizarEntityTypeType() {
        return MIZAR_ENTITY_TYPE;
    }



    public static MizarEntityType of(final String code) {
        if (code == null) {
            return null;
        }
        return MIZAR_ENTITY_TYPE.get(String.valueOf(code));
    }

}
