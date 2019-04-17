package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 老师下的班级情况
 * @author zangtao
 */
@Getter
@Setter
public class AgentTeacherIntoClazzInfo implements Serializable {
    private Integer gradeLevel;     //年级
    private String clazzName;//班级名称
    private Long gid;//班组id   groupId
    private Long cid ;// 班级id  classId
    private String clazzFullName;//班级名称（全称 三年级四班）
    private Subject subject;//科目
    private Integer regNum;//注册数
    private Integer authNum;//认证
    private Integer finEq1Hw;//一套
    private Integer finEq2Hw;//两套
    private Integer auStuCsMauc;//月活
    private Integer monthHwCount ;// 本月布置所有作业数
    private Integer monthValidHwCount; // 本月布置指定作业数
    private Integer lmHwCount;// 上月布置所有作业数

    private Integer klxTnCount; // 快乐学考号数
    private Integer tmFinCsTpGte1StuCount;   // 普通扫描（>=1）
    private Integer tmFinCsTpGte3StuCount;   // 普通扫描（>=3）

//    private Integer finEq1TpCount; // 1套试卷扫描数
//    private Integer tmCsAnshEq2StuCount; // 扫描数（业绩）

    private Boolean isAssignSHW = false;     // 是否布置假期作业

    private int finLowCsAnshEq1StuCount; //低标=1 (当前科目)
    private int finLowCsAnshGte2StuCount; //低标≥2 (当前科目)
    private int finHighCsAnshEq1StuCount; //高标=1 (当前科目)
    private int finHighCsAnshGte2StuCount; //高标≥2 (当前科目)

}
