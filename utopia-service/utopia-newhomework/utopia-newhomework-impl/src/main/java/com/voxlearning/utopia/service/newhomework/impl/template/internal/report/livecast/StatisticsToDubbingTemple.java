package com.voxlearning.utopia.service.newhomework.impl.template.internal.report.livecast;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkReportContext;
import com.voxlearning.utopia.service.newhomework.impl.template.StatisticsToObjectiveConfigTypeTemple;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Map;

@Named
public class StatisticsToDubbingTemple  extends StatisticsToObjectiveConfigTypeTemple {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.DUBBING;
    }

    @Override
    public LiveHomeworkReport.StatisticsToObjectiveConfigType statisticsToObjectiveConfigType(LiveHomeworkReportContext liveHomeworkReportContext, ObjectiveConfigType type) {
        LiveHomeworkReport.StatisticsToObjectiveConfigType statisticsToObjectiveConfigType = new LiveHomeworkReport.StatisticsToObjectiveConfigType();
        Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveHomeworkReportContext.getLiveCastHomeworkResultMap();
        Map<Long, User> userMap = liveHomeworkReportContext.getUserMap();
        statisticsToObjectiveConfigType.setTotalStudentNum(userMap.size());
        statisticsToObjectiveConfigType.setObjectiveConfigType(type);
        statisticsToObjectiveConfigType.setObjectiveConfigTypeName(type.getValue());
        int totalDurationToObjectiveConfigType = 0;
        for (LiveCastHomeworkResult liveCastHomeworkResult : liveCastHomeworkResultMap.values()) {
            if (liveCastHomeworkResult.isFinishedOfObjectiveConfigType(type)) {
                statisticsToObjectiveConfigType.setObjectiveConfigTypeFinishedNum(statisticsToObjectiveConfigType.getObjectiveConfigTypeFinishedNum() + 1);
                NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastHomeworkResult.getPractices().get(type);
                totalDurationToObjectiveConfigType += SafeConverter.toInt(newHomeworkResultAnswer.processDuration());
            }
        }
        if (statisticsToObjectiveConfigType.getObjectiveConfigTypeFinishedNum() != 0) {
            statisticsToObjectiveConfigType.setClazzAverDurationToObjectiveConfig(new BigDecimal(totalDurationToObjectiveConfigType).divide(new BigDecimal(60), BigDecimal.ROUND_HALF_UP).intValue());
        }
        return statisticsToObjectiveConfigType;
    }
}
