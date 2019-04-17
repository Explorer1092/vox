package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xinxin
 * @since 10/8/2016
 */
@Getter
@Setter
public class StudentGrowthLevelRewardMapper implements Serializable {
    private static final long serialVersionUID = 662914031727571921L;

    private Integer integralCount;  //学豆数量
    private String headWearId;  //头饰ID
    private Integer state; //领取状态:0不可领取　1已领取　2未领取
    private Integer level;
}
