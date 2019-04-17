package com.voxlearning.utopia.agent.view.grade;

import lombok.Getter;
import lombok.Setter;

/**
 * online模式下年级中班级的数据+
 *
 * @author song.wang
 * @date 2018/1/31
 */
@Getter
@Setter
public class GradeClass17InfoView {
    private Long classId;                                   // 班级ID
    private String className;                               // 班级名称
    private int regStuCount;                                // 注册学生数


    private boolean hasEngTeacher;                          // 班级是否有英语老师
    private boolean hasAuthEngTeacher;                      // 班级是否有认证英语老师
    private Long engTeacherId;                              // 英语老师ID
    private String engTeacherName;                          // 英语老师姓名
    private int tmEngHwSc;                                  // 本月布置英语作业套数
    private int tmFinEngHwGte3AuStuCount;                   // 英语月活

    private boolean hasMathTeacher;                         // 班级是否有数学老师
    private boolean hasAuthMathTeacher;                     // 班级是否有认证数学老师
    private Long mathTeacherId;                             // 数学老师ID
    private String mathTeacherName;                         // 数学老师姓名
    private int tmMathHwSc;                                 // 本月布置数学作业套数
    private int tmFinMathHwGte3AuStuCount;                  // 数学月活

    private boolean hasChnTeacher;                          // 班级是否有语文老师
    private boolean hasAuthChnTeacher;                      // 班级是否有认证语文老师
    private Long chnTeacherId;                              // 语文老师ID
    private String chnTeacherName;                          // 语文老师姓名
    private int tmChnHwSc;                                  // 本月布置语文作业套数
    private int tmFinChnHwGte3AuStuCount;                   // 语文月活




    private boolean engVacnHwFlag;                          // 是否布置英语假期作业
    private boolean mathVacnHwFlag;                         // 是否布置数学假期作业
    private boolean chnVacnHwFlag;                          // 是否布置语文假期作业

    private boolean engTermReviewFlag;                      // 是否布置英语期末作业
    private boolean mathTermReviewFlag;                     // 是否布置数学期末作业
    private boolean chnTermReviewFlag;                      // 是否布置语文期末作业

    private	int	bindParentStuNum;	                //绑定家长的学生数
    private	int parentStuActiveSettlementNum;	    //家长学生双活结算
}
