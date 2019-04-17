package com.voxlearning.utopia.service.mizar.api.entity.microcourse;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 微课堂-课程课时关联关系
 * Created by Wang Yuechen on 2016/12/08.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "micro_course_period_ref")
@DocumentIndexes({
        @DocumentIndex(def = "{'courseId':1}", background = true),
        @DocumentIndex(def = "{'periodId':1}", background = true),
})
@UtopiaCacheRevision("20161214")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MicroCoursePeriodRef implements CacheDimensionDocument {

    private static final long serialVersionUID = 1L;
    @DocumentId private String id;
    private String courseId;        // 课程ID
    private String periodId;        // 课时ID

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("C", courseId),
                newCacheKey("P", periodId)
        };
    }
}
