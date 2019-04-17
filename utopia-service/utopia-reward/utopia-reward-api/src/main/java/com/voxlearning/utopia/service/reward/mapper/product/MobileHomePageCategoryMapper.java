package com.voxlearning.utopia.service.reward.mapper.product;

import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2018-11-06 14:51
 **/
@Getter
@Setter
@ToString
public class MobileHomePageCategoryMapper implements Serializable{
    private Long id;
    private Integer type;
    private Integer oneLevelCategoryType;
    private String name;
    private Integer displayOrder;
    private List<RewardProductDetail> productList;
}
