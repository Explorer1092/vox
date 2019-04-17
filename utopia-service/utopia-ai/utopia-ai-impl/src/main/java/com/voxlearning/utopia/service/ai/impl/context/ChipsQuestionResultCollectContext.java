package com.voxlearning.utopia.service.ai.impl.context;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.context.AbstractAIContext;
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
public class ChipsQuestionResultCollectContext extends AbstractAIContext<ChipsQuestionResultCollectContext> {


    private static final long serialVersionUID = 3656556886322779335L;
    // in
    private String qid;
    private String unitId;
    private String lessonId;
    private String bookId;
    private ChipsQuestionType questionType;
    private String userVideo;
    private Long userId;
    private String input;

    // middle

    private StoneUnitData unit = new StoneUnitData();
    private StoneLessonData lesson = new StoneLessonData();
    private List<AIUserUnitResultPlan> aiUserUnitResultPlans = new ArrayList<>();
    private List<AIUserQuestionResultHistory> questionResultHistoryList = new ArrayList<>();
    private List<AIUserLessonResultHistory> lessonResultHistoryList = new ArrayList<>();


    // out
    private MapMessage result = MapMessage.successMessage();

}
