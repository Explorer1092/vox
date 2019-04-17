package com.voxlearning.utopia.service.campaign.impl.service.lottery;

import com.voxlearning.utopia.service.campaign.api.enums.ActivityCardEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Lottery {
    private ActivityCardEnum card;
    private double rate;
    private double target;

    Lottery(ActivityCardEnum card, double rate) {
        this.card = card;
        this.rate = rate;
    }
}