package com.voxlearning.utopia.service.reward.api.mapper.newversion.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PrizeClawWinningRecordEntity implements Serializable {
    private String createTime;
    private Integer consumeNum;
    private String prize;
}
