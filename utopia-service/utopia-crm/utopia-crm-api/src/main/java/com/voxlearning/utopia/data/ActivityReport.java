package com.voxlearning.utopia.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 活动报告
 */
@Setter
@Getter
public class ActivityReport implements Serializable {

    private Integer total; // 参与总人数

    private Integer complete; // 投放人数, 不计算

    private Integer avgTime; // 平均用时

    private Double avgScore; // 平均分数

    private Integer hScore; // 最高分

    private Integer lScore; // 最低分
}
