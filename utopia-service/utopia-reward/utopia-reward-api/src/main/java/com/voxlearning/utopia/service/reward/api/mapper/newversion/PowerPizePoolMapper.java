package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class PowerPizePoolMapper implements Serializable{

    private Integer powerPillar;
    private Integer fullPowerNumber;
    private Long fragmentNum;
    private List<RealGoodsEntity> realGoodsList;

    @Getter
    @Setter
    @ToString
    public class RealGoodsEntity implements Serializable {
        private String name;
        private String pictuerUrl;
    }
}
