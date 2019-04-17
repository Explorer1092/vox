package com.voxlearning.utopia.service.reward.mapper.product.crm;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2018-10-16 15:17
 **/
@Getter
@Setter
@ToString
public class GetProductDetailMapper {
    private List<CategoryMapper> oneLevelCategoryMappers;
    private List<CategoryMapper> twoLevelCategoryMappers;
    private List<TagMapper> categoryTagMappers;
    private List<List<SetMapper>> setMappers;

    @Getter
    @Setter
    @ToString
    public class CategoryMapper{
        private Long id;
        private String name;
        private Boolean isSelected;
        private Integer oneLevelCategoryType;
    }

    @Getter
    @Setter
    @ToString
    public class SetMapper{
        private Long id;
        private String name;
        private Boolean isSelected;
        private List<TagMapper> tagMappers;
    }

    @Getter
    @Setter
    @ToString
    public class TagMapper{
        private Long id;
        private String name;
        private Boolean isSelected;
    }
}
