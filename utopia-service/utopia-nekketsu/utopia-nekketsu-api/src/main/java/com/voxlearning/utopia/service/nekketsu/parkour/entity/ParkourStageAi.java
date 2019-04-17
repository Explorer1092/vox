package com.voxlearning.utopia.service.nekketsu.parkour.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Sadi.Wan on 2014/8/19.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParkourStageAi  implements Serializable {
    private static final long serialVersionUID = 6531087817202770089L;
    /** 角色名 */
    private String roleName = "";
    /** 等级。需要根据等级来算出对应的平跑速度 */
    private int level;
    /** 每道题平均耗时毫秒数。根据这个数据，让ai定时进行一次假答题 */
    private int timePerQuestion;
    /** 正确率,每次假答题的答对概率 */
    private double correctRate;
}
