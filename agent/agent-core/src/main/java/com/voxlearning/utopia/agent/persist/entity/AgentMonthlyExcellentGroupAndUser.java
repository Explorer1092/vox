package com.voxlearning.utopia.agent.persist.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentMonthlyExcellentGroupAndUser
 *
 * @author song.wang
 * @date 2016/10/25
 */
@Getter
@Setter
public class AgentMonthlyExcellentGroupAndUser implements Serializable {

    private Integer type; // 排行榜类型： 1： 大区排行榜 2：市经理排行榜   3：专员排行榜
    private Long userId; // 用户ID
    private String userName;//
    private Long groupId;//
    private String groupName;//
    private Integer ranking;// 排名
    private Integer totalCount; // 参与排名的人员总数
}
