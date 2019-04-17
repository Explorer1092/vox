package com.voxlearning.utopia.agent.persist.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentMonthlyPerformanceDistribution
 *
 * @author song.wang
 * @date 2016/10/25
 */
@Getter
@Setter
public class AgentMonthlyPerformanceDistribution implements Serializable {
    private Long groupId;
    private String groupName;
    private Long userId;
    private Double rankingRate; // 在排行榜中的位置（比例）
    private Integer interval1Count; // 排行分布在区间1的用户数量
    private Integer interval2Count; // 排行分布在区间2的用户数量
    private Integer interval3Count; // 排行分布在区间3的用户数量


}
