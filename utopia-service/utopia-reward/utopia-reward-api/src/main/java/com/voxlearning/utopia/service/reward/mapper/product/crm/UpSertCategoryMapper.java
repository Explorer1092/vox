package com.voxlearning.utopia.service.reward.mapper.product.crm;

import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class UpSertCategoryMapper implements Serializable{
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer visible;
    private Boolean display;
    private Integer displayOrder;
}
