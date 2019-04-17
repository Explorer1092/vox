package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * SchoolAnalysisData
 *
 * @author song.wang
 * @date 2016/12/7
 */
@Getter
@Setter
public class SchoolAnalysisData {

    private Long schoolId;
    private String schoolName;
    private SchoolLevel schoolLevel; //学校等级
    private Boolean visitedFlag; // 本月是否拜访过
    private Double authRate; // 认证率
    private Double permeabilitySasc; // 单活渗透率
    private Double permeabilityDasc; // 双活渗透率

    private Integer preMonthSasc; // 上月单活
    private Integer currentSasc; // 当前单活
    private Integer potentialSasc;   // 单活可挖

    private Integer preMonthDasc; // 上月双活
    private Integer currentDasc; // 目前双活
    private Integer potentialDasc;  // 双活可挖

}
