package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Agent 结算数据的前端展示
 * Created by Administrator on 2016/9/23.
 */
@Getter
@Setter
@NoArgsConstructor
public class AgentPaymentsData implements Serializable {
    private static final long serialVersionUID = 3224095066543906523L;
    private String id;                          // ID
    private String departmentName;              // 部门名
    private String userName;                    // 用户名
    private String account;                     // 帐号
    private String role;                        // 角色
    private Integer settlementMonth;            // 结算月份
    private Integer cityJuniorMeet;             // 市级专场会议
    private Integer countyJuniorMeet;           // 区级专场会议

    private Integer cityMiddleMeet;             // 市级专场会议
    private Integer countyMiddleMeet;           // 区级专场会议
    private Integer interCutJuniorMeet;         // 小学插播组会
    private Integer interCutMiddleMeet;         // 中学插播组会
    private Integer juniorTheMothClue;          // 本月线索数小学
    private Integer middleTheMothClue;          // 本月线索数中学
    //private Double royalties;                   // 市经理UStalk 提成

    private String indicator1Name;              // 指标1名称
    private Double indicator1;                  // 大区结算指标1
    private String indicator2Name;              // 指标1名称
    private Double indicator2;                  // 大区结算指标2
}
