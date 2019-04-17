package com.voxlearning.utopia.service.ai.impl.context;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.context.AbstractAIContext;
import com.voxlearning.utopia.service.ai.data.ChipsQuestionResultRequest;
import com.voxlearning.utopia.service.ai.data.StoneLessonData;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ChipsWechatQuestionResultContext extends AbstractAIContext<ChipsWechatQuestionResultContext> {

    private static final long serialVersionUID = -8746327867170805154L;

    // in
    private ChipsQuestionResultRequest chipsQuestionResultRequest;
    private Long userId;

    // middle
    private ChipsQuestionType questionType;
    private StoneUnitData unit = new StoneUnitData();
    private StoneLessonData lesson = new StoneLessonData();
    private List<ChipsWechatUserQuestionResultHistory> unitQuestionResultList = new ArrayList<>();
    private List<ChipsWechatUserLessonResultHistory> unitLessonResultList = new ArrayList<>();

    // out
    private MapMessage result = MapMessage.successMessage();


    public ChipsWechatQuestionResultContext(ChipsQuestionResultRequest chipsQuestionResultRequest, Long userId) {
        this.userId = userId;
        this.chipsQuestionResultRequest = chipsQuestionResultRequest;
    }

}
