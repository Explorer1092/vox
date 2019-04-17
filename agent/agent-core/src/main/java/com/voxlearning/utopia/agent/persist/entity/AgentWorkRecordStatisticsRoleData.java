package com.voxlearning.utopia.agent.persist.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentWorkRecordStatisticsRoleData
 *
 * @author song.wang
 * @date 2018/6/5
 */
@Getter
@Setter
public class AgentWorkRecordStatisticsRoleData implements Serializable{

    private static final long serialVersionUID = 1294297924134129482L;

    private Integer userCount;                     // 用户数量
    private Integer fillRecordUserCount;           // 填写工作记录的用户数量
    private Integer recordUnreachedUserCount;        // 填写工作记录未达标的用户数（当天未录入， 3天未录入， 5天未录入）
    private Double perCapitaWorkload;              // 人均工作量
    private Double perCapitaWorkDayNum;            // 人均工作天数
    private Double perCapitaIntoSchoolNum;         // 人均进校数
    private Double perCapitaAccompanyVisitNum;     // 人均陪访数
    private Double perCapitalVisitWorkload;     // 人均陪访工作量
    private Double perCapitaVisitResearcherNum;    // 人均资源拓维数
    private Double perCapitaMeetingNum;            // 人均组会数
    private Double perCapitaVisitTeaNum;           // 人均见师数
    private Double perCapitaVisitChiTeaNum;        // 人均见师数（语文）
    private Double perCapitaVisitMathTeaNum;       // 人均见师数（数学）
    private Double perCapitaVisitEngTeaNum;        // 人均见师数（英语）
}
