package com.voxlearning.utopia.service.ai.context;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.data.AIUserQuestionResultRequest;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultPlan;
import com.voxlearning.utopia.service.ai.entity.QuestionWeekPoint;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Summer on 2018/3/29
 */
@Getter
@Setter
@RequiredArgsConstructor
public class AIUserQuestionContext extends AbstractAIContext<AIUserQuestionContext> {

    private static final long serialVersionUID = -8746327867170805154L;

    // in
    private AIUserQuestionResultRequest aiUserQuestionResultRequest;
    private User user;
    private String data;

    // middle
    private List<QuestionWeekPoint> weekPoints = new ArrayList<>();
    private String sessionId = "";
    private boolean lessonUpdate = false;
    private List<AIUserUnitResultPlan> aiUserUnitResultPlans = new ArrayList<>();

    // out
    private MapMessage result = MapMessage.successMessage();

}
