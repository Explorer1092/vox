package com.voxlearning.utopia.service.vendor.api.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author peng
 * @since 16-6-27
 * 不同平台产品描述不同
 */
public enum FairyLandPlatform {
    STUDENT_PC("学生PC平台"), STUDENT_APP("学生app平台"), PARENT_APP("家长app平台"),PARENT_OA("家长自学乐园公众号");

    static final public Map<String, String> map = new HashMap();

    static {
        for (FairyLandPlatform platformType : FairyLandPlatform.values()) {
            map.put(platformType.name(), platformType.descName);
        }
    }

    public String descName;

    FairyLandPlatform(String descName) {
        this.descName = descName;
    }

    public static FairyLandPlatform of(String platform) {
        try {

            return valueOf(platform);
        } catch (Exception e) {
            return null;
        }
    }
}