package com.voxlearning.utopia.agent.bean.performance.school;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 一起作业模式的学校业绩展示器
 *
 * @author chunlin.yu
 * @create 2018-02-01 14:52
 **/
@Setter
@Getter
public class School17PerformanceVO {

    /**
     * 学生规模;
     */
    private int stuScale;
    /**
     * 累计注册学生数;
     */
    private int regStuCount;
    /**
     * 累计认证学生数;
     */
    private int auStuCount;

    /**
     * 本月完成3套及以上任一科目作业认证学生数;   认证学生，≥3 ；也叫月活或者单活
     */
    private int finSglSubjHwGte3AuStuCount;

    /**
     * 学科和科目业绩映射
     */
    private Map<Subject,SubjectPerformance> subjectPerformanceMap;

    /**
     * 分科业绩数据
     *
     * @author chunlin.yu
     * @create 2018-02-01 15:07
     **/
    @Setter
    @Getter
    public class SubjectPerformance {
        /**
         * 科目名称
         */
        private String subjectName;

        /**
         * 渗透率
         */
        private Double permeability;

        /**
         * 上月渗透率
         */
        private Double previousPermeability;


        /**
         * 本月完成3套及以上当前科目作业认证学生数;   认证学生，≥3
         */
        private int finHwGte3AuStuCount;

        /**
         * 上月完成3套及以上当前科目作业认证学生数;   认证学生，≥3
         */
        private int previousFinHwGte3AuStuCount;

        /**
         * 本月完成3套及以上当前科目作业认证学生数（新增）;
         */
        private int finHwGte3IncAuStuCount;

        /**
         * 当前科目月活次月留存率;
         */
        private Double mrtRate;

        /**
         * 上月当前科目月活次月留存率;
         */
        private Double previousMrtRate;


        /**
         * 本月完成0套当前科目作业学生数;   所有学生，=0
         */
        private int finHwEq0StuCount;

        /**
         * 本月完成1套当前科目作业学生数;   所有学生，=1
         */
        private int finHwEq1StuCount;
        /**
         * 本月完成2套当前科目作业学生数;   所有学生，=2
         */
        private int finHwEq2StuCount;

        private Integer finHwGte3AuStuCountDf;//新增月活
    }

}
