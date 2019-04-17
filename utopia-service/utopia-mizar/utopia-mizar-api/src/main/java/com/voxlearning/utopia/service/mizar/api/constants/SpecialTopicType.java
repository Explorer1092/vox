package com.voxlearning.utopia.service.mizar.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SpecialTopicType implements Serializable {
    TO_DETAIL("商品专题"),
    TO_URL("外链专题");
    @Getter
    private final String desc;

    private final static Map<String, SpecialTopicType> SPECIAL_TOPIC_TYPE = new LinkedHashMap<>();

    static {
        for (SpecialTopicType type : SpecialTopicType.values()) {
            SPECIAL_TOPIC_TYPE.put(type.name(), type);
        }
    }

    public static Map<String, SpecialTopicType> getAllSpecialTopicType() {
        return SPECIAL_TOPIC_TYPE;
    }

    public static SpecialTopicType of(final String name) {
        if (name == null) {
            return null;
        }
        return SPECIAL_TOPIC_TYPE.get(String.valueOf(name));
    }
}
