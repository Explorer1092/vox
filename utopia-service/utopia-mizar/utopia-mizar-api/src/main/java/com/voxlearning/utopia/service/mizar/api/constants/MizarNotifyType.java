package com.voxlearning.utopia.service.mizar.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MizarNotifyType implements Serializable {

    ADMIN_NOTICE(0, "系统通知"),
    SMS_NOTICE(1, "短信通知"),

    AUDIT_NOTICE(2, "审核变更"),
    AUDIT_DONE(3, "审核完成"),

    UNKNOWN(-1, "未知类型"), // 用于容错
    ;

    @Getter private final int id;
    @Getter private final String desc;

    private static final Map<String, MizarNotifyType> NOTIFY_MAP;

    static {
        NOTIFY_MAP = new LinkedHashMap<>();
        for (MizarNotifyType type : values()) {
            NOTIFY_MAP.put(type.name(), type);
        }
        NOTIFY_MAP.remove(UNKNOWN.name());
    }

    public static Map<String, MizarNotifyType> toMap() {
        return NOTIFY_MAP;
    }

    public static MizarNotifyType parse(String type) {
        if (type != null) {
            return NOTIFY_MAP.get(type);
        }
        return null;
    }

    public static MizarNotifyType safeParse(String type) {
        if (type != null) {
            return NOTIFY_MAP.get(type);
        }
        return UNKNOWN;
    }
}
