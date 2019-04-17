package com.voxlearning.utopia.api.constant;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jia HuanYin
 * @since 2015/11/9
 */
@Getter
public enum CrmSchoolClueStatus {

    已驳回(-1),
    草稿(0),
    待审核(1),
    已通过(2);

    public final int code;

    CrmSchoolClueStatus(int code) {
        this.code = code;
    }

    public static CrmSchoolClueStatus codeOf(Integer code) {
        return CODE_MAP.get(code);
    }

    private static final Map<Integer, CrmSchoolClueStatus> CODE_MAP;

    static {
        CODE_MAP = new HashMap<>();
        for (CrmSchoolClueStatus status : CrmSchoolClueStatus.values()) {
            CODE_MAP.put(status.code, status);
        }
    }
}
