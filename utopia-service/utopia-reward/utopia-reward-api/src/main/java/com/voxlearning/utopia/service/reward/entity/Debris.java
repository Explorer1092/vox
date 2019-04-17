package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.StringIdEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_DEBRIS")
@EqualsAndHashCode(callSuper = false, of = "userId")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180831")
public class Debris extends StringIdEntity implements CacheDimensionDocument {

    private static final long serialVersionUID = 8251773061487514282L;

    @UtopiaSqlColumn(name = "USER_ID") private Long userId;
    @UtopiaSqlColumn(name = "TOTAL_DEBRIS") private Long totalDebris;
    @UtopiaSqlColumn(name = "USABLE_DEBRIS") private Long usableDebris;

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(Debris.class, "U", userId);
    }

    public Debris() {
    }

    public Debris(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        long total = totalDebris == null ? 0 : totalDebris;
        long usable = usableDebris == null ? 0 : usableDebris;
        return "(Debris:" + userId + ",total=" + total + ",usable=" + usable + ")";
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                ck_userId(userId)
        };
    }

}
