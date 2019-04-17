package com.voxlearning.utopia.agent.bean.indicator.grade;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * GradeOnlineIndicator
 *
 * @author song.wang
 * @date 2018/8/3
 */
@Getter
@Setter
public class GradeOnlineIndicator implements Serializable {

    private static final long serialVersionUID = -4343503555789360103L;

    private Long schoolId;
    private Integer day;                                                           // 日期

    private Map<ClazzLevel, Map<Integer, OnlineIndicator>> indicatorMap = new HashMap<>();           // 各个年级的指标数据

    public OnlineIndicator fetchMonthData(ClazzLevel clazzLevel){
        Map<Integer, OnlineIndicator> gradeIndicatorMap = indicatorMap.get(clazzLevel);
        if(MapUtils.isEmpty(gradeIndicatorMap)){
            return new OnlineIndicator();
        }
        OnlineIndicator onlineIndicator = gradeIndicatorMap.get(AgentConstants.ONLINE_INDICATOR_MONTH);
        return onlineIndicator == null ? new OnlineIndicator() : onlineIndicator;
    }

    public OnlineIndicator fetchDayData(ClazzLevel clazzLevel){
        Map<Integer, OnlineIndicator> gradeIndicatorMap = indicatorMap.get(clazzLevel);
        if(MapUtils.isEmpty(gradeIndicatorMap)){
            return new OnlineIndicator();
        }
        OnlineIndicator onlineIndicator = gradeIndicatorMap.get(AgentConstants.ONLINE_INDICATOR_DAY);
        return onlineIndicator == null ? new OnlineIndicator() : onlineIndicator;
    }

    public OnlineIndicator fetchSumData(ClazzLevel clazzLevel){
        Map<Integer, OnlineIndicator> gradeIndicatorMap = indicatorMap.get(clazzLevel);
        if(MapUtils.isEmpty(gradeIndicatorMap)){
            return new OnlineIndicator();
        }
        OnlineIndicator onlineIndicator = gradeIndicatorMap.get(AgentConstants.ONLINE_INDICATOR_SUM);
        return onlineIndicator == null ? new OnlineIndicator() : onlineIndicator;
    }

    public OnlineIndicator fetchTermData(ClazzLevel clazzLevel){
        Map<Integer, OnlineIndicator> gradeIndicatorMap = indicatorMap.get(clazzLevel);
        if(MapUtils.isEmpty(gradeIndicatorMap)){
            return new OnlineIndicator();
        }
        OnlineIndicator onlineIndicator = gradeIndicatorMap.get(AgentConstants.ONLINE_INDICATOR_TERM);
        return onlineIndicator == null ? new OnlineIndicator() : onlineIndicator;
    }
}
