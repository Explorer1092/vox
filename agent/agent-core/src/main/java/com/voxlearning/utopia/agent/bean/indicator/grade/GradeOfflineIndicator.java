package com.voxlearning.utopia.agent.bean.indicator.grade;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.agent.bean.indicator.BaseOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OfflineIndicator;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * GradeOfflineIndicator
 *
 * @author song.wang
 * @date 2018/8/21
 */
@Getter
@Setter
public class GradeOfflineIndicator  implements Serializable {
    private static final long serialVersionUID = -4396863782437818921L;

    private Long schoolId;
    private Integer day;                                                           // 日期

    private Map<ClazzLevel, Map<Integer, OfflineIndicator>> indicatorMap = new HashMap<>();           // 各个年级的指标数据

    public OfflineIndicator fetchMonthData(ClazzLevel clazzLevel){
        Map<Integer, OfflineIndicator> gradeIndicatorMap = indicatorMap.get(clazzLevel);
        if(MapUtils.isEmpty(gradeIndicatorMap)){
            return new OfflineIndicator();
        }
        OfflineIndicator offlineIndicator = gradeIndicatorMap.get(AgentConstants.OFFLINE_INDICATOR_MONTH);
        return offlineIndicator == null ? new OfflineIndicator() : offlineIndicator;
    }

    public OfflineIndicator fetchDayData(ClazzLevel clazzLevel){
        Map<Integer, OfflineIndicator> gradeIndicatorMap = indicatorMap.get(clazzLevel);
        if(MapUtils.isEmpty(gradeIndicatorMap)){
            return new OfflineIndicator();
        }
        OfflineIndicator offlineIndicator = gradeIndicatorMap.get(AgentConstants.OFFLINE_INDICATOR_DAY);
        return offlineIndicator == null ? new OfflineIndicator() : offlineIndicator;
    }

    public OfflineIndicator fetchSumData(ClazzLevel clazzLevel){
        Map<Integer, OfflineIndicator> gradeIndicatorMap = indicatorMap.get(clazzLevel);
        if(MapUtils.isEmpty(gradeIndicatorMap)){
            return new OfflineIndicator();
        }
        OfflineIndicator offlineIndicator = gradeIndicatorMap.get(AgentConstants.OFFLINE_INDICATOR_SUM);
        return offlineIndicator == null ? new OfflineIndicator() : offlineIndicator;
    }

    public OfflineIndicator fetchTermData(ClazzLevel clazzLevel){
        Map<Integer, OfflineIndicator> gradeIndicatorMap = indicatorMap.get(clazzLevel);
        if(MapUtils.isEmpty(gradeIndicatorMap)){
            return new OfflineIndicator();
        }
        OfflineIndicator offlineIndicator = gradeIndicatorMap.get(AgentConstants.OFFLINE_INDICATOR_TERM);
        return offlineIndicator == null ? new OfflineIndicator() : offlineIndicator;
    }
}
