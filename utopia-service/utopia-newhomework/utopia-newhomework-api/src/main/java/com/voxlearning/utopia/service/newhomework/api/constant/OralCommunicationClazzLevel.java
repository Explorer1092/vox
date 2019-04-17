package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum OralCommunicationClazzLevel {
    LOW("低中年级", "junior"),
    HIGH("高年级", "senior"),
    ALL("全年级", "all");

    @Getter private final String name;
    @Getter private final String grade;

    public static OralCommunicationClazzLevel of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ignore) {
            return null;
        }
    }

    public static OralCommunicationClazzLevel ofGrade(String grade) {
        try {
            for (OralCommunicationClazzLevel clazzLevel : values()) {
                if (clazzLevel.getGrade().equals(grade)) {
                    return clazzLevel;
                }
            }
        } catch (Exception ignore) {
            return null;
        }
        return null;
    }
}
