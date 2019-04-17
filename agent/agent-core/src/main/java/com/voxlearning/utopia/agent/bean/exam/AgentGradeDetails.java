package com.voxlearning.utopia.agent.bean.exam;

import com.voxlearning.athena.bean.bigexam.ArtScienceCondition;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author chunlin.yu
 * @create 2018-04-19 22:23
 **/
@Getter
@Setter
public class AgentGradeDetails implements Serializable{
    private static final long serialVersionUID = 3047703348524530154L;

    private ArtScienceCondition agentArtScienceCondition;

    private List<AgentAutoApplyStatistic> agentAutoApplyStatistics;

    private List<AgentScanDetails> agentScanDetails;



}
