package com.voxlearning.utopia.service.mizar.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 专题位置
 *
 * @author xiang.lv
 * @date 2016/9/22   14:24
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SpecialTopicPosition {
    BANNER(1, "头图"),
    LEFT(2, "左侧图"),
    RIGHT_TOP(3, "右上方图"),
    RIGHT_BOTTOM(4, "右下方图");
    @Getter
    private final Integer code;
    @Getter
    private final String desc;

    private final static Map<Integer, SpecialTopicPosition> TOPIC_POSITION_CODE = new LinkedHashMap<>();

    static {
        for (SpecialTopicPosition position : SpecialTopicPosition.values()) {
            TOPIC_POSITION_CODE.put(position.getCode(), position);
        }
    }

    public static Map<Integer, SpecialTopicPosition> getTopicPositionCodeMap() {
        return TOPIC_POSITION_CODE;
    }

    public static SpecialTopicPosition of(final int code) {
        if (!TOPIC_POSITION_CODE.containsKey(code)) {
            return null;
        }
        return TOPIC_POSITION_CODE.get(code);
    }

}
