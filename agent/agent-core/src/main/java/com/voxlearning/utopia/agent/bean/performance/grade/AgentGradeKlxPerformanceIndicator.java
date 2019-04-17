package com.voxlearning.utopia.agent.bean.performance.grade;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentGradeKlxPerformanceIndicator
 *
 * @author song.wang
 * @date 2018/3/15
 */
@Getter
@Setter
public class AgentGradeKlxPerformanceIndicator implements Serializable{
//    private int stuScale;                                             // 学生规模
    private int klxTnCount;                                             // 快乐学考号数

    private int tmFinTpEq1StuCount;                                             // 本月作答1次任意科目试卷学生数

    private int tmFinTpGte1StuCount;                                         //本月作答1次以上试卷学生数（普通扫描）	普通扫描≥1;满足低标，剔除大考；不分科（每个学生所有科目相加

    private int pdFinTpGte1StuCount;                                         //昨日作答1次以上试卷学生数（普通扫描）

    private int tmFinTpGte1WithExamStuCount;                                //本月作答1次以上试卷学生数（常规扫描）;满足低标即可;不分科（每个学生所有科目相加）

    private int pdFinTpGte1WithExamStuCount;                               //昨日作答1次以上试卷学生数（常规扫描）


    private int tmFinTpEq2StuCount;                                             // 本月作答2次任意科目试卷学生数
    private int tmFinTpGte3StuCount;                                             // 本月作答3次及以上任意科目试卷学生数
    private int pdFinTpGte3StuCount;                                             // 昨日作答3次及以上任意科目试卷学生数

    private int tmFinMathBgExamStuCount;                                             // 本月数学大考扫描学生数
    private int pdFinMathBgExamStuCount;                                             // 昨日数学大考扫描学生数
    private int tmFinMathTpGte1StuCount;                                             // 本月作答1次及以上数学试卷学生数
    private int pdFinMathTpGte1StuCount;                                             // 昨日作答1次及以上数学试卷学生数

    private int tmFinEngBgExamStuCount;                                             // 本月英语大考扫描学生数
    private int pdFinEngBgExamStuCount;                                             // 昨日英语大考扫描学生数
    private int tmFinEngTpGte1StuCount;                                             // 本月作答1次及以上英语试卷学生数
    private int pdFinEngTpGte1StuCount;                                             // 昨日作答1次及以上英语试卷学生数

    private int tmFinPhyBgExamStuCount;                                             // 本月物理大考扫描学生数
    private int pdFinPhyBgExamStuCount;                                             // 昨日物理大考扫描学生数
    private int tmFinPhyTpGte1StuCount;                                             // 本月作答1次及以上物理试卷学生数
    private int pdFinPhyTpGte1StuCount;                                             // 昨日作答1次及以上物理试卷学生数

    private int tmFinCheBgExamStuCount;                                             // 本月化学大考扫描学生数
    private int pdFinCheBgExamStuCount;                                             // 昨日化学大考扫描学生数
    private int tmFinCheTpGte1StuCount;                                             // 本月作答1次及以上化学试卷学生数
    private int pdFinCheTpGte1StuCount;                                             // 昨日作答1次及以上化学试卷学生数

    private int tmFinBiolBgExamStuCount;                                             // 本月生物大考扫描学生数
    private int pdFinBiolBgExamStuCount;                                             // 昨日生物大考扫描学生数
    private int tmFinBiolTpGte1StuCount;                                             // 本月作答1次及以上生物试卷学生数
    private int pdFinBiolTpGte1StuCount;                                             // 昨日作答1次及以上生物试卷学生数

    private int tmFinChnBgExamStuCount;                                             // 本月语文大考扫描学生数
    private int pdFinChnBgExamStuCount;                                             // 昨日语文大考扫描学生数
    private int tmFinChnTpGte1StuCount;                                             // 本月作答1次及以上语文试卷学生数
    private int pdFinChnTpGte1StuCount;                                             // 昨日作答1次及以上语文试卷学生数

    private int tmFinHistBgExamStuCount;                                             // 本月历史大考扫描学生数
    private int pdFinHistBgExamStuCount;                                             // 昨日历史大考扫描学生数
    private int tmFinHistTpGte1StuCount;                                             // 本月作答1次及以上历史试卷学生数
    private int pdFinHistTpGte1StuCount;                                             // 昨日作答1次及以上历史试卷学生数

    private int tmFinGeogBgExamStuCount;                                             // 本月地理大考扫描学生数
    private int pdFinGeogBgExamStuCount;                                             // 昨日地理大考扫描学生数
    private int tmFinGeogTpGte1StuCount;                                             // 本月作答1次及以上地理试卷学生数
    private int pdFinGeogTpGte1StuCount;                                             // 昨日作答1次及以上地理试卷学生数

    private int tmFinPolBgExamStuCount;                                             // 本月政治大考扫描学生数
    private int pdFinPolBgExamStuCount;                                             // 昨日政治大考扫描学生数
    private int tmFinPolTpGte1StuCount;                                             // 本月作答1次及以上政治试卷学生数
    private int pdFinPolTpGte1StuCount;                                             // 昨日作答1次及以上政治试卷学生数
}
