package com.voxlearning.utopia.service.campaign.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Data;

@Data
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_MORAL_MEDAL")
@UtopiaCacheRevision("20180626")
@UtopiaCacheExpiration(value = 7 * 60 * 60 * 24)
public class MoralMedal extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {

    private Long teacherId;     // 老师ID
    private Long studentId;     // 学生ID
    @UtopiaSqlColumn(name = "CLAZZ_ID")
    private Long groupId;       // 学生当前组 (历史遗留问题,本来说要用 CLAZZ ,线上建完表又改为了 GROUP )
    private Integer medalId;    // 勋章ID
    private Boolean liked;      // 是否已点赞 默认 false

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                ckId(id),
                CacheKeyGenerator.generateCacheKey(MoralMedal.class, "SID", studentId),
                CacheKeyGenerator.generateCacheKey(MoralMedal.class, new String[]{"TID", "CID"}, new Object[]{teacherId, groupId})
        };
    }

    public static String ckId(Long id) {
        return CacheKeyGenerator.generateCacheKey(MoralMedal.class, id);
    }
}
