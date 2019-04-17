package com.voxlearning.utopia.agent.bean.performance.school;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 17作业学校业绩字典
 *
 * @author chunlin.yu
 * @create 2018-01-31 11:15
 **/
@Getter
@Setter
public class AgentSchool17PerformanceData implements Serializable {

    private static final long serialVersionUID = 7839447028602686483L;

    private Integer day;
    private Long schoolId;

    private AgentSchool17PerformanceIndicator indicatorData;                      // 指标数据

//    /**
//     * 学生规模;
//     */
//    private int stuScale;
//    /**
//     * 累计注册学生数;
//     */
//    private int regStuCount;
//    /**
//     * 累计认证学生数;
//     */
//    private int auStuCount;
//    /**
//     * 本月注册学生数;
//     */
//    private int tmRegStuCount;
//    /**
//     * 本月认证学生数;
//     */
//    private int tmAuStuCount;
//    /**
//     * 昨日注册学生数;
//     */
//    private int regStuCountDf;
//    /**
//     * 昨日认证学生数;
//     */
//    private int auStuCountDf;
//    /**
//     * 本学期注册未认证学生数;
//     */
//    private int termRegUnAuStuCount;
//    /**
//     * 认证英语老师数;
//     */
//    private int auEngTeaCount;
//    /**
//     * 认证数学老师数;
//     */
//    private int auMathTeaCount;
//    /**
//     * 认证语文老师数;
//     */
//    private int auChnTeaCount;
//    /**
//     * 布置假期作业认证英语老师数;
//     */
//    private int assignVacnHwAuEngTeaCount;
//    /**
//     * 布置假期作业认证数学老师数;
//     */
//    private int assignVacnHwAuMathTeaCount;
//    /**
//     * 布置假期作业认证语文老师数;
//     */
//    private int assignVacnHwAuChnTeaCount;
//    /**
//     * 本月认证英语老师使用数;
//     */
//    private int tmAuEngTeaCount;
//    /**
//     * 本月认证数学老师使用数;
//     */
//    private int tmAuMathTeaCount;
//    /**
//     * 本月认证语文老师使用数;
//     */
//    private int tmAuChnTeaCount;
//    /**
//     * 本月完成1套任一科目作业学生数;   所有学生，=1
//     */
//    private int finSglSubjHwEq1StuCount;
//    /**
//     * 本月完成2套任一科目作业学生数;   所有学生，=2
//     */
//    private int finSglSubjHwEq2StuCount;
//    /**
//     * 本月完成3套及以上任一科目作业学生数;   所有学生，≥3
//     */
//    private int finSglSubjHwGte3StuCount;
//    /**
//     * 本月完成1套任一科目作业认证学生数;   认证学生，=1
//     */
//    private int finSglSubjHwEq1AuStuCount;
//    /**
//     * 本月完成2套任一科目作业认证学生数;   认证学生，=2
//     */
//    private int finSglSubjHwEq2AuStuCount;
//    /**
//     * 本月完成3套及以上任一科目作业认证学生数;   认证学生，≥3
//     */
//    private int finSglSubjHwGte3AuStuCount;
//    /**
//     * 本月完成3套及以上任一科目作业认证学生数（新增）;
//     */
//    private int finSglSubjHwGte3IncAuStuCount;
//    /**
//     * 本月完成3套及以上任一科目作业认证学生数（短回）;
//     */
//    private int finSglSubjHwGte3StBfAuStuCount;
//    /**
//     * 本月完成3套及以上任一科目作业认证学生数（长回）;
//     */
//    private int finSglSubjHwGte3LtBfAuStuCount;
//    /**
//     * 昨日完成3套及以上任一科目作业认证学生数;
//     */
//    private int finSglSubjHwGte3AuStuCountDf;
//    /**
//     * 昨日完成3套及以上任一科目作业认证学生数（新增）;
//     */
//    private int finSglSubjHwGte3IncAuStuCountDf;
//    /**
//     * 昨日完成3套及以上任一科目作业认证学生数（短回）;
//     */
//    private int finSglSubjHwGte3StBfAuStuCountDf;
//    /**
//     * 昨日完成3套及以上任一科目作业认证学生数（长回）;
//     */
//    private int finSglSubjHwGte3LtBfAuStuCountDf;
//
//    /**
//     * 上月单科月活
//     */
//    private int lmFinSglSubjHwGte3AuStuCount;
//
//    /**
//     * 上月单科月活留存数
//     */
//    private int sglSubjMrtStuCount;
//
//    /**
//     * 单科月活次月留存率;
//     */
//    @Deprecated
//    private double sglSubjMrtRate;
//    /**
//     * 本月完成1套英语作业学生数;   所有学生，=1
//     */
//    private int finEngHwEq1StuCount;
//    /**
//     * 本月完成2套英语作业学生数;   所有学生，=2
//     */
//    private int finEngHwEq2StuCount;
//    /**
//     * 本月完成3套及以上英语作业学生数;   所有学生，≥3
//     */
//    private int finEngHwGte3StuCount;
//    /**
//     * 本月完成1套英语作业认证学生数;   认证学生，=1
//     */
//    private int finEngHwEq1AuStuCount;
//    /**
//     * 本月完成2套英语作业认证学生数;   认证学生，=2
//     */
//    private int finEngHwEq2AuStuCount;
//    /**
//     * 本月完成3套及以上英语作业认证学生数;   认证学生，≥3
//     */
//    private int finEngHwGte3AuStuCount;
//    /**
//     * 本月完成3套及以上英语作业认证学生数（新增）;
//     */
//    private int finEngHwGte3IncAuStuCount;
//    /**
//     * 本月完成3套及以上英语作业认证学生数（短回）;
//     */
//    private int finEngHwGte3StBfAuStuCount;
//    /**
//     * 本月完成3套及以上英语作业认证学生数（长回）;
//     */
//    private int finEngHwGte3LtBfAuStuCount;
//    /**
//     * 昨日完成3套及以上英语作业认证学生数;
//     */
//    private int finEngHwGte3AuStuCountDf;
//    /**
//     * 昨日完成3套及以上英语作业认证学生数（新增）;
//     */
//    private int finEngHwGte3IncAuStuCountDf;
//    /**
//     * 昨日完成3套及以上英语作业认证学生数（短回）;
//     */
//    private int finEngHwGte3StBfAuStuCountDf;
//    /**
//     * 昨日完成3套及以上英语作业认证学生数（长回）;
//     */
//    private int finEngHwGte3LtBfAuStuCountDf;
//
//    /**
//     * 上月英语月活
//     */
//    private int lmFinEngHwGte3AuStuCount;
//
//    /**
//     * 上月英语月活留存数
//     */
//    private int engMrtStuCount;
//
//    /**
//     * 英语月活次月留存率;
//     */
//    @Deprecated
//    private double engMrtRate;
//
//    /**
//     * 累计英语新增结算学生数
//     */
//    private int engSettlementStuCount;
//
//    /**
//     * 本月完成1套数学作业学生数;   所有学生，=1
//     */
//    private int finMathHwEq1StuCount;
//    /**
//     * 本月完成2套数学作业学生数;   所有学生，=2
//     */
//    private int finMathHwEq2StuCount;
//    /**
//     * 本月完成3套及以上数学作业学生数;   所有学生，≥3
//     */
//    private int finMathHwGte3StuCount;
//    /**
//     * 本月完成1套数学作业认证学生数;   认证学生，=1
//     */
//    private int finMathHwEq1AuStuCount;
//    /**
//     * 本月完成2套数学作业认证学生数;   认证学生，=2
//     */
//    private int finMathHwEq2AuStuCount;
//    /**
//     * 本月完成3套及以上数学作业认证学生数;   认证学生，≥3
//     */
//    private int finMathHwGte3AuStuCount;
//    /**
//     * 本月完成3套及以上数学作业认证学生数（新增）;
//     */
//    private int finMathHwGte3IncAuStuCount;
//    /**
//     * 本月完成3套及以上数学作业认证学生数（短回）;
//     */
//    private int finMathHwGte3StBfAuStuCount;
//    /**
//     * 本月完成3套及以上数学作业认证学生数（长回）;
//     */
//    private int finMathHwGte3LtBfAuStuCount;
//    /**
//     * 昨日完成3套及以上数学作业认证学生数;
//     */
//    private int finMathHwGte3AuStuCountDf;
//    /**
//     * 昨日完成3套及以上数学作业认证学生数（新增）;
//     */
//    private int finMathHwGte3IncAuStuCountDf;
//    /**
//     * 昨日完成3套及以上数学作业认证学生数（短回）;
//     */
//    private int finMathHwGte3StBfAuStuCountDf;
//    /**
//     * 昨日完成3套及以上数学作业认证学生数（长回）;
//     */
//    private int finMathHwGte3LtBfAuStuCountDf;
//
//    /**
//     * 上月数学月活
//     */
//    private int lmFinMathHwGte3AuStuCount;
//
//    /**
//     * 上月数学月活留存数
//     */
//    private int mathMrtStuCount;
//
//    /**
//     * 数学月活次月留存率;
//     */
//    @Deprecated
//    private double mathMrtRate;
//
//    /**
//     * 累计数学新增结算学生数
//     */
//    private int mathSettlementStuCount;
//
//    /**
//     * 本月完成1套语文作业学生数;   所有学生，=1
//     */
//    private int finChnHwEq1StuCount;
//    /**
//     * 本月完成2套语文作业学生数;   所有学生，=2
//     */
//    private int finChnHwEq2StuCount;
//    /**
//     * 本月完成3套及以上语文作业学生数;   所有学生，≥3
//     */
//    private int finChnHwGte3StuCount;
//    /**
//     * 本月完成1套语文作业认证学生数;   认证学生，=1
//     */
//    private int finChnHwEq1AuStuCount;
//    /**
//     * 本月完成2套语文作业认证学生数;   认证学生，=2
//     */
//    private int finChnHwEq2AuStuCount;
//    /**
//     * 本月完成3套及以上语文作业认证学生数;   认证学生，≥3
//     */
//    private int finChnHwGte3AuStuCount;
//    /**
//     * 本月完成3套及以上语文作业认证学生数（新增）;
//     */
//    private int finChnHwGte3IncAuStuCount;
//    /**
//     * 本月完成3套及以上语文作业认证学生数（短回）;
//     */
//    private int finChnHwGte3StBfAuStuCount;
//    /**
//     * 本月完成3套及以上语文作业认证学生数（长回）;
//     */
//    private int finChnHwGte3LtBfAuStuCount;
//    /**
//     * 昨日完成3套及以上语文作业认证学生数;
//     */
//    private int finChnHwGte3AuStuCountDf;
//    /**
//     * 昨日完成3套及以上语文作业认证学生数（新增）;
//     */
//    private int finChnHwGte3IncAuStuCountDf;
//    /**
//     * 昨日完成3套及以上语文作业认证学生数（短回）;
//     */
//    private int finChnHwGte3StBfAuStuCountDf;
//    /**
//     * 昨日完成3套及以上语文作业认证学生数（长回）;
//     */
//    private int finChnHwGte3LtBfAuStuCountDf;
//
//    /**
//     * 上月语文月活
//     */
//    private int lmFinChnHwGte3AuStuCount;
//
//    /**
//     * 上月语文月活留存数
//     */
//    private int chnMrtStuCount;
//
//    /**
//     * 语文月活次月留存率;
//     */
//    @Deprecated
//    private double chnMrtRate;
//
//    /**
//     * 累计语文新增结算学生数
//     */
//    private int chnSettlementStuCount;
//
//    /**
//     * 上次拜访日期;
//     */
//    private Date latestVisitTime;
//    /**
//     * 本月完成1次及以上英语口语测评学生数;
//     */
//    private int finEngOralTestGte1StuCount;
//    /**
//     * 本周完成1套及以上任意科目作业的认证学生数; 单科周活
//     */
//    private int twFinSglSubjHwGte1AuStuCount;
//    /**
//     * 本周完成1套及以上英语科目作业的认证学生数; 英语周活
//     */
//    private int twFinEngHwGte1AuStuCount;
//    /**
//     * 本周完成1套及以上数学科目作业的认证学生数; 数学周活
//     */
//    private int twFinMathHwGte1AuStuCount;
//    /**
//     * 本周完成1套及以上语文科目作业的认证学生数; 语文周活
//     */
//    private int twFinChnHwGte1AuStuCount;


}
