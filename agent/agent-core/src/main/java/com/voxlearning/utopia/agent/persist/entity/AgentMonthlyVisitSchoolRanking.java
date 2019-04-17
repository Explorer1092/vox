package com.voxlearning.utopia.agent.persist.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentMonthlyVisitSchoolRanking
 *
 * @author song.wang
 * @date 2016/8/16
 */
@Getter
@Setter
@NoArgsConstructor
public class AgentMonthlyVisitSchoolRanking implements Serializable {
    private Long userId;
    private String userName;
    private Long groupId;
    private String groupName;
    private Integer visitSchoolCount;
}
