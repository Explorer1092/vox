package com.voxlearning.utopia.service.reward.api.enums.support;

import com.voxlearning.utopia.service.reward.api.enums.RewardTagEnum;
import lombok.Data;

@Data
public class TagEnumNode {
    private RewardTagEnum tagEnum;
    private RewardTagEnum[] children;
}
