package com.voxlearning.utopia.service.zone.api.entity.giving;

import com.voxlearning.utopia.service.zone.api.entity.boss.AwardDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author : kai.sun
 * @version : 2018-11-16
 * @description :
 **/

@Getter
@Setter
@NoArgsConstructor
public class SelfProgressAward implements Serializable {
    private static final long serialVersionUID = -2451179843629639475L;
    private Integer type; //不同类型 （对应不同奖励 1 2 3）
    private Integer selfTarget; //个人目标进度
    private Boolean receive; //是否领取
    private List<AwardDetail> awards; //奖励
    private String activityAwardId;
}
