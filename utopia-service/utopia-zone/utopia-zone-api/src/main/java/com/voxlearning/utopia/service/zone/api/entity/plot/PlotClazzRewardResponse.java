package com.voxlearning.utopia.service.zone.api.entity.plot;

import com.voxlearning.utopia.service.zone.api.entity.boss.AwardDetail;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author : xdf
 * @version : 2018-11
 **/
@Getter
@Setter
public class PlotClazzRewardResponse implements Serializable {
    private static final long serialVersionUID = -2817707488165114925L;
    private Integer contribution; //班级贡献值
    private List<AwardDetail> awards;
}
