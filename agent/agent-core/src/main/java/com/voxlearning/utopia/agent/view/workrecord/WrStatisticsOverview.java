package com.voxlearning.utopia.agent.view.workrecord;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 *  工作量统计概览
 *
 * @author song.wang
 * @date 2018/6/7
 */
@Getter
@Setter
public class WrStatisticsOverview {
    private Long groupId;                            // 部门ID
    private String groupName;                        // 部门名称

    private String schoolLevelName;
    private List<WrStatisticsOverviewRoleData> roleDataList = new ArrayList<>();

}
