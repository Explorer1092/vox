package com.voxlearning.utopia.agent.view.performance;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Performance17ViewDataItem
 *
 * @author song.wang
 * @date 2018/3/6
 */
@Getter
@Setter
public class Performance17ViewDataItem {

    private String name;

    private int tmFinHwGte1StuCount;               // 本月1套（新增或回流）（全部）
    private int pdFinHwGte1StuCount;               // 昨日1套（新增或回流）（全部）

    private int tmFinHwGte3AuStuCount;             // 本月认证3套（新增或回流）
    private int pdFinHwGte3AuStuCount;             // 昨日认证3套（新增或回流）

    private int budget;                            // 预算
    private double completeRate;                   // 完成率

    private double mrtRate1;                      // 1套留存率1
    private double mrtRate2;                      // 3套留存率2


}
