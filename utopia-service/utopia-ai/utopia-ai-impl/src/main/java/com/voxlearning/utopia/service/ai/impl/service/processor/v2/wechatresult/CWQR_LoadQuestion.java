package com.voxlearning.utopia.service.ai.impl.service.processor.v2.wechatresult;

import com.voxlearning.utopia.service.ai.constant.ChipsErrorType;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.context.ChipsWechatQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;

@Named
public class CWQR_LoadQuestion extends AbstractAiSupport implements IAITask<ChipsWechatQuestionResultContext> {

    @Override
    public void execute(ChipsWechatQuestionResultContext context) {

        ChipsQuestionType questionType = ChipsQuestionType.of(context.getChipsQuestionResultRequest().getQuestionType());
        if (questionType == null || questionType == ChipsQuestionType.unknown) {
            context.errorResponse("题目不存在");
            context.setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
            return;
        }

        context.setQuestionType(questionType);
    }
}
