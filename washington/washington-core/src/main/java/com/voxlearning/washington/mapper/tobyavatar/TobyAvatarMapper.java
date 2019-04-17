package com.voxlearning.washington.mapper.tobyavatar;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2018-10-24 20:21
 **/
@Getter
@Setter
@ToString
public class TobyAvatarMapper implements Serializable{

    TobyAvatarUnit tobyImage;
    TobyAvatarUnit tobyAccessory;
    TobyAvatarUnit tobyCountenance;
    TobyAvatarUnit tobyProps;

    @Getter
    @Setter
    @ToString
    public class TobyAvatarUnit{
        private Integer x;
        private Integer y;
        private Long productId;
        private String url;
    }

}
