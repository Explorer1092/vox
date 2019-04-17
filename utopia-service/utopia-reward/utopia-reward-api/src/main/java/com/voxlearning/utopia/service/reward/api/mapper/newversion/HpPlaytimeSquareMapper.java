package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.PlaytimeGameEntity;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.PlaytimeVideoEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class HpPlaytimeSquareMapper implements Serializable {
    private PlaytimeVideoEntity video;
    private PlaytimeGameEntity game;

}
