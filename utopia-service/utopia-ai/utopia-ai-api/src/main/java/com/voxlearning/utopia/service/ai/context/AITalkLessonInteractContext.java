package com.voxlearning.utopia.service.ai.context;

import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.TalkResultInfo;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author songtao
 * @since 2018/5/23
 */
@Getter
@Setter
@RequiredArgsConstructor
public class AITalkLessonInteractContext extends AbstractAIContext<AITalkLessonInteractContext> {
    //in
    private User user;
    private LessonType type;
    private String usercode;
    private String lessonId;
    private String qid;
    private String input;
    private String roleName;


    //middle
    private String requestUrl = "";
    private Map<Object, Object> requestParameter = new HashMap<>();
    private TalkResultInfo resultInfo;

    // out
    private Map<String, Object> result = new HashMap<>();

}
