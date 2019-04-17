package com.voxlearning.utopia.admin.mapper.reward.powerpillar;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class GetPrizeListMapper implements Serializable {
    private long id;
    private int prizeType;//1:碎片，2：实物
    private int level;
    private long productId;
    private int fragmentNum;
    private String name;
    private int stock;
    private int initStock;
    private Boolean isReserve;//是否是备选（所有正常配置奖品池中奖品都发完了，则发这个）
}
