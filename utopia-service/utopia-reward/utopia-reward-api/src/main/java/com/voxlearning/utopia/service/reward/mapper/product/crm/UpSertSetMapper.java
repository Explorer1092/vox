package com.voxlearning.utopia.service.reward.mapper.product.crm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class UpSertSetMapper implements Serializable{
    private Long id;
    private String name;
    private Integer visible;
    private Boolean display;
    private Integer displayOrder;
}
