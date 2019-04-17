package com.voxlearning.utopia.agent.view.workrecord;

import lombok.Getter;
import lombok.Setter;

/**
 * WrStatisticsUserData
 *
 * @author song.wang
 * @date 2018/6/8
 */
@Getter
@Setter
public class WrStatisticsUserData {
    private Long userId;
    private String userName;
    private Double userIntoSchoolWorkload;                   // 进校工作量
    private Double userVisitWorkload;                        // 陪访工作量
    private Double userWorkload;                             // 全部工作量（部门经理或者专员）
    private Integer userWorkDays;                            // 工作天数
    private Integer userNeedWordDays;                        // 需要工作的天数（工作日）
}
