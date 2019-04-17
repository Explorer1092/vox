package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PictureBookPracticeType {
    READING("阅读绘本", "阅读绘本"),
    WORDS("高频词练习", "高频词练习"),
    ORAL("跟读", "跟读绘本重点句子"),
    EXAM("习题", "检测对绘本的理解"),
    DUBBING("配音", "录制完整绘本内容");

    @Getter private final String typeName;
    @Getter private final String description;

    public static PictureBookPracticeType of(String type) {
        try {
            return PictureBookPracticeType.valueOf(type);
        } catch (Exception e) {
            return null;
        }
    }
}
