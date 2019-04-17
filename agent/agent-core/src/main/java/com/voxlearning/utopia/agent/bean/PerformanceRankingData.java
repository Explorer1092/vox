package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

/**
 * PerformanceRankingData
 *
 * @author song.wang
 * @date 2016/7/18
 */

@Getter
@Setter
public class PerformanceRankingData implements Serializable{

    private static final DecimalFormat df   = new DecimalFormat("##0.00");
    private Integer type; // 排行榜类型： 1： 大区排行榜 2：市经理排行榜   3：专员排行榜
    private Long userId;
    private String userName;
    private Long groupId;
    private String groupName;
    private Integer ranking;
    private Integer rankingFloat;
    private Integer totalCount; // 参与排名的人员总数
    private List<PerformanceRankingData> subordinateDataList; // 下属人员的排行信息

}
