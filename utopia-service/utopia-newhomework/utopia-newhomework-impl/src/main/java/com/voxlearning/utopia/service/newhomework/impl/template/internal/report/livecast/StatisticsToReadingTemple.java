package com.voxlearning.utopia.service.newhomework.impl.template.internal.report.livecast;


import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkReportContext;
import com.voxlearning.utopia.service.newhomework.impl.template.StatisticsToObjectiveConfigTypeTemple;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

@Named
public class StatisticsToReadingTemple extends StatisticsToObjectiveConfigTypeTemple {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.READING;
    }

    @Override
    public LiveHomeworkReport.StatisticsToObjectiveConfigType statisticsToObjectiveConfigType(LiveHomeworkReportContext liveHomeworkReportContext, ObjectiveConfigType type) {
        LiveHomeworkReport.StatisticsToObjectiveConfigType statisticsToObjectiveConfigType = new LiveHomeworkReport.StatisticsToObjectiveConfigType();
        commonHandler(statisticsToObjectiveConfigType, liveHomeworkReportContext, type);
        return statisticsToObjectiveConfigType;
    }
}
