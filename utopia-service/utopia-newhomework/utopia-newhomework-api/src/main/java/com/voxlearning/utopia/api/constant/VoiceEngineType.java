package com.voxlearning.utopia.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tanguohong on 2015/4/3.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum VoiceEngineType {
    ChiVox(0, "驰声公有云引擎"), //需要添加MP3
    ChiVoxOld(1, "驰声私有云引擎"),
    Unisound(2, "云知声公有云引擎"),
    Vox17(3, "自研打分引擎"),
    SingSound(4, "先声打分引擎");

    @Getter private final int key;
    @Getter private final String value;

    private static final Map<String, String> subjectMap;

    static {
        subjectMap = new HashMap<>();
        for (VoiceEngineType voiceEngineType : values()) {
            subjectMap.put(voiceEngineType.name(), voiceEngineType.getValue());
        }
    }

    public static VoiceEngineType of(String value) {
        try {
            return VoiceEngineType.valueOf(value);
        } catch (Exception ignored) {
            return ChiVoxOld;
        }
    }
}
