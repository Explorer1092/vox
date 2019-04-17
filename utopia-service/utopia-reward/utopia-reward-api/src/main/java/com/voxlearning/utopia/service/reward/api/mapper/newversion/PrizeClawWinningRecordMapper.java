package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.PrizeClawWinningRecordEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class PrizeClawWinningRecordMapper implements Serializable {
    private Integer consumeSum;
    private Integer winningSum;
    private List<PrizeClawWinningRecordEntity> prizeRecordList;
}
