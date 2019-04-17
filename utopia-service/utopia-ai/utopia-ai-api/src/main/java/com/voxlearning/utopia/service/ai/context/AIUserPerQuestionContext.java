package com.voxlearning.utopia.service.ai.context;

import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.TalkResultInfo;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
@RequiredArgsConstructor
public class AIUserPerQuestionContext extends AbstractAIContext<AIUserPerQuestionContext> {
    //in
    private User user;
    private LessonType type;
    private String lessonId;
    private String qid;
    private String input;
    private String unitId;


    //middle
    private String requestUrl = "";
    private Map<Object, Object> requestParameter = new HashMap<>();
    private TalkResultInfo resultInfo;

    // out
    private Map<String, Object> result = new HashMap<>();

}
