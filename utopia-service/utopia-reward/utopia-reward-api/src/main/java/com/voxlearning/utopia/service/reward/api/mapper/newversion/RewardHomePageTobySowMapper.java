package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.HpMyTobyEntity;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.HpPublicGoodPlaqueEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class RewardHomePageTobySowMapper implements Serializable {
    private Boolean isPoweFull;
    private Boolean isHasNewLeaveWord;
    private Boolean isShowRenameTip;
    private Boolean isProductArea;
    private Integer fullPowerNumber;
    private Long integralNum;
    private Integer powerPillar;
    private HpMyTobyEntity toby;
    private HpPublicGoodPlaqueEntity publicGoodPlaque;
}
