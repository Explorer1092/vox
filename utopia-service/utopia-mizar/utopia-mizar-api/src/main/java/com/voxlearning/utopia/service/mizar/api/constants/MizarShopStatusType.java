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
 * @date 2016-09-13
 * @author xiang.lv
 * 机构状态枚举类
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MizarShopStatusType {
    OFFLINE(0, "OFFLINE","审核不通过"),
    PENDING(1, "PENDING","审核中"),
    ONLINE(2, "ONLINE","审核通过");
    @Getter
    private final Integer id;
    @Getter
    private final String name;
    @Getter
    private final String desc;

    private final static Map<String, MizarShopStatusType> SHOP_STATUS_TYPE_MAP = new LinkedHashMap<>();
    private final static List<Integer> STATUS_ID_LIST = new ArrayList<Integer>();

    static {
        for (MizarShopStatusType statusType : MizarShopStatusType.values()) {
            STATUS_ID_LIST.add(statusType.getId());
            SHOP_STATUS_TYPE_MAP.put(String.valueOf(statusType.getId()), statusType);
        }
    }

    public static Map<String, MizarShopStatusType> getAllMizarShopStatus() {
        return SHOP_STATUS_TYPE_MAP;
    }

    public static List<Integer> getAllMizarShopStatusTypeIds() {
        return STATUS_ID_LIST;
    }

    public static MizarShopStatusType of(Integer type) {
        if (type == null) {
            return null;
        }
        return SHOP_STATUS_TYPE_MAP.get(String.valueOf(type));
    }

    public static MizarShopStatusType parse(String type) {
        for (MizarShopStatusType statusType : MizarShopStatusType.values()) {
            if (statusType.name().equals(type)) {
                return statusType;
            }
        }
        return null;
    }

}
