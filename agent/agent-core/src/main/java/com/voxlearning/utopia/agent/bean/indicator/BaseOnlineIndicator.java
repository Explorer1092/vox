package com.voxlearning.utopia.agent.bean.indicator;

import com.voxlearning.utopia.agent.constants.AgentConstants;

import java.util.Map;

/**
 * BaseOnlineIndicator
 *
 * @author song.wang
 * @date 2018/8/21
 */
public abstract class BaseOnlineIndicator {
    protected abstract Map<Integer, OnlineIndicator> getIndicatorMap();

    public OnlineIndicator fetchMonthData(){
        OnlineIndicator onlineIndicator = getIndicatorMap().get(AgentConstants.ONLINE_INDICATOR_MONTH);
        return onlineIndicator == null ? new OnlineIndicator() : onlineIndicator;
    }

    public OnlineIndicator fetchDayData(){
        OnlineIndicator onlineIndicator =  getIndicatorMap().get(AgentConstants.ONLINE_INDICATOR_DAY);
        return onlineIndicator == null ? new OnlineIndicator() : onlineIndicator;
    }

    public OnlineIndicator fetchSumData(){
        OnlineIndicator onlineIndicator = getIndicatorMap().get(AgentConstants.ONLINE_INDICATOR_SUM);
        return onlineIndicator == null ? new OnlineIndicator() : onlineIndicator;
    }

    public OnlineIndicator fetchTermData(){
        OnlineIndicator onlineIndicator = getIndicatorMap().get(AgentConstants.ONLINE_INDICATOR_TERM);
        return onlineIndicator == null ? new OnlineIndicator() : onlineIndicator;
    }
}
