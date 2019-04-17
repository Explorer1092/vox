package com.voxlearning.utopia.agent.view.workrecord;

import lombok.Getter;
import lombok.Setter;

/**
 *
 *
 * @author song.wang
 * @date 2018/6/7
 */
@Getter
@Setter
public class WrStatisticsOverviewRoleData {
    private Integer roleId;
    private String roleName;
    private Integer userCount;
    private Integer fillRecordUserCount;
    private Integer recordUnreachedUserCount;
    private Double perCapitaWorkload;              // 人均工作量
}
