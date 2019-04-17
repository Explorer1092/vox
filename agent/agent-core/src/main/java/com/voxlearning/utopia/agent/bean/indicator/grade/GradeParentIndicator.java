package com.voxlearning.utopia.agent.bean.indicator.grade;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.agent.bean.indicator.ParentIndicator;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * GradeParentIndicator
 *
 * @author deliang.che
 * @since  2019/2/21
 */
@Getter
@Setter
public class GradeParentIndicator implements Serializable {

    private static final long serialVersionUID = -1296775773950402339L;

    private Long schoolId;
    private Integer day;                                                           // 日期

    private Map<ClazzLevel, Map<Integer, ParentIndicator>> indicatorMap = new HashMap<>();           // 各个年级的指标数据

    public ParentIndicator fetchMonthData(ClazzLevel clazzLevel){
        Map<Integer, ParentIndicator> gradeIndicatorMap = indicatorMap.get(clazzLevel);
        if(MapUtils.isEmpty(gradeIndicatorMap)){
            return new ParentIndicator();
        }
        ParentIndicator parentIndicator = gradeIndicatorMap.get(AgentConstants.ONLINE_INDICATOR_MONTH);
        return parentIndicator == null ? new ParentIndicator() : parentIndicator;
    }

    public ParentIndicator fetchDayData(ClazzLevel clazzLevel){
        Map<Integer, ParentIndicator> gradeIndicatorMap = indicatorMap.get(clazzLevel);
        if(MapUtils.isEmpty(gradeIndicatorMap)){
            return new ParentIndicator();
        }
        ParentIndicator parentIndicator = gradeIndicatorMap.get(AgentConstants.ONLINE_INDICATOR_DAY);
        return parentIndicator == null ? new ParentIndicator() : parentIndicator;
    }

    public ParentIndicator fetchSumData(ClazzLevel clazzLevel){
        Map<Integer, ParentIndicator> gradeIndicatorMap = indicatorMap.get(clazzLevel);
        if(MapUtils.isEmpty(gradeIndicatorMap)){
            return new ParentIndicator();
        }
        ParentIndicator parentIndicator = gradeIndicatorMap.get(AgentConstants.ONLINE_INDICATOR_SUM);
        return parentIndicator == null ? new ParentIndicator() : parentIndicator;
    }

    public ParentIndicator fetchTermData(ClazzLevel clazzLevel){
        Map<Integer, ParentIndicator> gradeIndicatorMap = indicatorMap.get(clazzLevel);
        if(MapUtils.isEmpty(gradeIndicatorMap)){
            return new ParentIndicator();
        }
        ParentIndicator parentIndicator = gradeIndicatorMap.get(AgentConstants.ONLINE_INDICATOR_TERM);
        return parentIndicator == null ? new ParentIndicator() : parentIndicator;
    }
}
