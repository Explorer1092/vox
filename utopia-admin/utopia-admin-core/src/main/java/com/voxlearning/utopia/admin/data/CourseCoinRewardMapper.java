package com.voxlearning.utopia.admin.data;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xuerui.zhang
 * @since 2018/9/19 下午4:11
 */
@Data
public class CourseCoinRewardMapper implements Serializable {
    private String id;
    private Long spuId;
    private Integer coinTypeId;
    private Integer coinCount;
    private String rewardType;
    private Integer rewardCount;
    private Integer weekCount;
    private Integer dayCount;
    private String createUser;
}
