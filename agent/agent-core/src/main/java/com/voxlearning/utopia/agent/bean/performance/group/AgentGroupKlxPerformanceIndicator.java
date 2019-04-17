package com.voxlearning.utopia.agent.bean.performance.group;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentGroupKlxPerformanceIndicator
 *
 * @author song.wang
 * @date 2018/3/15
 */
@Getter
@Setter
public class AgentGroupKlxPerformanceIndicator implements Serializable {
    private int klxTnCount;                                             // 快乐学考号数
    private int tmScanTpCount;                                             // 本月扫描试卷数

    private int tmFinCsTpEq1StuCount;                                             // 本月作答1次当前科目试卷学生数

    private int tmFinCsTpGte1StuCount;                           //本月作答1次及以上当前科目试卷学生数（普通扫描）	普通扫描≥1;满足低标，剔除大考；

    private int pdFinCsTpGte1StuCount;//昨日作答1次及以上当前科目试卷学生数（普通扫描)

    private int tmFinCsTpGte1WithExamStuCount; //本月作答1次及以上当前科目试卷学生数（常规扫描）	常规扫描≥1;满足低标即可

    private int pdFinCsTpGte1WithExamStuCount;//昨日作答1次及以上当前科目试卷学生数（常规扫描）


    private int tmFinCsTpEq2StuCount;                                             // 本月作答2次当前科目试卷学生数
    private int tmFinCsTpGte3StuCount;                                             // 本月作答3次及以上当前科目试卷学生数
    private int pdFinCsTpGte3StuCount;                                             // 昨日作答3次及以上当前科目试卷学生数
}

