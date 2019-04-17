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
@DocumentCollection(collection = "vox_teaching_diagnosis_preview_question_result")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180725")
public class TeachingDiagnosisPreviewQuestionResult implements Serializable {

    private static final long serialVersionUID = 903218648134447724L;
    private static final String ID_SEP = "-";
    @DocumentId
    private String id;
    private Long userId;
    private String questionId;
    private String experimentId;
    private Boolean master;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    private List<List<String>> userAnswer;
    private Long duration;

    public static String generateId(Long studentId, String questionId, String experimentId) {
        return studentId + ID_SEP + questionId + ID_SEP + experimentId;
    }
    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TeachingDiagnosisPreviewQuestionResult.class, id);
    }

}
