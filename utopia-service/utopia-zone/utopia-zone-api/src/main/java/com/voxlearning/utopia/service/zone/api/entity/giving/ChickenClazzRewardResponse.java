package com.voxlearning.utopia.service.zone.api.entity.giving;

import com.voxlearning.utopia.service.zone.api.entity.boss.AwardDetail;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 吃鸡助力类
 * @author dongfeng.xue
 * @date 2018-11-14
 */
@Getter@Setter
public class ChickenClazzRewardResponse implements Serializable {
    private static final long serialVersionUID = -2817707488165114925L;
    private Integer targetValue;
//    private String name;
//    private String pic;
//    private String num;
    private String id; //奖励id
    private Boolean isReceive; //是否领取
    private List<AwardDetail> awards;
}
