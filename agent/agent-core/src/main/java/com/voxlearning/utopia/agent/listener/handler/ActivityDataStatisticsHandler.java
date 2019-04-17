package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.service.activity.ActivityStatisticsService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

@Named
public class ActivityDataStatisticsHandler {

    @Inject
    private ActivityStatisticsService statisticsService;

    public void handle(String activityId, Integer day){


        if(day == null || day < 20181201){
            day = SafeConverter.toInt(DateUtils.dateToString(DateUtils.addDays(new Date(), -1), "yyyyMMdd"));
        }

        Date startDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
        if(startDate == null){
            startDate = DayRange.newInstance(DateUtils.addDays(new Date(), -1).getTime()).getStartDate();
        }
        Date endDate = DateUtils.addDays(startDate, 1);

        if(StringUtils.isBlank(activityId)){
            statisticsService.calOrderStatisticsData(startDate, endDate);
        }else {
            statisticsService.calOrderStatisticsData(activityId, startDate, endDate);
        }

    }
}
