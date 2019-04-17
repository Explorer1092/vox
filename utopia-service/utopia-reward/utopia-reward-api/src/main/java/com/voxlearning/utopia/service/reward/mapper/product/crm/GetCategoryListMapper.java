package com.voxlearning.utopia.service.reward.mapper.product.crm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GetCategoryListMapper {
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer oneLevelCategoryType;
    private Integer twoLevelCategoryType;
}
