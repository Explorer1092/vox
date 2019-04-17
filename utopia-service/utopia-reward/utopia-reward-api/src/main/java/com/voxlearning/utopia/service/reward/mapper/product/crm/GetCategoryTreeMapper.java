package com.voxlearning.utopia.service.reward.mapper.product.crm;

import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductTag;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class GetCategoryTreeMapper {
    private Long id;
    private String name;
    private String visible;
    private Boolean display;
    private Integer displayOrder;
    private Integer oneLevelCategoryType;
    private List<ChildrenCategory> childrenCategory;

    @Getter
    @Setter
    @ToString
    public class ChildrenCategory{
        private Long id;
        private String name;
        private String visible;
        private Boolean display;
        private Integer displayOrder;
        private Integer twoLevelCategoryType;
        private List<ProductTag> childrenTrgList;
    }
}
