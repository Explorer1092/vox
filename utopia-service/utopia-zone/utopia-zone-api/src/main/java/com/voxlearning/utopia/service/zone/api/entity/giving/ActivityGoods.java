package com.voxlearning.utopia.service.zone.api.entity.giving;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author : kai.sun
 * @version : 2018-11-16
 * @description :
 **/
@Getter
@Setter
@NoArgsConstructor
public class ActivityGoods implements Serializable {
    private static final long serialVersionUID = -8774631414328854070L;
    private Integer type;
    private String name;
    private Integer num;
}
