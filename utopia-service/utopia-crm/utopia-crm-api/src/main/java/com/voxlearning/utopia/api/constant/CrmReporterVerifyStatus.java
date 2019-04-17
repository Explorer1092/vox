package com.voxlearning.utopia.api.constant;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang wei on 2016/7/26.
 */


@Getter
public enum CrmReporterVerifyStatus {
    已驳回(3),
    待审核(1),
    NULL(0),
    已通过(2);

    public final int code;

    CrmReporterVerifyStatus(int code) {
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
