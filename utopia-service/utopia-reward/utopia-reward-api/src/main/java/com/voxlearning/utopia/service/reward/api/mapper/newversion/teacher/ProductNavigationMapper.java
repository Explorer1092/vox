package com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class ProductNavigationMapper implements Serializable{
    private Long categoryId;
    private String categoryCode;
    private String name;

}
