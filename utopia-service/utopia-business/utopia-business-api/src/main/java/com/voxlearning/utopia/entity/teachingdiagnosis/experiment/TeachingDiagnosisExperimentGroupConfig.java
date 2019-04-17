package com.voxlearning.utopia.entity.teachingdiagnosis.experiment;

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
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.ExperimentType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Getter
@Setter
@DocumentConnection(configName = "mongo-madrid")
@DocumentDatabase(database = "vox-diagnosis")
@DocumentCollection(collection = "mdl_course_experiment_group_config")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180725")
public class TeachingDiagnosisExperimentGroupConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    @DocumentId
    private String id;
    private String name;
    private ExperimentType groupType;
    private Map<String, Object> ext;
    private String updater;
    private Boolean disabled;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TeachingDiagnosisExperimentGroupConfig.class, id);
    }

    public static String ck_all(ExperimentType type) {
        return CacheKeyGenerator.generateCacheKey(TeachingDiagnosisExperimentGroupConfig.class,
                new String[]{"TYPE"},
                new Object[]{type});
    }
}
