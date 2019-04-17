package com.voxlearning.utopia.agent.view.workrecord;

import lombok.Getter;
import lombok.Setter;

/**
 * WrStatisticsGroupData
 *
 * @author song.wang
 * @date 2018/6/8
 */
@Getter
@Setter
public class WrStatisticsGroupData {
    private Long groupId;
    private String groupName;
    private Integer userCount;
    private Integer recordUnreachedUserCount;        // 填写工作记录未达标的用户数（当天未录入， 3天未录入， 5天未录入）
    private Double perCapitaWorkload;              // 人均工作量
    private boolean clickable;                     // 是否可点击下钻
}
