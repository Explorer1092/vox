package com.voxlearning.utopia.service.newhomework.api.constant;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author guoqiang.li
 * @since 2016/9/7
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum OfflineHomeworkContentType {
    READ("朗读课文", true, true, false),
    LISTEN("听课文磁带", true, true, false),
    DICTATION("听写单词", false, true, false),
    CUSTOMIZE("自定义作业", false, false, true);

    @Getter
    private final String description;
    @Getter
    private final boolean needPracticeCount;
    @Getter
    private final boolean needBookUnit;
    @Getter
    private final boolean needCustomContent;

    public static OfflineHomeworkContentType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static List<OfflineHomeworkContentType> getSubjectTypes(Subject subject) {
        if (Subject.ENGLISH == subject) {
            return Arrays.asList(READ, LISTEN, DICTATION, CUSTOMIZE);
        } else {
            return Collections.singletonList(CUSTOMIZE);
        }
    }
}
