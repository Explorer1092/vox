package com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.data.StoneLessonData;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;
import java.util.Collections;
import java.util.Optional;

@Named
public class CQR_LoadLesson extends AbstractAiSupport implements IAITask<ChipsQuestionResultContext> {

    @Override
    public void execute(ChipsQuestionResultContext context) {
        StoneLessonData lessonData = Optional.ofNullable(context.getChipsQuestionResultRequest()).map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singletonList(e.getLessonId())))
                .filter(e -> MapUtils.isNotEmpty(e))
                .map(e -> e.get(context.getChipsQuestionResultRequest().getLessonId()))
                .map(StoneLessonData::newInstance)
                .orElse(null);
        if (lessonData == null) {
            context.errorResponse("lesson 不存在");
            return;
        }
        context.setLesson(lessonData);
    }
}
