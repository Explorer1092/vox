package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum OutsideReadingBookType {

    NOVEL("1", "小说"),
    POETRY("2", "诗歌"),
    FAIRYTALE("3", "童话"),
    FABLE("4", "寓言"),
    PROSE("5", "散文"),
    LEGENDS("6", "神话传说"),
    STORIES("7", "儿童故事");


    @Getter private final String genre;
    @Getter private final String name;

    public static OutsideReadingBookType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
