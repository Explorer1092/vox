package com.voxlearning.utopia.agent.bean.indicator;

import com.voxlearning.utopia.agent.constants.AgentConstants;

import java.util.Map;

/**
 * BaseParentIndicator
 *
 * @author deliang.che
 * @since  2019/2/21
 */
public abstract class BaseParentIndicator {
    protected abstract Map<Integer, ParentIndicator> getIndicatorMap();

    public ParentIndicator fetchMonthData(){
        ParentIndicator parentIndicator = getIndicatorMap().get(AgentConstants.ONLINE_INDICATOR_MONTH);
        return parentIndicator == null ? new ParentIndicator() : parentIndicator;
    }

    public ParentIndicator fetchDayData(){
        ParentIndicator parentIndicator =  getIndicatorMap().get(AgentConstants.ONLINE_INDICATOR_DAY);
        return parentIndicator == null ? new ParentIndicator() : parentIndicator;
    }

    public ParentIndicator fetchSumData(){
        ParentIndicator parentIndicator = getIndicatorMap().get(AgentConstants.ONLINE_INDICATOR_SUM);
        return parentIndicator == null ? new ParentIndicator() : parentIndicator;
    }

    public ParentIndicator fetchTermData(){
        ParentIndicator parentIndicator = getIndicatorMap().get(AgentConstants.ONLINE_INDICATOR_TERM);
        return parentIndicator == null ? new ParentIndicator() : parentIndicator;
    }
}
