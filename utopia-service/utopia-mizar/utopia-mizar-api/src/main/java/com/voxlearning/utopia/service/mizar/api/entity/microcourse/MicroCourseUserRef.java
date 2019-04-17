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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 微课堂-用户与课程关联关系
 * Created by Wang Yuechen on 2016/12/08.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "micro_course_user_ref")
@DocumentIndexes({
        @DocumentIndex(def = "{'courseId':1}", background = true),
        @DocumentIndex(def = "{'userId':1}", background = true),
})
@UtopiaCacheRevision("20161208")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MicroCourseUserRef implements CacheDimensionDocument {

    private static final long serialVersionUID = 1L;
    @DocumentId private String id;
    private String courseId;        // 课程ID
    private String userId;          // 用户ID
    private CourseUserRole role;              // 用户角色

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id),
                newCacheKey("C", courseId),
                newCacheKey("U", userId)
        };
    }

    public enum CourseUserRole {
        Lecturer,     // 讲师
        Assistant,    // 助教
        ;

        public static CourseUserRole parse(String role) {
            if (StringUtils.isBlank(role)) {
                return null;
            }
            try {
                return valueOf(role);
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
