package com.voxlearning.utopia.service.zone.api.entity.giving;

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
public class SelfActivityProgress implements Serializable {
    private static final long serialVersionUID = 1581600002871218333L;
    private Integer progress;   //个人进度
    private List<SelfProgressAward> awardList;
    private List<ActivityGoods> goodsList;
    private Boolean isHave;//是否有邀请次数
}
