package com.voxlearning.utopia.service.reward.mapper.product;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2018-11-06 14:51
 **/
@Getter
@Setter
@ToString
public class CategoryMapper implements Serializable{
    private Long id;
    private Integer type;
    private String name;
    private Integer displayOrder;
}
