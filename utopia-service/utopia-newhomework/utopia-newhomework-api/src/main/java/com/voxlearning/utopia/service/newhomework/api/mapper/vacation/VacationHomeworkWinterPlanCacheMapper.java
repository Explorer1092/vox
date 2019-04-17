package com.voxlearning.utopia.service.newhomework.api.mapper.vacation;

import com.voxlearning.utopia.service.question.api.entity.WinterDayPlan;
import com.voxlearning.utopia.service.question.api.entity.WinterWeekPlan;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by tanguohong on 2016/12/5.
 */
@Getter
@Setter
public class VacationHomeworkWinterPlanCacheMapper implements Serializable{

    private static final long serialVersionUID = -1333795169503749778L;

    private String id;
    private String bookId;
    private LinkedHashMap<String, WinterWeekPlan> weekPlan;
    private LinkedHashMap<String, List<String>> weekPlanDays;
    private LinkedHashMap<String, WinterDayPlan> dayPlan;   // key为weekRank + "-" + dayRank
}
