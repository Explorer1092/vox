package com.voxlearning.utopia.api.constant;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/5/18.
 */
@Getter
public enum CrmSchoolClueExternOrBoarder {
    EXTERN(1),  //走读
    BOARDER(2), //寄宿
    EXTERNANDBOARDER(3); //走读/寄宿

    public final int code;

    CrmSchoolClueExternOrBoarder(int code) {
        this.code = code;
    }

    public static CrmSchoolClueExternOrBoarder codeOf(Integer code) {
        return CODE_MAP.get(code);
    }

    private static final Map<Integer, CrmSchoolClueExternOrBoarder> CODE_MAP;

    static {
        CODE_MAP = new HashMap<>();
        for (CrmSchoolClueExternOrBoarder status : CrmSchoolClueExternOrBoarder.values()) {
            CODE_MAP.put(status.code, status);
        }
    }

    public static CrmSchoolClueExternOrBoarder safeParse(Integer value) {
        return safeParse(value, BOARDER);
    }

    public static CrmSchoolClueExternOrBoarder safeParse(Integer value, CrmSchoolClueExternOrBoarder defaultLevel) {
        if (value == null) return defaultLevel;
        return CODE_MAP.getOrDefault(value, defaultLevel);
    }
}
