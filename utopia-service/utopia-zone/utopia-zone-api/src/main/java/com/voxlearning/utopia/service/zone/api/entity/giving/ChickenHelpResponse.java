package com.voxlearning.utopia.service.zone.api.entity.giving;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 吃鸡助力类
 * @author dongfeng.xue
 * @date 2018-11-14
 */
@Getter@Setter
public class ChickenHelpResponse implements Serializable {
    private static final long serialVersionUID = -2817707488165114925L;
    private String name;
    private Long userId;
    private String pic;
    private String id;
    private Integer count; //数量
}
