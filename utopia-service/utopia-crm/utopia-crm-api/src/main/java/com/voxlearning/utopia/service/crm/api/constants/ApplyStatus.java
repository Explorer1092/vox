package com.voxlearning.utopia.service.crm.api.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 申请的审核状态
 *
 * @author song.wang
 * @date 2016/12/28
 */
public enum ApplyStatus {
    PENDING(1, "审核中"),
    APPROVED(2, "通过"),
    REJECTED(3, "拒绝"),
    REVOKED(4, "撤销")
    ;

    @Getter
    private final Integer code;
    @Getter
    private final String desc;

    ApplyStatus(Integer code, String desc){
        this.code = code;
        this.desc = desc;
    }

    private static final Map<Integer, ApplyStatus> statusMap;

    static {
        statusMap = new HashMap<>();
        for (ApplyStatus type : values()) {
            statusMap.put(type.getCode(), type);
        }
    }

    public static ApplyStatus typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return statusMap.get(id);
    }

    public static ApplyStatus nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
