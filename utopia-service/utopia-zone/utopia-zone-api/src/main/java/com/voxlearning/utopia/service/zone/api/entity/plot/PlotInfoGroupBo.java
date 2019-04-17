package com.voxlearning.utopia.service.zone.api.entity.plot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : kai.sun
 * @version : 2018-11-14
 * @description :
 **/

@Getter
@Setter
@NoArgsConstructor
public class PlotInfoGroupBo implements Serializable {

    private static final long serialVersionUID = 1580199356845512335L;
    private String id;

    private Integer activityId;

    private String bgm;

    private String audioUrl;

    private String unlockCoverImg;

    public PlotInfoGroupBo(PlotInfo plotInfo){
        this.id = plotInfo.getId();
        this.activityId = plotInfo.getActivityId();
        this.bgm = plotInfo.getBgm();
        this.audioUrl = plotInfo.getAudioUrl();
        this.unlockCoverImg = plotInfo.getUnlockCoverImg();
    }

}
