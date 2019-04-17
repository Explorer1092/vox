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
import java.util.List;
@Getter
@Setter
@DocumentConnection(configName = "mongo-madrid")
@DocumentDatabase(database = "vox-diagnosis")
@DocumentCollection(collection = "vox_teaching_diagnosis_task")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180720")
public class TeachingDiagnosisTask implements Serializable {

    private static final long serialVersionUID = 151282530630791997L;
    private static String ID_SEP = "-";
    @DocumentId
    private String id;
    private Long userId;
    private String experimentId;
    private String experimentGroupId;
    private List<TeachingDiagnosisTaskCourse> courses;
    private List<String> previewQuestionList;
    private Integer totalNumber;
    private Integer wrongNumber;
    private Date createTime;
    private Date updateTime;

    public static String generateId(Long studentId, String experimentGroupId) {
        return studentId + ID_SEP + experimentGroupId;
    }

    public static String generateCacheKeyById(String id) {
        return CacheKeyGenerator.generateCacheKey(TeachingDiagnosisTask.class, id);
    }

    public static String generateCacheKeyByUserId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(TeachingDiagnosisTask.class,
                new String[]{"UId"},
                new Object[]{userId},
                new Object[]{0L});
    }
}
