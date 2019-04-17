package com.voxlearning.utopia.agent.bean.performance.group;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * AgentGroup17PerformanceIndicator
 *
 * @author song.wang
 * @date 2018/1/31
 */
@Getter
@Setter
public class AgentGroup17PerformanceIndicator implements Serializable {

    private int regStuCount;                                      // 注册学生数
    private int auStuCount;                                      // 认证学生数
    private int termRegUnAuStuCount;                                      // 本学期注册未认证学生数

    private Date latestHwTime;                                      // 上次布置作业日期
    private int tmHwSc;                                      // 本月布置所有作业套数
    private int tmTgtHwSc;                                      // 本月布置指定作业套数
    private boolean vacnHwFlag;                                         // 是否已布置寒假作业
    private boolean termReviewFlag;                                     // 是否已布置期末作业

    private int finCsHwEq1StuCount;                                      // 本月完成1套当前科目作业学生数
    private int finCsHwEq2StuCount;                                      // 本月完成2套当前科目作业学生数
    private int finCsHwGte3StuCount;                                      // 本月完成3套及以上当前科目作业学生数
    private int finCsHwEq1AuStuCount;                                      // 本月完成1套当前科目作业认证学生数
    private int finCsHwEq2AuStuCount;                                      // 本月完成2套当前科目作业认证学生数
    private int finCsHwGte3AuStuCount;                                      // 本月完成3套及以上当前科目作业认证学生数

    private int csSettlementStuCount;                                      // 累计当前科目新增结算学生数

}
