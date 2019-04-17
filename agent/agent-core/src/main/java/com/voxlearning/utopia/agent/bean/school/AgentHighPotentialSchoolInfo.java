package com.voxlearning.utopia.agent.bean.school;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import lombok.Getter;
import lombok.Setter;

/**
 * 高潜校信息
 *
 * @author deliang.che
 * @since 2018/7/31
 */
@Getter
@Setter
public class AgentHighPotentialSchoolInfo {
    private Long schoolId;
    private String schoolName;
    private SchoolLevel schoolLevel;                            // 学校等级
    private AuthenticationState authState;                      // 认证状态
    private AgentSchoolPopularityType schoolPopularityType;     // 名校，重点校，普通校
    private Boolean hasBd = false;                              // 是否有专员负责
    private Long bdId;                                          // 专员ID
    private String bdName;                                      // 专员姓名
    private String address;                                     //地址

    private int finChnHwEq1UnSettleStuCount;                // 本月语文新增1套
    private int finChnHwEq2UnSettleStuCount;                // 本月语文新增2套

    private int finMathHwEq1UnSettleStuCount;               // 本月数学新增1套
    private int finMathHwEq2UnSettleStuCount;               // 本月数学新增2套

    private int finEngHwEq1UnSettleStuCount;                // 本月英语新增1套
    private int finEngHwEq2UnSettleStuCount;                // 本月英语新增2套

    private int mauPotentialValue;                          //月活潜力值

    private String genDistance; // 距离当前坐标距离



}
