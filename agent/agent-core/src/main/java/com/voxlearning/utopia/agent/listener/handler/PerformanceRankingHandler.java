package com.voxlearning.utopia.agent.listener.handler;


import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.service.ranking.AgentRankingService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by alex on 2016/7/20.
 */
@Named
public class PerformanceRankingHandler extends SpringContainerSupport {

    @Inject
    private AgentRankingService agentRankingService;

    public void runningRanking(int date) {
        agentRankingService.generateRanking(date);
    }
}
