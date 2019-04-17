package com.voxlearning.utopia.agent.bean.ranking;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author chunlin.yu
 * @create 2018-04-09 15:56
 **/

@Getter
@Setter
public class PerformanceRankingSumDataItem implements Serializable {
    private static final long serialVersionUID = 215369186436613849L;

    private int regStuCountDf;                                     //昨日注册学生数
    private int tmRegStuCount;                                     //本月注册学生数
    @Deprecated
    private int finMathHwGte3IncAuStuCountDf;                                     //昨日完成3套及以上数学作业认证学生数（新增）
    @Deprecated
    private int finMathHwGte3IncAuStuCount;                                     //本月完成3套及以上数学作业认证学生数（新增）
    private int pdFinMathHwGte1IncStuCount;                                     // 昨日数学新增 >= 1
    private int tmFinMathHwGte1IncStuCount;                                     // 本月数学新增 >= 1
    private int pdFinChnHwGte1IncStuCount;                                      // 昨日语文新增 >= 1
    private int tmFinChnHwGte1IncStuCount;                                      // 本月语文新增 >= 1
    private int pdFinEngHwGte1IncStuCount;                                      // 昨日英语新增 >= 1
    private int tmFinEngHwGte1IncStuCount;                                      // 本月英语新增 >= 1

    private int pdFinSglSubjHwGte1IncStuCount;                                      // 昨日单科新增 >= 1
    private int tmFinSglSubjHwGte1IncStuCount;                                      // 本月单科新增 >= 1

    @Deprecated
    private int pdFinTpGte1WithExamStuCount;
    @Deprecated
    private int tmFinTpGte1WithExamStuCount;

    private int pdRegEngTeaCount;//昨日英语老师注册榜
    private int tmRegEngTeaCount;//本月英语老师注册榜
    private int pdRegMathTeaCount;//昨日数学老师注册榜
    private int tmRegMathTeaCount;//本月数学老师注册榜

    private int pdRegSglSubjTeaCount;//昨日老师注册榜
    private int tmRegSglSubjTeaCount;//本月老师注册榜

    private int pdPromoteRegStuCount;//昨日小升初注册学生数
    private int tmPromoteRegStuCount;//本月小升初注册学生数

    private int pdBindStuParentNum;//昨日新增绑定家长榜
    private int tmBindStuParentNum;//本月新增绑定家长榜
    private int pdLoginGte1BindStuParentNum;//昨日家长活跃1次榜
    private int tmLoginGte1BindStuParentNum;//本月家长活跃1次榜
    private int pdParentStuActiveSettlementNum;//昨日学生家长双活榜
    private int tmParentStuActiveSettlementNum;//本月学生家长双活榜

    private int headCount;                                                    // 部门下的专员数量 idType = 1时有值
}
