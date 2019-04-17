package com.voxlearning.utopia.agent.bean.performance.school;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 17作业考核指标
 *
 * @author song.wang
 * @date 2017/12/11
 */
@Getter
@Setter
public class AgentSchool17PerformanceIndicator  implements Serializable {

    private int stuScale;                                     //学生规模
    private int regStuCount;                                     //累计注册学生数
    private int auStuCount;                                     //累计认证学生数

    private int tmRegStuCount;                                     //本月注册学生数
    private int tmAuStuCount;                                     //本月认证学生数
    private int regStuCountDf;                                     //昨日注册学生数
    private int auStuCountDf;                                     //昨日认证学生数

    private int termRegUnAuStuCount;                                     //本学期注册未认证学生数

    private int auEngTeaCount;                                     //认证英语老师数
    private int auMathTeaCount;                                     //认证数学老师数
    private int auChnTeaCount;                                     //认证语文老师数

    private int assignVacnHwAuEngTeaCount;                                     //布置假期作业认证英语老师数
    private int assignVacnHwAuMathTeaCount;                                     //布置假期作业认证数学老师数
    private int assignVacnHwAuChnTeaCount;                                     //布置假期作业认证语文老师数

    private int tmAuEngTeaCount;                                     //本月认证英语老师使用数
    private int tmAuMathTeaCount;                                     //本月认证数学老师使用数
    private int tmAuChnTeaCount;                                     //本月认证语文老师使用数

    private int tmLoginStuCount;                                     //MAU

    private int finSglSubjHwEq1StuCount;                                     //本月完成1套任一科目作业学生数
    private int finSglSubjHwEq2StuCount;                                     //本月完成2套任一科目作业学生数
    private int finSglSubjHwGte3StuCount;                                     //本月完成3套及以上任一科目作业学生数
    private int finSglSubjHwEq1AuStuCount;                                     //本月完成1套任一科目作业认证学生数
    private int finSglSubjHwEq2AuStuCount;                                     //本月完成2套任一科目作业认证学生数
    private int finSglSubjHwGte3AuStuCount;                                     //本月完成3套及以上任一科目作业认证学生数
    private int finSglSubjHwGte3IncAuStuCount;                                     //本月完成3套及以上任一科目作业认证学生数（新增）
    private int finSglSubjHwGte3StBfAuStuCount;                                     //本月完成3套及以上任一科目作业认证学生数（短回）
    private int finSglSubjHwGte3LtBfAuStuCount;                                     //本月完成3套及以上任一科目作业认证学生数（长回）
    private int finSglSubjHwGte3AuStuCountDf;                                     //昨日完成3套及以上任一科目作业认证学生数
    private int finSglSubjHwGte3IncAuStuCountDf;                                     //昨日完成3套及以上任一科目作业认证学生数（新增）
    private int finSglSubjHwGte3StBfAuStuCountDf;                                     //昨日完成3套及以上任一科目作业认证学生数（短回）
    private int finSglSubjHwGte3LtBfAuStuCountDf;                                     //昨日完成3套及以上任一科目作业认证学生数（长回）
    private int lmFinSglSubjHwGte3AuStuCount;                                     // 上月单科月活
    private int sglSubjMrtStuCount;                                                // 上月单科月活留存数
//    private Double sglSubjMrtRate;                                     //单科月活次月留存率

    private int finEngHwEq1StuCount;                                     //本月完成1套英语作业学生数
    private int finEngHwEq2StuCount;                                     //本月完成2套英语作业学生数
    private int finEngHwGte3StuCount;                                     //本月完成3套及以上英语作业学生数
    private int finEngHwEq1AuStuCount;                                     //本月完成1套英语作业认证学生数
    private int finEngHwEq2AuStuCount;                                     //本月完成2套英语作业认证学生数
    private int finEngHwGte3AuStuCount;                                     //本月完成3套及以上英语作业认证学生数
    private int finEngHwGte3IncAuStuCount;                                     //本月完成3套及以上英语作业认证学生数（新增）
    private int finEngHwGte3StBfAuStuCount;                                     //本月完成3套及以上英语作业认证学生数（短回）
    private int finEngHwGte3LtBfAuStuCount;                                     //本月完成3套及以上英语作业认证学生数（长回）
    private int finEngHwGte3AuStuCountDf;                                     //昨日完成3套及以上英语作业认证学生数
    private int finEngHwGte3IncAuStuCountDf;                                     //昨日完成3套及以上英语作业认证学生数（新增）
    private int finEngHwGte3StBfAuStuCountDf;                                     //昨日完成3套及以上英语作业认证学生数（短回）
    private int finEngHwGte3LtBfAuStuCountDf;                                     //昨日完成3套及以上英语作业认证学生数（长回）
    private int lmFinEngHwGte3AuStuCount;                                      // 上月英语月活
    private int engMrtStuCount;                                                // 上月英语月活留存数
//    private Double engMrtRate;                                     //英语月活次月留存率
    private int engSettlementStuCount;                                     //累计英语新增结算学生数

    private int finMathHwEq1StuCount;                                     //本月完成1套数学作业学生数
    private int finMathHwEq2StuCount;                                     //本月完成2套数学作业学生数
    private int finMathHwGte3StuCount;                                     //本月完成3套及以上数学作业学生数
    private int finMathHwEq1AuStuCount;                                     //本月完成1套数学作业认证学生数
    private int finMathHwEq2AuStuCount;                                     //本月完成2套数学作业认证学生数
    private int finMathHwGte3AuStuCount;                                     //本月完成3套及以上数学作业认证学生数
    private int finMathHwGte3IncAuStuCount;                                     //本月完成3套及以上数学作业认证学生数（新增）
    private int finMathHwGte3StBfAuStuCount;                                     //本月完成3套及以上数学作业认证学生数（短回）
    private int finMathHwGte3LtBfAuStuCount;                                     //本月完成3套及以上数学作业认证学生数（长回）
    private int finMathHwGte3AuStuCountDf;                                     //昨日完成3套及以上数学作业认证学生数
    private int finMathHwGte3IncAuStuCountDf;                                     //昨日完成3套及以上数学作业认证学生数（新增）
    private int finMathHwGte3StBfAuStuCountDf;                                     //昨日完成3套及以上数学作业认证学生数（短回）
    private int finMathHwGte3LtBfAuStuCountDf;                                     //昨日完成3套及以上数学作业认证学生数（长回）
    private int lmFinMathHwGte3AuStuCount;                                    // 上月数学月活
    private int mathMrtStuCount;                                              // 上月数学月活留存数
//    private Double mathMrtRate;                                     //数学月活次月留存率
    private int mathSettlementStuCount;                                     //累计数学新增结算学生数

    private int finChnHwEq1StuCount;                                     //本月完成1套语文作业学生数
    private int finChnHwEq2StuCount;                                     //本月完成2套语文作业学生数
    private int finChnHwGte3StuCount;                                     //本月完成3套及以上语文作业学生数
    private int finChnHwEq1AuStuCount;                                     //本月完成1套语文作业认证学生数
    private int finChnHwEq2AuStuCount;                                     //本月完成2套语文作业认证学生数
    private int finChnHwGte3AuStuCount;                                     //本月完成3套及以上语文作业认证学生数
    private int finChnHwGte3IncAuStuCount;                                     //本月完成3套及以上语文作业认证学生数（新增）
    private int finChnHwGte3StBfAuStuCount;                                     //本月完成3套及以上语文作业认证学生数（短回）
    private int finChnHwGte3LtBfAuStuCount;                                     //本月完成3套及以上语文作业认证学生数（长回）
    private int finChnHwGte3AuStuCountDf;                                     //昨日完成3套及以上语文作业认证学生数
    private int finChnHwGte3IncAuStuCountDf;                                     //昨日完成3套及以上语文作业认证学生数（新增）
    private int finChnHwGte3StBfAuStuCountDf;                                     //昨日完成3套及以上语文作业认证学生数（短回）
    private int finChnHwGte3LtBfAuStuCountDf;                                     //昨日完成3套及以上语文作业认证学生数（长回）
    private int lmFinChnHwGte3AuStuCount;                                        // 上月语文月活
    private int chnMrtStuCount;                                                  // 上月语文月活留存数
//    private Double chnMrtRate;                                     //语文月活次月留存率
    private int chnSettlementStuCount;                                     //累计语文新增结算学生数

    private Date latestVisitTime;                                     //上次拜访日期

    private int twFinSglSubjHwGte1AuStuCount;                                     //本周完成1套及以上任意科目作业的认证学生数
    private int twFinEngHwGte1AuStuCount;                                     //本周完成1套及以上英语科目作业的认证学生数
    private int twFinMathHwGte1AuStuCount;                                     //本周完成1套及以上数学科目作业的认证学生数
    private int twFinChnHwGte1AuStuCount;                                     //本周完成1套及以上语文科目作业的认证学生数

    private int finEngOralTestGte1StuCount;                                     //本月完成1次及以上英语口语测评学生数

    private int tmFinMathHwEq1IncStuCount;                                      // 本月数学新增1套
    private int tmFinMathHwEq2IncStuCount;                                      // 本月数学新增2套
    private int tmFinMathHwGte1IncStuCount;                                     // 本月数学新增 >= 1
    private int pdFinMathHwGte1IncStuCount;                                     // 昨日数学新增 >= 1
    private int tmAssignFirstHwMathTeaCount;                                    // 本月第一次布置作业的数学老师数

    private int tmFinEngHwEq1IncStuCount;                                       // 本月英语新增1套
    private int tmFinEngHwEq2IncStuCount;                                       // 本月英语新增2套
    private int tmFinEngHwGte1IncStuCount;                                      // 本月英语新增 >= 1
    private int pdFinEngHwGte1IncStuCount;                                      // 昨日英语新增 >= 1
    private int tmAssignFirstHwEngTeaCount;                                     // 本月第一次布置作业的英语老师数

    private int tmFinChnHwEq1IncStuCount;                                       // 本月语文新增1套
    private int tmFinChnHwEq2IncStuCount;                                       // 本月语文新增2套
    private int tmFinChnHwGte1IncStuCount;                                      // 本月语文新增 >= 1
    private int pdFinChnHwGte1IncStuCount;                                      // 昨日语文新增 >= 1
    private int tmAssignFirstHwChnTeaCount;                                     // 本月第一次布置作业的语文老师数

    private int pdFinEngOralTestGte1StuCount;                                   // 昨日完成1次及以上英语口语测评学生数


}
