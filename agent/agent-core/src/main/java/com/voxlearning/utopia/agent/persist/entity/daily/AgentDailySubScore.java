package com.voxlearning.utopia.agent.persist.entity.daily;

import com.voxlearning.utopia.agent.constants.AgentDailyScoreSubIndex;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 日报子指标
 *
 * @author deliang.che
 * @since  2019/3/28
 */

@Getter
@Setter
public class AgentDailySubScore implements Serializable {


    private static final long serialVersionUID = 2025317902194729676L;

    private AgentDailyScoreSubIndex index;//指标

    private List<Integer> statisticsNumList;//运算过程中的统计数值列表
    private Double score;               //得分

}

