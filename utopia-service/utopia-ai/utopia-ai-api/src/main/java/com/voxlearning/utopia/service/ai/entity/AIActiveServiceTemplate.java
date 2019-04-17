package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author guangqing
 * @since 2018/10/30
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_ai_active_service_template")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181030")
public class AIActiveServiceTemplate implements Serializable {

    private static final long serialVersionUID = 5222568932838164762L;

    @DocumentId
    private String id;
    private String unitId;
    private String bookId;
    private String bookName;
    private String title;
    private String json;
    @DocumentCreateTimestamp
    private Date createDate; // 做题时间
    @DocumentUpdateTimestamp
    private Date updateDate; // 更新时间

    public static String ck_bookId(String bookId) {
        return CacheKeyGenerator.generateCacheKey(AIActiveServiceTemplate.class, "bookId", bookId);
    }

    public static String ck_unitId(String unitId) {
        return CacheKeyGenerator.generateCacheKey(AIActiveServiceTemplate.class, "unitId", unitId);
    }

    public static String ck_bookId_unitId(String bookId, String unitId) {
        return CacheKeyGenerator.generateCacheKey(AIActiveServiceTemplate.class, new String[]{"bookId", "unitId"}, new Object[]{bookId, unitId});
    }


}
