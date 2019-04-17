package com.voxlearning.utopia.service.ai.impl.service.processor.v2.wechatresult;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.ai.constant.ChipsErrorType;
import com.voxlearning.utopia.service.ai.data.StoneLessonData;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.context.ChipsWechatQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;
import java.util.Collections;
import java.util.Optional;

@Named
public class CWQR_LoadLesson extends AbstractAiSupport implements IAITask<ChipsWechatQuestionResultContext> {

    @Override
    public void execute(ChipsWechatQuestionResultContext context) {
        StoneLessonData lessonData = Optional.ofNullable(context.getChipsQuestionResultRequest()).map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singletonList(e.getLessonId())))
                .filter(e -> MapUtils.isNotEmpty(e))
                .map(e -> e.get(context.getChipsQuestionResultRequest().getLessonId()))
                .map(StoneLessonData::newInstance)
                .orElse(null);
        if (lessonData == null) {
            context.errorResponse("课程不存在");
            context.setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
            return;
        }
        context.setLesson(lessonData);
    }
}
