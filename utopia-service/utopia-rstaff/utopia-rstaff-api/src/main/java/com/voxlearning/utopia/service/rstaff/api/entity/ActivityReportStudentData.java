package com.voxlearning.utopia.service.rstaff.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.ObjectIdEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DocumentTable(table = "VOX_ACTIVITY_REPORT_STUDENT_DATA_{}", dynamic = true)
@DocumentConnection(configName = "hs_misc")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ActivityReportStudentData extends ObjectIdEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = -8152408146315381861L;

    private String activityId;           // 活动 code
    private Long clazzId;                // 班级 ID
    private Long studentId;              // 学生 ID
    private String studentName;          // 学生名称
    private Integer dayCount;            // 参与天数
    private Integer maxScore;            // 最高分
    private Long minTime;                // 最短用时

    public String ckActivityIdClazzId(String activityId, Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(ActivityReportStudentData.class,
                new String[]{"AID", "CID"},
                new Object[]{activityId, clazzId}
        );
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                ckActivityIdClazzId(activityId, clazzId)
        };
    }
}
