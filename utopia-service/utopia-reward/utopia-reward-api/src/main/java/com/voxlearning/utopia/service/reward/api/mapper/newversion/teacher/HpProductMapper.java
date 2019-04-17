package com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher;

import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.ProductRegionEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class HpProductMapper implements Serializable {

    private List<ProductRegionEntity> productRegion;
    private ProductRegionEntity materialRegion;

}
