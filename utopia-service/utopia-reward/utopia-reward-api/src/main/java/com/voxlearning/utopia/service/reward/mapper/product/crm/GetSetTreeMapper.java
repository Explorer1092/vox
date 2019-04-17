package com.voxlearning.utopia.service.reward.mapper.product.crm;

import com.voxlearning.utopia.service.reward.entity.newversion.ProductTag;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class GetSetTreeMapper {
    private Long id;
    private String name;
    private String visible;
    private Boolean display;
    private Integer displayOrder;
    private List<ProductTag> childrenTrgList;
}
