package com.voxlearning.utopia.service.business.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.ObjectIdEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@UtopiaCacheRevision("20190315")
@DocumentDatabase(database = "vox_o2o")
@DocumentCollection(collection = "vox_teacher_resource_ref")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class TeacherResourceRef extends ObjectIdEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 7993281011107703887L;

    private Long teacherId;                           // 老师ID
    private String resourceId;                        // 资源ID
    private Boolean shareParent;                      // 是否有分享给家长

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                CacheKeyGenerator.generateCacheKey(TeacherResourceRef.class,
                        new String[]{"TID"},
                        new Object[]{this.teacherId}
                )
        };
    }

}
