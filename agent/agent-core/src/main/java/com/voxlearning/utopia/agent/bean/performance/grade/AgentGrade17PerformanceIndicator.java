package com.voxlearning.utopia.agent.bean.performance.grade;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentGrade17PerformanceIndicator
 *
 * @author song.wang
 * @date 2018/1/31
 */

@Getter
@Setter
public class AgentGrade17PerformanceIndicator implements Serializable {
//    private int stuScale;                                 // 学生规模

    private int regStuCount;                                 // 累计注册学生数
    private int auStuCount;                                 // 累计认证学生数
    private int termRegUnAuStuCount;                                 // 本学期注册未认证学生数

    private int finSglSubjHwEq1StuCount;                                 // 本月完成1套任一科目作业学生数
    private int finSglSubjHwEq2StuCount;                                 // 本月完成2套任一科目作业学生数
    private int finSglSubjHwGte3StuCount;                                 // 本月完成3套及以上任一科目作业学生数
    private int finSglSubjHwEq1AuStuCount;                                 // 本月完成1套任一科目作业认证学生数
    private int finSglSubjHwEq2AuStuCount;                                 // 本月完成2套任一科目作业认证学生数
    private int finSglSubjHwGte3AuStuCount;                                 // 本月完成3套及以上任一科目作业认证学生数

    private int finEngHwEq1StuCount;                                 // 本月完成1套英语作业学生数
    private int finEngHwEq2StuCount;                                 // 本月完成2套英语作业学生数
    private int finEngHwGte3StuCount;                                 // 本月完成3套及以上英语作业学生数
    private int finEngHwEq1AuStuCount;                                 // 本月完成1套英语作业认证学生数
    private int finEngHwEq2AuStuCount;                                 // 本月完成2套英语作业认证学生数
    private int finEngHwGte3AuStuCount;                                 // 本月完成3套及以上英语作业认证学生数

    private int engSettlementStuCount;                                 // 累计英语新增结算学生数

    private int finMathHwEq1StuCount;                                 // 本月完成1套数学作业学生数
    private int finMathHwEq2StuCount;                                 // 本月完成2套数学作业学生数
    private int finMathHwGte3StuCount;                                 // 本月完成3套及以上数学作业学生数
    private int finMathHwEq1AuStuCount;                                 // 本月完成1套数学作业认证学生数
    private int finMathHwEq2AuStuCount;                                 // 本月完成2套数学作业认证学生数
    private int finMathHwGte3AuStuCount;                                 // 本月完成3套及以上数学作业认证学生数

    private int mathSettlementStuCount;                                  // 累计数学新增结算学生数


    private int finChnHwEq1StuCount;                                 // 本月完成1套语文作业学生数
    private int finChnHwEq2StuCount;                                 // 本月完成2套语文作业学生数
    private int finChnHwGte3StuCount;                                 // 本月完成3套及以上语文作业学生数
    private int finChnHwEq1AuStuCount;                                 // 本月完成1套语文作业认证学生数
    private int finChnHwEq2AuStuCount;                                 // 本月完成2套语文作业认证学生数
    private int finChnHwGte3AuStuCount;                                 // 本月完成3套及以上语文作业认证学生数

    private int chnSettlementStuCount;                                  // 累计语文新增结算学生数

}
