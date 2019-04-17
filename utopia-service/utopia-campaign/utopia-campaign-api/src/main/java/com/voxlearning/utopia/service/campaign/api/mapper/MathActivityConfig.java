package com.voxlearning.utopia.service.campaign.api.mapper;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Data
public class MathActivityConfig implements Serializable{

    private static final long serialVersionUID = 7445427406216746142L;

    private String startTime;
    private Integer regionCode;
    private List<String> timeList;

    public int judgePhase(Date time){
        int defaultPhase = 0;
        if(time == null)
            return defaultPhase;

        List<Date> phaseTimeList = Optional.ofNullable(timeList)
                .orElse(Collections.emptyList())
                .stream()
                .map(t -> DateUtils.stringToDate(t, DateUtils.FORMAT_SQL_DATE))
                .sorted(Date::compareTo)
                .collect(toList());
        if(CollectionUtils.isEmpty(phaseTimeList))
            return defaultPhase;

        int currentPhase = 0;
        for(int i = 3; i >= 1;i --){
            if(time.after(phaseTimeList.get(i - 1))){
                currentPhase = i;
                break;
            }
        }

        return currentPhase;
    }

    public Date parseStartTime(){
        return DateUtils.stringToDate(startTime);
    }
}
