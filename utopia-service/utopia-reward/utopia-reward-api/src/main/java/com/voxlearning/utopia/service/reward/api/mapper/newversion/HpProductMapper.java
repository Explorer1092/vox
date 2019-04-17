package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.HpProductEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class HpProductMapper implements Serializable {
    private List<HpProductEntity> topknotList;
    private List<HpProductEntity> hotProductList;
    private List<HpProductEntity> hotPublicGoodTagList;
}
