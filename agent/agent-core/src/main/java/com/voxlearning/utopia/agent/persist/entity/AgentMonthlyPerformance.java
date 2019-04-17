package com.voxlearning.utopia.agent.persist.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentMonthlyPerformance
 *
 * @author song.wang
 * @date 2016/10/25
 */
@Getter
@Setter
public class AgentMonthlyPerformance implements Serializable {

    private Long userId;
    private String userName;
    private Double juniorSascCompleteRate; // 小学单科完成率
    private Double juniorDascCompleteRate; // 小学双科完成率
    private Double middleSascCompleteRate; // 中学单科完成率
}
