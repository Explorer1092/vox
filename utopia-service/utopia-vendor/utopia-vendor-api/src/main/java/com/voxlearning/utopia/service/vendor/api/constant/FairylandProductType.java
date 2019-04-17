package com.voxlearning.utopia.service.vendor.api.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author peng
 * @since 16-6-27
 * 自学乐园产品分类型
 */
public enum FairylandProductType {
    APPS("应用"), BOOKS("图书影像"), ELEC_PRODUCT("电子产品"), INNER_APPS("内部应用");

    public String typeName;

    FairylandProductType(String typeName) {
        this.typeName = typeName;
    }

    static final public Map<String, String> map = new HashMap();

    static {
        for (FairylandProductType productType : FairylandProductType.values()) {
            map.put(productType.name(), productType.typeName);
        }
    }

    public static FairylandProductType of(String value) {
        try {
            return FairylandProductType.valueOf(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static FairylandProductType parse(String value) {
        try {
            return FairylandProductType.valueOf(value);
        } catch (Exception ignored) {
            return APPS;
        }

    }
}