package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 活动城市的基本信息
 * Created by yagaung.wang on 2016/8/2.
 */
@Getter
@Setter
public class CityInfo implements Serializable {
    private static final long serialVersionUID = -960296254722998882L;

    private Integer cityCode;
    private String cityName;
}
