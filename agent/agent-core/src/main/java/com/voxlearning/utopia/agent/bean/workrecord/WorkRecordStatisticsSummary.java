package com.voxlearning.utopia.agent.bean.workrecord;

import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chunlin.yu
 * @create 2018-01-23 15:46
 **/
@Setter
@Getter
public class WorkRecordStatisticsSummary {

    private Long groupId;                            // 部门ID
    private String groupName;                        // 部门名称

    private Integer groupUserCount;                  // 部门下所有人员数量
    private Integer fillInWorkRecordUserCount;       // 填写工作记录的人员数量

    // 下属工作情况
    //---------专员
    private Integer bdUserCount;                     // 专员数量
    private Integer bdFillInWorkRecordUserCount;     // 填写工作记录的专员数量
    private Double bdPerCapitaWorkload;             // 专员人均日均工作量

    //---------市经理
    private Integer cmUserCount;                     // 市经理数量
    private Integer cmFillInWorkRecordUserCount;     // 填写工作记录的市经理数量
    private Double cmPerCapitaWorkload;             // 市经理人均日均工作量


    //---------区域经理
    private Integer amUserCount;                     // 区域经理数量
    private Integer amFillInWorkRecordUserCount;     // 填写工作记录的区域经理数量
    private Double amPerCapitaWorkload;             // 区域经理人均日均工作量

    //---------大区经理
    private Integer rmUserCount;                     // 大区经理数量
    private Integer rmFillInWorkRecordUserCount;     // 填写工作记录的大区经理数量
    private Double rmPerCapitaWorkload;             // 大区经理人均日均工作量


    //---------专员进校情况
    private Double bdPerCapitaIntoSchool;             // 专员人均日均进校次数
    private Double bdVisitSchoolAvgTeaCount;           // 校均拜访老师数
    private Double bdVisitEngTeaPercent;                 // 专员拜访英语老师占比
    private Double bdVisitMathTeaPercent;              // 专员拜访的数学老师占比
}
