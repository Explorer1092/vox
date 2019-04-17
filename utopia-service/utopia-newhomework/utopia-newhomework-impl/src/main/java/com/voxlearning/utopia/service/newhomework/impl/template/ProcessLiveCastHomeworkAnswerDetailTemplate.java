package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastReportRateContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract public class ProcessLiveCastHomeworkAnswerDetailTemplate extends NewHomeworkSpringBean {
    abstract public ObjectiveConfigType getObjectiveConfigType();

    abstract public void processNewHomeworkAnswerDetail(LiveCastReportRateContext liveCastReportRateContext);

    abstract public void processNewHomeworkAnswerDetailPersonal(LiveCastReportRateContext liveCastReportRateContext);

    protected Map<String, LiveCastHomeworkResult> handlerNewHomeworkResultMap(Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap, ObjectiveConfigType objectiveConfigType) {
        return liveCastHomeworkResultMap
                .values()
                .stream()
                .filter(o -> o.isFinishedOfObjectiveConfigType(objectiveConfigType))
                .collect(Collectors
                        .toMap(LiveCastHomeworkResult::getId, Function.identity()));
    }
}
