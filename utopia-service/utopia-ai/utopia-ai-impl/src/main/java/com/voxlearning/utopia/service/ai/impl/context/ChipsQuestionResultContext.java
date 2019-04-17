package com.voxlearning.utopia.service.ai.impl.context;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.context.AbstractAIContext;
import com.voxlearning.utopia.service.ai.data.ChipsQuestionResultRequest;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultPlan;
import com.voxlearning.utopia.service.ai.data.StoneLessonData;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ChipsQuestionResultContext extends AbstractAIContext<ChipsQuestionResultContext> {

    private static final long serialVersionUID = -8746327867170805154L;

    // in
    private ChipsQuestionResultRequest chipsQuestionResultRequest;
    private Long userId;

    // middle
    private ChipsQuestionType questionType;
    private StoneUnitData unit = new StoneUnitData();
    private StoneLessonData lesson = new StoneLessonData();
    private List<AIUserUnitResultPlan> aiUserUnitResultPlans = new ArrayList<>();
    private List<AIUserQuestionResultHistory> unitQuestionResultList = new ArrayList<>();
    private List<AIUserLessonResultHistory> lessonResultHistoryList = new ArrayList<>();


    // out
    private MapMessage result = MapMessage.successMessage();


    public ChipsQuestionResultContext(ChipsQuestionResultRequest chipsQuestionResultRequest, Long userId) {
        this.userId = userId;
        this.chipsQuestionResultRequest = chipsQuestionResultRequest;
    }

}
