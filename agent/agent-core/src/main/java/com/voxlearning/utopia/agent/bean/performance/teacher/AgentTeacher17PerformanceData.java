package com.voxlearning.utopia.agent.bean.performance.teacher;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 老师业绩数据封装
 *
 * @author chunlin.yu
 * @create 2018-02-26 17:01
 **/
@Getter
@Setter
public class AgentTeacher17PerformanceData implements Serializable{

    private static final long serialVersionUID = 1369550061593416470L;

    private Long teacherId;

    private Integer day;


    /**
     * 注册日期； ；
     */
    private Date regTime;
    /**
     * 认证日期； ；
     */
    private Date auTime;
    /**
     * 认证状态； ；
     */
    private int auState;
    /**
     * 带班数量； ；
     */
    private int clazzCount;
    /**
     * 注册学生数； ；
     */
    private int regStuCount;
    /**
     * 认证学生数； ；
     */
    private int auStuCount;
    /**
     * 本学期注册未认证学生数； 每学期重新设置起始日期，本学期起始日期：2017-09-01；
     */
    private int termRegUnAuStuCount;
    /**
     * 上次布置作业日期； ；
     */
    private Date latestHwTime;
    /**
     * 本月布置所有作业套数； ；
     */
    private int tmHwSc;
    /**
     * 本月布置指定作业套数； ；
     */
    private int tmTgtHwSc;
    /**
     * 本月布置所有作业班组最大套数； ；
     */
    private int tmGroupMaxHwSc;
    /**
     * 本月布置所有作业班组最小套数； ；
     */
    private int tmGroupMinHwSc;
    /**
     * 布置假期作业的班组数； ；
     */
    private int vacnHwGroupCount;

    private int termReviewGroupCount;   //布置期末作业的班组数
    /**
     * 本周布置所有作业套数； ；
     */
    private int twHwSc;
    /**
     * 本月完成1套当前科目作业学生数； 所有学生，=1；
     */
    private int finCsHwEq1StuCount;
    /**
     * 本月完成2套当前科目作业学生数； 所有学生，=2；
     */
    private int finCsHwEq2StuCount;
    /**
     * 本月完成3套及以上当前科目作业学生数； 所有学生，≥3；
     */
    private int finCsHwGte3StuCount;
    /**
     * 本月完成1套当前科目作业认证学生数； 认证学生，=1；
     */
    private int finCsHwEq1AuStuCount;
    /**
     * 本月完成2套当前科目作业认证学生数； 认证学生，=2；
     */
    private int finCsHwEq2AuStuCount;
    /**
     * 本月完成3套及以上当前科目作业认证学生数； 认证学生，≥3； 科目月活
     */
    private int finCsHwGte3AuStuCount;
    /**
     * 累计当前科目新增结算学生数； ；
     */
    private int csSettlementStuCount;
    /**
     * 上月布置作业套数
     */
    private int  imHwSc;
    private int pdFinCsHwGte3AuStuCount;                                             // 昨日完成3套及以上当前科目作业认证学生数
    private int tmFinCsHwGte3IncAuStuCount;                                          // 本月完成3套及以上当前科目作业认证学生数（新增）
    private int pdFinCsHwGte3IncAuStuCount;                                          // 昨日完成3套及以上当前科目作业认证学生数（新增）
    private int lmFinCsHwGte3AuStuCount;                                             // 上月当前科目月活
    private int tmFinCsHwEq1IncStuCount;                                             // 未被结算过当前科目新增的学生当月完成1套当前科目作业的学生数
    private int tmFinCsHwEq2IncStuCount;                                             // 未被结算过当前科目新增的学生当月完成2套当前科目作业的学生数
    private int tmFinCsHwGte1IncStuCount;                                            // 未被结算过当前科目新增的学生当月完成1套及以上当前科目作业的学生数
    private int pdFinCsHwGte1IncStuCount;                                            // 昨日未被结算过当前科目新增的学生完成1套及以上当前科目作业的学生数

    //TODO 这些指标数据目前只是把字段加到这里  还没有从大数据那边取值
    private int potentialInc1StuCount;//新增1套
    private int potentialInc2StuCount;//新增2套
    private int potentialBack1StuCount;//回流2套
    private int potentialBack2StuCount;//回流2套

}
