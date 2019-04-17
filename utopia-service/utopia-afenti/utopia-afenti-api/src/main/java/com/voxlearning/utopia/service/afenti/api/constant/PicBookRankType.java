package com.voxlearning.utopia.service.afenti.api.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Summer on 2018/4/9
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PicBookRankType {

    SCHOOL(1, "学校榜"),
    ALL(2, "全国榜");

    public Integer type;
    public String desc;

    private static Map<Integer, PicBookRankType> statusMap;

    static {
        statusMap = new LinkedHashMap<>();
        for (PicBookRankType type : values()) {
            statusMap.put(type.type, type);
        }
    }

    public static PicBookRankType of(Integer type) {
        return statusMap.get(type);
    }
}
