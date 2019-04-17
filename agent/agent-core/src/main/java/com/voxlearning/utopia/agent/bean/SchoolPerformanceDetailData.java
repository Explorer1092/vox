package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPermeabilityType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *
 *
 * @author song.wang
 * @date 2017/9/7
 */
@Getter
@Setter
public class SchoolPerformanceDetailData implements Serializable {
    private static final long serialVersionUID = 5330005742059077343L;

    private Long schoolId;
    private SchoolLevel schoolLevel;
    private AgentSchoolPermeabilityType permeability;            // 渗透类型

    private int stuScale;                                        // 学校规模

    private int regStuCount;                                     // 注册学生数
    private int authStuCount;                                    // 认证学生数
    private int tmIncRegStuCount;                                // 本月新增注册学生数
    private int tmIncAuthStuCount;                               // 本月新增认证学生数
    private int regStuCountDf;                                   // 昨日新增注册学生数
    private int authStuCountDf;                                  // 昨日新增认证学生数

    private int finSglSubjHwEq1UnAuStuCount;                     // =1 未认证学生当月完成1套任一科目作业学生数
    private int finSglSubjHwEq2UnAuStuCount;                     // =2 未认证学生当月完成2套任一科目作业学生数
    private int finSglSubjHwGte3UnAuStuCount;                    // ≥3，未认证学生当月完成3套及以上任一科目作业学生数

    private int finSglSubjHwEq1IncAuStuCount;                    // =1 当月认证学生当月完成1套任一科目作业学生数 （完成1套作业的新增认证学生数）  (对应大数据字段 finSglSubjHwEq1TmAuStuCount )
    private int finSglSubjHwEq2IncAuStuCount;                    // =2 当月认证学生当月完成2套任一科目作业学生数 （完成2套作业的新增认证学生数）  (对应大数据字段 finSglSubjHwEq2TmAuStuCount )
    private int finSglSubjHwEq1StBfAuStuCount;                   // =1 近6个月认证学生（不包含当月）当月完成1套任一科目作业学生数 （完成1套作业的短回认证学生数）  (对应大数据字段 finSglSubjHwEq1StAuStuCount )
    private int finSglSubjHwEq2StBfAuStuCount;                   // =2 近6个月认证学生（不包含当月）当月完成2套任一科目作业学生数 （完成2套作业的短回认证学生数）  (对应大数据字段 finSglSubjHwEq2StAuStuCount )
    private int finSglSubjHwEq1LtBfAuStuCount;                   // =1 6个月以前认证学生当月完成1套任一科目作业学生数 （完成1套作业的长回认证学生数）  (对应大数据字段 finSglSubjHwEq1LtAuStuCount )
    private int finSglSubjHwEq2LtBfAuStuCount;                   // =2 6个月以前认证学生当月完成2套任一科目作业学生数 （完成2套作业的长回认证学生数）  (对应大数据字段 finSglSubjHwEq2LtAuStuCount )

    private int finSglSubjHwGte3AuStuCount;                      // 认证学生当月完成3套及以上任一科目作业学生数
    private int finSglSubjHwGte3AuStuCountDf;                    // 认证学生当月完成3套及以上任一科目作业学生数（日浮）
    private int finSglSubjHwGte3IncAuStuCount;                   // 认证学生当月完成3套及以上任一科目作业学生数（新增）
    private int finSglSubjHwGte3IncAuStuCountDf;                 // 认证学生当月完成3套及以上任一科目作业学生数（新增日浮）
    private int finSglSubjHwGte3LtBfAuStuCount;                  // 认证学生当月完成3套及以上任一科目作业学生数（长回）
    private int finSglSubjHwGte3LtBfAuStuCountDf;                // 认证学生当月完成3套及以上任一科目作业学生数（长回日浮）
    private int finSglSubjHwGte3StBfAuStuCount;                  // 认证学生当月完成3套及以上任一科目作业学生数（短回）
    private int finSglSubjHwGte3StBfAuStuCountDf;                // 认证学生当月完成3套及以上任一科目作业学生数（短回日浮）


    //// 快乐学部分
    private int stuKlxTnCount;                                   // 考号数
    // 数学
    private int finLowMathAnshEq1StuCount;                       // 低标=1，数学扫描
    private int finLowMathAnshGte2StuCount;                      // 低标≥2，数学扫描
    private int finHighMathAnshEq1StuCount;                      // 高标=1，数学扫描
    private int finMathAnshGte2StuCount;                         // 当月作答2次及以上数学试卷学生数
    private int finMathAnshGte2StuCountDf;                       // 当月作答2次及以上数学试卷学生数（日浮）
    private int finMathAnshGte2IncStuCount;                      // 当月作答2次及以上数学试卷学生数（新增）
    private int finMathAnshGte2BfStuCount;                       // 当月作答2次及以上数学试卷学生数（回流）

    // 副科
    private int finLowDeputyAnshEq1StuCount;                     // 低标=1，副科扫描
    private int finLowDeputyAnshGte2StuCount;                    // 低标≥2，副科扫描
    private int finHighDeputyAnshEq1StuCount;                    // 高标=1，副科扫描
    private int finDeputyAnshGte2StuCount;                       // 当月作答2次及以上副科试卷学生数
    private int finDeputyAnshGte2StuCountDf;                     // 当月作答2次及以上副科试卷学生数（日浮）
    private int finDeputyAnshGte2IncStuCount;                    // 当月作答2次及以上副科试卷学生数（新增）
    private int finDeputyAnshGte2BfStuCount;                     // 当月作答2次及以上副科试卷学生数（回流）

    // 其他
    private int finLowOtherAnshEq1StuCount;                      // 低标=1，其他扫描
    private int finLowOtherAnshGte2StuCount;                     // 低标≥2，其他扫描
    private int finHighOtherAnshEq1StuCount;                     // 高标=1，其他扫描
    private int finOtherAnshGte2StuCount;                        // 当月作答2次及以上其他科目试卷学生数
    private int finOtherAnshGte2StuCountDf;                      // 当月作答2次及以上其他科目试卷学生数（日浮）
    private int finOtherAnshGte2IncStuCount;                     // 当月作答2次及以上其他科目试卷学生数（新增）
    private int finOtherAnshGte2BfStuCount;                      // 当月作答2次及以上其他科目试卷学生数（回流）

    // 全科
    private int finLowAllSubjAnshEq1StuCount;                    // 低标=1，全部扫描
    private int finLowAllSubjAnshGte2StuCount;                   // 低标≥2，全部扫描
    private int finHighAllSubjAnshEq1StuCount;                   // 高标=1，全部扫描
    private int finAllSubjAnshGte2StuCount;                      // 当月作答2次及以上全部科目试卷学生数
    private int finAllSubjAnshGte2StuCountDf;                    // 当月作答2次及以上全部科目试卷学生数（日浮）
    private int finAllSubjAnshGte2IncStuCount;                   // 当月作答2次及以上全部科目试卷学生数（新增）
    private int finAllSubjAnshGte2BfStuCount;                    // 当月作答2次及以上全部科目试卷学生数（回流）

    /** =1，所有学生当月完成1套任一科目作业学生数*/
    private int finSglSubjHwEq1StuCount;

    /** =2，所有学生当月完成2套任一科目作业学生数 */
    private int finSglSubjHwEq2StuCount;


    @Deprecated
    private int finEngHwGte3AuStuCount;                          // 认证学生当月完成3套及以上英语作业学生数
    @Deprecated
    private int finEngHwGte3AuStuCountDf;                        // 认证学生当月完成3套及以上英语作业学生数（日浮）

}
