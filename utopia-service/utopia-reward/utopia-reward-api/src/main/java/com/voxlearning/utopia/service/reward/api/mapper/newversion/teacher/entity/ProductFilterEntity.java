package com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class ProductFilterEntity {
    private Long id;
    private String name;
    private Boolean status;
    private List<ProductFilterEntity> children;

    public static ProductFilterEntity buildDefaultFilterItem(Boolean status) {
        ProductFilterEntity entity = new ProductFilterEntity();
        entity.setStatus(status);
        entity.setChildren(new ArrayList<>());
        entity.setId(0l);
        entity.setName("全部");
        return entity;
    }
}
