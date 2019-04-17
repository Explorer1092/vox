package com.voxlearning.utopia.agent.bean.school;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import lombok.Getter;
import lombok.Setter;

/**
 * 月活top校信息
 *
 * @author deliang.che
 * @since 2018/8/1
 */
@Getter
@Setter
public class AgentMauTopSchoolInfo {
    private Long schoolId;
    private String schoolName;
    private SchoolLevel schoolLevel;                            // 学校等级
    private AgentSchoolPopularityType schoolPopularityType;     // 名校，重点校，普通校
    private Boolean hasBd = false;                              // 是否有专员负责
    private Long bdId;                                          // 专员ID
    private String bdName;                                      // 专员姓名
    private String address;                                     //地址
    private int finHwGte3StuCount;                       // 学科本月月活
    private int lastSixMonthMaxFinHwGte3StuCount;           // 学科最高月活（近6个月）
    private String genDistance; // 距离当前坐标距离
}
