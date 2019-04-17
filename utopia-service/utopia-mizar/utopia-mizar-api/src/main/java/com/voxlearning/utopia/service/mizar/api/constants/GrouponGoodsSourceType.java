package com.voxlearning.utopia.service.mizar.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品来源枚举
 * @author xiang.lv
 * @date 2016/9/22   14:24
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum GrouponGoodsSourceType {
    TIAN_MAO("tian_mao","天猫"),
    TAO_BAO("tao_bao","淘宝"),
    JD("jing_dong","京东"),
    DANG_DNAG("dang_dang","当当"),
    AMAZON_CHINA("amazon_china","亚马逊中国");
    @Getter
    private final String code;
    @Getter
    private final String name;

    private final static Map<String, GrouponGoodsSourceType> GROUPGOODS_SOURCE_TYPE = new LinkedHashMap<>();
    private final static List<String> GROUPGOODS_SOURCE_TYPE_CODE = new ArrayList<String>();

    static {
        for (GrouponGoodsSourceType statusType : GrouponGoodsSourceType.values()) {
            GROUPGOODS_SOURCE_TYPE_CODE.add(statusType.getCode());
            GROUPGOODS_SOURCE_TYPE.put(statusType.getCode(), statusType);
        }
    }

    public static Map<String, GrouponGoodsSourceType> getAllGrouponGoodsSourceType() {
        return GROUPGOODS_SOURCE_TYPE;
    }

    public static List<String> getAllGroupGoodsSourceTypeCode() {
        return GROUPGOODS_SOURCE_TYPE_CODE;
    }

    public static GrouponGoodsSourceType of(final String code) {
        if (code == null) {
            return null;
        }
        return GROUPGOODS_SOURCE_TYPE.get(String.valueOf(code));
    }

}
