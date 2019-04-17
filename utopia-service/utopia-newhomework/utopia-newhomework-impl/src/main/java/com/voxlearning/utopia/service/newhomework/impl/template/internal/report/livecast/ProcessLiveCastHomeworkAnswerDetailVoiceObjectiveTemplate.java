package com.voxlearning.utopia.service.newhomework.impl.template.internal.report.livecast;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

@Named
public class ProcessLiveCastHomeworkAnswerDetailVoiceObjectiveTemplate extends ProcessLiveCastHomeworkAnswerDetailPhotoObjectiveTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.VOICE_OBJECTIVE;
    }
}
