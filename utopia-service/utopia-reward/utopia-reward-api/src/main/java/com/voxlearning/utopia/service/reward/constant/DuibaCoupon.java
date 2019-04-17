package com.voxlearning.utopia.service.reward.constant;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author songtao
 * @since 2018/4/27
 */
@Getter
@Setter
public class DuibaCoupon implements Serializable{
    private static final long serialVersionUID = 6475627601202003280L;
    private String name;
    private String type;
    private Long credits;
    private String orderNum;
    private Integer actualPrice;
    private Integer duibaAppId;
}
