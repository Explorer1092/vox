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
@DocumentTable(table = "VOX_17JT_COURSE_GRADE")
@CacheBean(type = YiqiJTCourseGrade.class)
@UtopiaCacheRevision("20180626")
public class YiqiJTCourseGrade extends AbstractDatabaseEntity {
    @UtopiaSqlColumn(name = "COURSE_ID") private Long courseId;
    @UtopiaSqlColumn(name = "GRADE_ID") private Integer gradeId;
}
