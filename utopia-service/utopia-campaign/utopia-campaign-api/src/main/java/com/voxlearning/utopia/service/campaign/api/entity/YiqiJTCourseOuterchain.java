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
@DocumentTable(table = "VOX_17JT_COURSE_OUTERCHAIN")
@CacheBean(type = YiqiJTCourseOuterchain.class)
@UtopiaCacheRevision("20180626")
public class YiqiJTCourseOuterchain extends AbstractDatabaseEntity {
    @UtopiaSqlColumn(name = "COURSE_ID") private Long courseId;
    @UtopiaSqlColumn(name = "OUTERCHAIN") private String outerchain;
    @UtopiaSqlColumn(name = "OUTERCHAIN_URL") private String outerchainUrl;
}
