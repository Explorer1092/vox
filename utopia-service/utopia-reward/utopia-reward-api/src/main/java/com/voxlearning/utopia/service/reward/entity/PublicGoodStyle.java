package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 公益 - 样式实体
 * Created by ganhaitian on 2018/6/11.
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_PUBLIC_GOOD_STYLE")
public class PublicGoodStyle extends AbstractDatabaseEntity{

    private static final long serialVersionUID = 8132421182431552962L;

    @UtopiaSqlColumn(name = "MODEL") private String model;
    @UtopiaSqlColumn(name = "NAME") private String name;                    // 名称
    @UtopiaSqlColumn(name = "SUMMARY") private String summary;              // 简介
    @UtopiaSqlColumn(name = "PREVIEW_IMG_URL") private String previewImg;   // 预览图

}
