package com.voxlearning.utopia.agent.view.performance;

import lombok.Getter;
import lombok.Setter;

/**
 * PerformanceKlxViewDataItem
 *
 * @author song.wang
 * @date 2018/4/17
 */
@Setter
@Getter
public class PerformanceKlxViewDataItem {

    private String name;

    private int tmFinTpGte1StuCount;            // 普通扫描 >= 1 套
    private int tmFinTpGte3StuCount;            // 普通扫描 >= 3 套


    private int tmFinBgExamGte3SubjStuCount;            // 大考 >= 3 科
    private int tmFinBgExamGte6SubjStuCount;            // 大考 >= 6 科

}
