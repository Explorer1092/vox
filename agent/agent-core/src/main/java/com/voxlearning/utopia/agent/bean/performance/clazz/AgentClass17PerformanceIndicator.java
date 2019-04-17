package com.voxlearning.utopia.agent.bean.performance.clazz;

import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentClass17PerformanceIndicator
 *
 * @author song.wang
 * @date 2018/1/31
 */
@Getter
@Setter
public class AgentClass17PerformanceIndicator implements Serializable {

    private int regStuCount;                               // 累计注册学生数
    private int auStuCount;                               // 累计认证学生数
    private int termRegUnAuStuCount;                               // 本学期注册未认证学生数

    private int tmEngHwSc;                               // 本月布置英语作业套数
    private int tmMathHwSc;                               // 本月布置数学作业套数
    private int tmChnHwSc;                               // 本月布置语文作业套数
    private boolean engVacnHwFlag;                               // 是否布置英语假期作业
    private boolean mathVacnHwFlag;                               // 是否布置数学假期作业
    private boolean chnVacnHwFlag;                               // 是否布置语文假期作业

    private boolean engTermReviewFlag;  //是否布置英语期末作业
    private boolean mathTermReviewFlag; //是否布置数学期末作业
    private boolean chnTermReviewFlag;  //是否布置语文期末作业

    private int finSglSubjHwEq1StuCount;                               // 本月完成1套任一科目作业学生数
    private int finSglSubjHwEq2StuCount;                               // 本月完成2套任一科目作业学生数
    private int finSglSubjHwGte3StuCount;                               // 本月完成3套及以上任一科目作业学生数
    private int finSglSubjHwEq1AuStuCount;                               // 本月完成1套任一科目作业认证学生数
    private int finSglSubjHwEq2AuStuCount;                               // 本月完成2套任一科目作业认证学生数
    private int finSglSubjHwGte3AuStuCount;                               // 本月完成3套及以上任一科目作业认证学生数

    private int finEngHwEq1StuCount;                               // 本月完成1套英语作业学生数
    private int finEngHwEq2StuCount;                               // 本月完成2套英语作业学生数
    private int finEngHwGte3StuCount;                               // 本月完成3套及以上英语作业学生数
    private int finEngHwEq1AuStuCount;                               // 本月完成1套英语作业认证学生数
    private int finEngHwEq2AuStuCount;                               // 本月完成2套英语作业认证学生数
    private int finEngHwGte3AuStuCount;                               // 本月完成3套及以上英语作业认证学生数

    private int engSettlementStuCount;                               // 累计英语新增结算学生数


    private int finMathHwEq1StuCount;                               // 本月完成1套数学作业学生数
    private int finMathHwEq2StuCount;                               // 本月完成2套数学作业学生数
    private int finMathHwGte3StuCount;                               // 本月完成3套及以上数学作业学生数
    private int finMathHwEq1AuStuCount;                               // 本月完成1套数学作业认证学生数
    private int finMathHwEq2AuStuCount;                               // 本月完成2套数学作业认证学生数
    private int finMathHwGte3AuStuCount;                               // 本月完成3套及以上数学作业认证学生数

    private int mathSettlementStuCount;                               // 累计数学新增结算学生数


    private int finChnHwEq1StuCount;                               // 本月完成1套语文作业学生数
    private int finChnHwEq2StuCount;                               // 本月完成2套语文作业学生数
    private int finChnHwGte3StuCount;                               // 本月完成3套及以上语文作业学生数
    private int finChnHwEq1AuStuCount;                               // 本月完成1套语文作业认证学生数
    private int finChnHwEq2AuStuCount;                               // 本月完成2套语文作业认证学生数
    private int finChnHwGte3AuStuCount;                               // 本月完成3套及以上语文作业认证学生数

    private int chnSettlementStuCount;                                // 累计语文新增结算学生数


    private int tmRegStuCount;        //本月注册学生数
    private int pdRegStuCount;        //昨日注册学生数
    private int tmAuStuCount;        //本月认证学生数
    private int pdAuStuCount;        //昨日认证学生数
    private int pdFinEngHwGte3AuStuCount;        //昨日完成3套及以上英语作业认证学生数
    private int tmFinEngHwGte3IncAuStuCount;        //本月完成3套及以上英语作业认证学生数（新增）
    private int pdFinEngHwGte3IncAuStuCount;        //昨日完成3套及以上英语作业认证学生数（新增）
    private int lmFinEngHwGte3AuStuCount;        //上月英语月活
    private int tmFinEngHwEq1IncStuCount;        //未被结算过英语新增的学生当月完成1套英语作业的学生数
    private int tmFinEngHwEq2IncStuCount;        //未被结算过英语新增的学生当月完成2套英语作业的学生数
    private int tmFinEngHwGte1IncStuCount;        //未被结算过英语新增的学生当月完成1套及以上英语作业的学生数
    private int pdFinEngHwGte1IncStuCount;        //昨日未被结算过英语新增的学生完成1套及以上英语作业的学生数
    private int pdFinMathHwGte3AuStuCount;        //昨日完成3套及以上数学作业认证学生数
    private int tmFinMathHwGte3IncAuStuCount;        //本月完成3套及以上数学作业认证学生数（新增）
    private int pdFinMathHwGte3IncAuStuCount;        //昨日完成3套及以上数学作业认证学生数（新增）
    private int lmFinMathHwGte3AuStuCount;        //上月数学月活
    private int tmFinMathHwEq1IncStuCount;        //未被结算过数学新增的学生当月完成1套数学作业的学生数
    private int tmFinMathHwEq2IncStuCount;        //未被结算过数学新增的学生当月完成2套数学作业的学生数
    private int tmFinMathHwGte1IncStuCount;        //未被结算过数学新增的学生当月完成1套及以上数学作业的学生数
    private int pdFinMathHwGte1IncStuCount;        //昨日未被结算过数学新增的学生完成1套及以上数学作业的学生数
    private int pdFinChnHwGte3AuStuCount;        //昨日完成3套及以上语文作业认证学生数
    private int tmFinChnHwGte3IncAuStuCount;        //本月完成3套及以上语文作业认证学生数（新增）
    private int pdFinChnHwGte3IncAuStuCount;        //昨日完成3套及以上语文作业认证学生数（新增）
    private int lmFinChnHwGte3AuStuCount;        //上月语文月活
    private int tmFinChnHwEq1IncStuCount;        //未被结算过语文新增的学生当月完成1套语文作业的学生数
    private int tmFinChnHwEq2IncStuCount;        //未被结算过语文新增的学生当月完成2套语文作业的学生数
    private int tmFinChnHwGte1IncStuCount;        //未被结算过语文新增的学生当月完成1套及以上语文作业的学生数
    private int pdFinChnHwGte1IncStuCount;        //昨日未被结算过语文新增的学生完成1套及以上语文作业的学生数
    private int tmFinEngOralTestGte1StuCount;        //本月完成1次及以上英语口语测评学生数
    private int pdFinEngOralTestGte1StuCount;        //昨日完成1次及以上英语口语测评学生数




}
