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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "mongo-madrid")
@DocumentDatabase(database = "vox-diagnosis")
@DocumentCollection(collection = "mdl_course_experiment_log")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180724")
public class DiagnosisExperimentLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @DocumentId
    private String id;
    private String experimentId;
    private Operation operation;
    private String updater;

    private Boolean disabled;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(DiagnosisExperimentLog.class, id);
    }

    public static String ck_exp_id(String experimentId) {
        return CacheKeyGenerator.generateCacheKey(DiagnosisExperimentLog.class,
                new String[]{"EXP"},
                new Object[]{experimentId});
    }
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Operation {
        Create("创建"), Modify("修改"), GoOnline("上线"), GoOffline("下线"), Delete("删除");
        @Getter
        private final String description;
    }
}
