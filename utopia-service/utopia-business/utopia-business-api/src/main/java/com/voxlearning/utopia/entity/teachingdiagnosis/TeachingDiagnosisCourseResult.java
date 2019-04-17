package com.voxlearning.utopia.entity.teachingdiagnosis;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "mongo-madrid")
@DocumentDatabase(database = "vox-diagnosis")
@DocumentCollection(collection = "vox_teaching_diagnosis_course_result")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180208")
public class TeachingDiagnosisCourseResult implements Serializable {
    private static final long serialVersionUID = 2254595379221491661L;
    private static final String ID_SEP = "-";
    @DocumentId
    private String id;
    private String taskId;
    private String courseId;
    private Date finishTime;

    public static String generateCacheKeyById(String id) {
        return CacheKeyGenerator.generateCacheKey(TeachingDiagnosisCourseResult.class, id);
    }

    public static String generateID(String taskId, String courseId) {
        return taskId + ID_SEP + courseId;
    }
}
