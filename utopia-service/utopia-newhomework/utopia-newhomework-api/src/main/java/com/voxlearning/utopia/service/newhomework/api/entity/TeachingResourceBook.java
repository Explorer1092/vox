package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DocumentConnection(configName = "homework")
@DocumentTable(table = "VOX_TEACHING_RESOURCE_BOOK")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
@UtopiaCacheRevision("20190319")
public class TeachingResourceBook extends AbstractDatabaseEntity {
    private static final long serialVersionUID = -2692860519239309192L;

    @DocumentField("BOOK_ID") private String bookId;      // 教材id

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(TeachingResourceBook.class, id);
    }
}
