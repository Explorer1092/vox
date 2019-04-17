package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.LeaveWordGoodsListEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class LeaveWordGoodsListMapper implements Serializable {
    private Long integralNum;
    private Long fragmentNum;
    private List<LeaveWordGoodsListEntity> leaveWordGoodsList;
}
