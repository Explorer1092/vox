package com.voxlearning.utopia.agent.bean.performance.school;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 科目活跃数据
 *
 * @author chunlin.yu
 * @create 2018-02-01 19:48
 **/
@Getter
@Setter
public class SubjectActiveItem {

    /**
     * 科目名称
     */
    private String subjectName;
    /**
     * 活跃类型；1：周活，2：月活
     */
    private int auType;
    private List<AU> aus;

    /**
     * 活跃
     */
    @Getter
    @Setter
    public class AU{
        /**
         * 展示名称
         */
        private String showName;
        /**
         * 活跃数量
         */
        private int activeCount;
        /**
         * 留存率
         */
        private double rtRate;
    }
}
