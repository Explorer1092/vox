package com.voxlearning.utopia.service.campaign.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@UtopiaCacheRevision("20181022")
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_TEACHER_COURSEWARE_RESOURCE_REF")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@EqualsAndHashCode(of = {"userId", "resourceType", "resourceId"}, callSuper = false)
public class TeacherResourceRef extends AbstractDatabaseEntityWithDisabledField implements Serializable, CacheDimensionDocument {

    private static final long serialVersionUID = 6989490205718509539L;

    private Long userId;
    private Type resourceType;
    private String resourceId;
    private String resourceName;
    private String url;

    public String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(TeacherResourceRef.class,
                new String[]{"UID"},
                new Object[]{userId}
        );
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                ck_userId(this.userId)
        };
    }

    @Getter
    public enum Type {
        COURSEWARE(0, "课件大赛"),
        ;

        Type(int code, String name) {
            this.code = code;
            this.name = name;
        }

        private int code;
        private String name;
    }
}
