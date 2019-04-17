package com.voxlearning.utopia.entity.teachingdiagnosis;

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
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@DocumentConnection(configName = "mongo-madrid")
@DocumentDatabase(database = "vox-diagnosis")
@DocumentCollection(collection = "vox_teaching_diagnosis_course_question_result")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180719")
public class TeachingDiagnosisCourseQuestionResult implements Serializable {

    private static final long serialVersionUID = 903218648134447724L;
    private static String ID_SEP = "-";
    @DocumentId
    private String id;
    private Long userId;
    private String questionId;
    private String courseId;
    private String taskId;
    private Boolean master;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    private List<List<String>> userAnswer;
    private Long duration;

    public static String ck_task(String taskId) {
        return CacheKeyGenerator.generateCacheKey(TeachingDiagnosisCourseQuestionResult.class,
                new String[]{"taskId"},
                new Object[]{taskId});
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TeachingDiagnosisCourseQuestionResult.class, id);
    }

    public static String generateId(String taskId, String questionId) {
        return taskId + ID_SEP + questionId;
    }
}
