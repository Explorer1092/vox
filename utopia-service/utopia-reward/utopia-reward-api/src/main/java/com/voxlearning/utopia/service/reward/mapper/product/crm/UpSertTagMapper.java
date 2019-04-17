package com.voxlearning.utopia.service.reward.mapper.product.crm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class UpSertTagMapper implements Serializable{
    private Long id;
    private String name;
    private Long parentId;
    private Integer parentType;
    private Integer displayOrder;
}
