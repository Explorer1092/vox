package com.voxlearning.utopia.service.campaign.api.entity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_17JT_COURSE_CATALOG")
@CacheBean(type = YiqiJTCourseCatalog.class)
@UtopiaCacheRevision("20180710")
public class YiqiJTCourseCatalog extends AbstractDatabaseEntity {
    @UtopiaSqlColumn(name = "COURSE_ID") private Long courseId;
    @UtopiaSqlColumn(name = "TIME_NODE") private String timeNode;
    @UtopiaSqlColumn(name = "CATALOG_DESCRIBE") private String catalogDescribe;
}
