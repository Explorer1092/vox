package com.voxlearning.utopia.service.mizar.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品数据来源枚举
 * @author xiang.lv
 * @date 2016/9/22   14:24
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum GrouponGoodsDataSourceType {
    ZHE("zhe_800","折800采集"),
    MANUAL_EDIT("manual_edit","人工编辑");
    @Getter
    private final String code;
    @Getter
    private final String name;

    private final static Map<String, GrouponGoodsDataSourceType> GROUPGOODS_SOURCE_TYPE = new LinkedHashMap<>();
    private final static List<String> GROUPGOODS_SOURCE_TYPE_CODE = new ArrayList<String>();

    static {
        for (GrouponGoodsDataSourceType statusType : GrouponGoodsDataSourceType.values()) {
            GROUPGOODS_SOURCE_TYPE_CODE.add(statusType.getCode());
            GROUPGOODS_SOURCE_TYPE.put(String.valueOf(statusType.getCode()), statusType);
        }
    }

    public static Map<String, GrouponGoodsDataSourceType> getAllDataSourceTypes() {
        return GROUPGOODS_SOURCE_TYPE;
    }

    public static List<String> getAllGroupGoodsSourceTypeCode() {
        return GROUPGOODS_SOURCE_TYPE_CODE;
    }

    public static GrouponGoodsDataSourceType of(final String code) {
        if (code == null) {
            return null;
        }
        return GROUPGOODS_SOURCE_TYPE.get(String.valueOf(code));
    }

}
