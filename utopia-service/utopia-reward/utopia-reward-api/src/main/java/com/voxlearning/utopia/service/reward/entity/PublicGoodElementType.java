package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 *  公益 - 元素类别
 * Created by ganhaitian on 2018/6/11.
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_PUBLIC_GOOD_ELEMENT_TYPE")
public class PublicGoodElementType extends AbstractDatabaseEntity{

    private static final long serialVersionUID = 2238725048340799100L;

    @UtopiaSqlColumn(name = "CODE") private String code;
    @UtopiaSqlColumn(name = "NAME") private String name;
    @UtopiaSqlColumn(name = "STYLE_ID") private Long styleId;
    @UtopiaSqlColumn(name = "PRICE") private Integer price;
    @UtopiaSqlColumn(name = "IMG_URL") private String img;
    @UtopiaSqlColumn(name = "QUANTIFIER") private String quantifier;

}
