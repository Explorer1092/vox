package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 班组业绩封装
 *
 * @author chunlin.yu
 * @create 2017-11-07 11:07
 **/
@Getter
@Setter
public class GroupPerformanceDetail implements Serializable{

    private static final long serialVersionUID = -5259868789579479755L;

    /**
     * 班组ID
     */
    private Long groupId;

    /**
     * ≥3，未认证学生当月完成3套及以上当前科目作业学生数
     */
    private int finCsHwGte3UnAuStuCount;

    /**
     * 未认证学生当月完成1套当前科目作业学生数；且当月未达成单科月活学生
     */
    private int finCsHwEq1UnAuStuCount;

    /**
     * 未认证学生当月完成2套当前科目作业学生数；且当月未达成单科月活学生
     */
    private int finCsHwEq2UnAuStuCount;

    /**
     * 当月认证学生当月完成1套当前科目作业学生数；且当月未达成单科月活学生
     */
    private int finCsHwEq1IncAuStuCount;

    /**
     * 当月认证学生当月完成2套当前科目作业学生数；且当月未达成单科月活学生
     */
    private int finCsHwEq2IncAuStuCount;

    /**
     * 近6个月认证学生（不包含当月）当月完成1套当前科目作业学生数；且当月未达成单科月活学生
     */
    private int finCsHwEq1StBfAuStuCount;

    /**
     * 近6个月认证学生（不包含当月）当月完成2套当前科目作业学生数；且当月未达成单科月活学生
     */
    private int finCsHwEq2StBfAuStuCount;

    /**
     * 6个月以前认证学生当月完成1套当前科目作业学生数；且当月未达成单科月活学生
     */
    private int finCsHwEq1LtBfAuStuCount;

    /**
     * 6个月以前认证学生当月完成2套当前科目作业学生数；且当月未达成单科月活学生
     */
    private int finCsHwEq2LtBfAuStuCount;

    /**
     *  低标=1 (当前科目)
     */
    private int finLowCsAnshEq1StuCount;

    /**
     * 低标≥2 (当前科目)
     */
    private int finLowCsAnshGte2StuCount;

    /**
     * 高标=1 (当前科目)
     */
    private int finHighCsAnshEq1StuCount;

    /**
     * 高标≥2 (当前科目)
     */
    private int finHighCsAnshGte2StuCount;

    /**
     * 当前班组所有学生当月完成1套当前科目作业学生数；=1，不限制当月是否达成了单科月活（20171130新增）
     */
    private int finCsHwEq1StuCount;

    /**
     * 当前班组所有学生当月完成2套当前科目作业学生数；=2，不限制当月是否达成了单科月活（20171130新增）
     */
    private int finCsHwEq2StuCount;
}
