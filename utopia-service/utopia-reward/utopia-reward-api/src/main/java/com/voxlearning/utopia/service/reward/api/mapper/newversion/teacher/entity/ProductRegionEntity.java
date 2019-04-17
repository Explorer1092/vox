package com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity;

import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class ProductRegionEntity implements Serializable {
    private String categoryName;
    private Long categoryId;
    private String categoryCode;
    private List<RewardProductDetail> productList;
}
