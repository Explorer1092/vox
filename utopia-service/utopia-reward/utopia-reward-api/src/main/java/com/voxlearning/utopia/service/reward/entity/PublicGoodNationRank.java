package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 公益 - 全国榜实体
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_PUBLIC_GOOD_NATION_RANK")
public class PublicGoodNationRank extends AbstractDatabaseEntity{

    private static final long serialVersionUID = 727678524125026221L;

    @UtopiaSqlColumn(name = "SCHOOL_NAME") private String schoolName;
    @UtopiaSqlColumn(name = "MONEY") private Long money;

}
