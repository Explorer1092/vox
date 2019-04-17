package com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult;

import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;

@Named
public class CQR_LoadQuestion extends AbstractAiSupport implements IAITask<ChipsQuestionResultContext> {

    @Override
    public void execute(ChipsQuestionResultContext context) {

        ChipsQuestionType questionType = ChipsQuestionType.of(context.getChipsQuestionResultRequest().getQuestionType());
        if (questionType == null || questionType == ChipsQuestionType.unknown) {
            context.errorResponse("question type error.");
            return;
        }

        context.setQuestionType(questionType);
    }
}
