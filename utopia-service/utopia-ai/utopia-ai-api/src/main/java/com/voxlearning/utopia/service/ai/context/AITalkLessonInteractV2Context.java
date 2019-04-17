package com.voxlearning.utopia.service.ai.context;

import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@RequiredArgsConstructor
public class AITalkLessonInteractV2Context extends AITalkLessonInteractContext {
    //in
    private String bookId;
    private String unitId;

    private ChipsQuestionType questionType;
}
