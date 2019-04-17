package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkReportContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.math.BigDecimal;
import java.util.Map;

abstract public class StatisticsToObjectiveConfigTypeTemple extends NewHomeworkSpringBean {
    abstract public ObjectiveConfigType getObjectiveConfigType();

    abstract public LiveHomeworkReport.StatisticsToObjectiveConfigType statisticsToObjectiveConfigType(LiveHomeworkReportContext liveHomeworkReportContext, ObjectiveConfigType type);


    //1、初始化数据：liveCastHomeworkResultMap、userMap、liveCastHomeworkProcessResultMap、NewHomeworkPracticeContent、questions
    //2、设置全部人数和类型信息
    //3、对liveCastHomeworkResultMap进行循环统计
    //3->统计类型完成人数、时间、分数
    //4、数据归整
    protected void commonHandler(LiveHomeworkReport.StatisticsToObjectiveConfigType statisticsToObjectiveConfigType, LiveHomeworkReportContext liveHomeworkReportContext, ObjectiveConfigType type) {
        Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveHomeworkReportContext.getLiveCastHomeworkResultMap();
        Map<Long, User> userMap = liveHomeworkReportContext.getUserMap();
        statisticsToObjectiveConfigType.setTotalStudentNum(userMap.size());
        statisticsToObjectiveConfigType.setObjectiveConfigType(type);
        statisticsToObjectiveConfigType.setObjectiveConfigTypeName(type.getValue());
        int totalScoreToObjectiveConfigType = 0;
        int totalDurationToObjectiveConfigType = 0;
        for (LiveCastHomeworkResult liveCastHomeworkResult : liveCastHomeworkResultMap.values()) {
            if (liveCastHomeworkResult.isFinishedOfObjectiveConfigType(type)) {
                statisticsToObjectiveConfigType.setObjectiveConfigTypeFinishedNum(statisticsToObjectiveConfigType.getObjectiveConfigTypeFinishedNum() + 1);
                NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastHomeworkResult.getPractices().get(type);
                totalScoreToObjectiveConfigType += SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
                totalDurationToObjectiveConfigType += SafeConverter.toInt(newHomeworkResultAnswer.processDuration());
            }
        }
        if (statisticsToObjectiveConfigType.getObjectiveConfigTypeFinishedNum() != 0) {
            statisticsToObjectiveConfigType.setClazzAverScoreToObjectiveConfig(new BigDecimal(totalScoreToObjectiveConfigType).divide(new BigDecimal(statisticsToObjectiveConfigType.getObjectiveConfigTypeFinishedNum()), 0, BigDecimal.ROUND_HALF_UP).intValue());
            statisticsToObjectiveConfigType.setClazzAverDurationToObjectiveConfig(new BigDecimal(totalDurationToObjectiveConfigType).divide(new BigDecimal(60), BigDecimal.ROUND_HALF_UP).intValue());
        }
    }

}
