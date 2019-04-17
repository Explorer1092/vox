package com.voxlearning.utopia.service.newexam.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum NewExamQuestionType {
    Oral(1),
    Choice(2),
    Blank(3);
    @Getter
    private final int key;
}
