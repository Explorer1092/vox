package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum HomeworkQueueMessageType {
    UNKNOWN("Unknown"),
    SJHPR("SaveJournalNewHomeworkProcessResult");

    @Getter private final String description;

    public static HomeworkQueueMessageType safeParse(String name) {
        try {
            return HomeworkQueueMessageType.valueOf(name);
        } catch (Exception ex) {
            return UNKNOWN;
        }
    }
}
