package com.voxlearning.utopia.service.campaign.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.campaign.api.enums.TeacherActivityEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 本来是给老师用的, 后来也开始存家长(要是有一列存用户 type 就更好了)
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_TEACHER_ACTIVITY_REF")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181210")
public class TeacherActivityRef extends AbstractDatabaseEntity implements CacheDimensionDocument {

    @UtopiaSqlColumn
    private Long userId;            // 用户 ID
    @UtopiaSqlColumn
    private String type;            // 活动类型

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                ckId(id),
                ckType(type),
                ckIdTypeId(userId, type)
        };
    }

    private static String ckId(Long refId) {
        return CacheKeyGenerator.generateCacheKey(TeacherActivityRef.class, refId);
    }

    private static String ckType(String type) {
        return CacheKeyGenerator.generateCacheKey(TeacherActivityRef.class, type);
    }

    private String ckIdTypeId(Long userId, String type) {
        return CacheKeyGenerator.generateCacheKey(TeacherActivityRef.class, new String[]{"UID", "TYPE"}, new Object[]{userId, type});
    }

    public TeacherActivityEnum fetchActivityType() {
        try {
            return TeacherActivityEnum.valueOf(type);
        } catch (Exception e) {
            return null;
        }
    }

}
