package com.voxlearning.utopia.service.ai.impl.context;

import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.context.AbstractAIContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChipsNewTalkCollectContext extends AbstractAIContext<ChipsNewTalkCollectContext> {


    private static final long serialVersionUID = 3656556886322779335L;
    private Long userId;
    private String qid;
    private String unitId;
    private String lessonId;
    private String bookId;
    private ChipsQuestionType questionType;
    private LessonType lessonType;

    private String input;
    private String level;
    private String userVideo;
}
