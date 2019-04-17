package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * PerformanceViewDataLmRateItem
 *
 * @author song.wang
 * @date 2017/11/8
 */
@Getter
@Setter
public class PerformanceViewDataLmRateItem {

    private String name;
    private double maucLmRate;     // 月环比（月活）
    private double incMaucLmRate;  // 月环比（新增）
    private double ltMaucLmRate;   // 月环比（长回）
    private double stMaucLmRate;   // 月环比（短回）


}
