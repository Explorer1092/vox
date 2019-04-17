package com.voxlearning.utopia.agent.persist.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * AgentWeekSubordinateData
 *
 * @author song.wang
 * @date 2016/8/11
 */
@Getter
@Setter
@NoArgsConstructor
public class AgentWeekSubordinateData implements Serializable {
    private static final long serialVersionUID = -4569175539063137480L;

    private Long userId;
    private String userName;
    private Integer ranking; // 当前排名
    private Integer rankingFloat; // 排名变化
    private List<Integer> unWorkedDayList; // 未进校日期
}
