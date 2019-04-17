package com.voxlearning.utopia.agent.bean.indicator;

import com.voxlearning.utopia.agent.constants.AgentConstants;

import java.util.Map;

/**
 * BaseOfflineIndicator
 *
 * @author song.wang
 * @date 2018/8/21
 */
public abstract class BaseOfflineIndicator {

    protected abstract Map<Integer, OfflineIndicator> getIndicatorMap();

    public OfflineIndicator fetchMonthData(){
        OfflineIndicator offlineIndicator = getIndicatorMap().get(AgentConstants.OFFLINE_INDICATOR_MONTH);
        return offlineIndicator == null ? new OfflineIndicator() : offlineIndicator;
    }

    public OfflineIndicator fetchDayData(){
        OfflineIndicator offlineIndicator =  getIndicatorMap().get(AgentConstants.OFFLINE_INDICATOR_DAY);
        return offlineIndicator == null ? new OfflineIndicator() : offlineIndicator;
    }

    public OfflineIndicator fetchSumData(){
        OfflineIndicator offlineIndicator = getIndicatorMap().get(AgentConstants.OFFLINE_INDICATOR_SUM);
        return offlineIndicator == null ? new OfflineIndicator() : offlineIndicator;
    }

    public OfflineIndicator fetchTermData(){
        OfflineIndicator offlineIndicator = getIndicatorMap().get(AgentConstants.OFFLINE_INDICATOR_TERM);
        return offlineIndicator == null ? new OfflineIndicator() : offlineIndicator;
    }
}
