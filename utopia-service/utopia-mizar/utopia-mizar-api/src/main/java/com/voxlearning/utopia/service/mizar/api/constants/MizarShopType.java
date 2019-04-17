package com.voxlearning.utopia.service.mizar.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @date 2016-09-14
 * @author xiang.lv
 * 机构类型(线上1,线下0)
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MizarShopType {
    OFFLINE(0, "OFFLINE","线下机构"),
    ONLINE(1, "ONLINE","线上机构");
    @Getter
    private final Integer id;
    @Getter
    private final String name;
    @Getter
    private final String desc;

    private final static Map<String, MizarShopType> SHOP_TYPE_MAP = new LinkedHashMap<>();
    private final static List<Integer> STATUS_ID_LIST = new ArrayList<Integer>();

    static {
        for (MizarShopType shopType : MizarShopType.values()) {
            STATUS_ID_LIST.add(shopType.getId());
            SHOP_TYPE_MAP.put(String.valueOf(shopType.getId()), shopType);
        }
    }

    public static Map<String, MizarShopType> getAllMizarShopTypes() {
        return SHOP_TYPE_MAP;
    }

    public static List<Integer> getAllMizarShopStatusTypeIds() {
        return STATUS_ID_LIST;
    }

    public static MizarShopType of(Integer type) {
        if (type == null) {
            return null;
        }
        return SHOP_TYPE_MAP.get(String.valueOf(type));
    }

}
