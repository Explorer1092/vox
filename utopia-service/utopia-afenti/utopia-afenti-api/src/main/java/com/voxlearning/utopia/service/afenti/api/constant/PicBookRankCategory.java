package com.voxlearning.utopia.service.afenti.api.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Summer on 2018/4/8
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PicBookRankCategory {
    READ(1, "读书榜"),
    WORD(2, "单词榜");

    public Integer type;
    public String desc;

    private static Map<Integer, PicBookRankCategory> statusMap;

    static {
        statusMap = new LinkedHashMap<>();
        for (PicBookRankCategory type : values()) {
            statusMap.put(type.type, type);
        }
    }

    public static PicBookRankCategory of(Integer type) {
        return statusMap.get(type);
    }

}
