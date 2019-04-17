package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum OralCommunicationContentType {

    INTERACTIVE_CONVERSATION("oral_practice_conversation", "互动对话"),
    INTERACTIVE_PICTURE_BOOK("interactive_picture_book", "互动绘本"),
    INTERACTIVE_VIDEO("interactive_video", "互动视频");

    @Getter private final String stoneDataScheme;
    @Getter private final String name;

    public static OralCommunicationContentType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ignore) {
            return null;
        }
    }

}
