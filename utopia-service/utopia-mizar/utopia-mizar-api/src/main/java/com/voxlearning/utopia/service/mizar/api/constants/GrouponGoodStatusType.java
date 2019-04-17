package com.voxlearning.utopia.service.mizar.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiang.lv on 2016/9/23.
 *
 * @author xiang.lv
 * @date 2016/9/23   17:44
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum GrouponGoodStatusType {
    OFFLINE(0, "OFFLINE","下线"),
    ONLINE(1, "ONLINE","上线"),
    PENDING(2, "PENDING","审核中");

    @Getter
    private final Integer id;
    @Getter
    private final String code;
    @Getter
    private final String desc;

    private final static Map<String, GrouponGoodStatusType> TYPE_MAP = new LinkedHashMap<>();
    private final static List<Integer> ID_LIST = new ArrayList<Integer>();

    static {
        for (GrouponGoodStatusType type : GrouponGoodStatusType.values()) {
            ID_LIST.add(type.getId());
            TYPE_MAP.put(String.valueOf(type.getId()), type);
        }
    }

    public static Map<String, GrouponGoodStatusType> getAllGrouponGoodStatus() {
        return TYPE_MAP;
    }

    public static List<Integer> getAllGrouponGoodStatusIds() {
        return ID_LIST;
    }

    public static GrouponGoodStatusType of(Integer type) {
        if (type == null) {
            return null;
        }
        return TYPE_MAP.get(String.valueOf(type));
    }

}
