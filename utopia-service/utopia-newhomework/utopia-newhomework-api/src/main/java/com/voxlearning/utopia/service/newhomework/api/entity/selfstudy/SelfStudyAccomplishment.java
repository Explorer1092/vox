package com.voxlearning.utopia.service.newhomework.api.entity.selfstudy;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Getter
@Setter
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "self_study_accomplishment")
@UtopiaCacheExpiration(345600)
@UtopiaCacheRevision("20181016")
public class SelfStudyAccomplishment implements Serializable {
    private static final long serialVersionUID = 8214487668191545825L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;                                      // 主键id，直接使用作业id
    private Map<Long, Detail> details;                      // <StudentId, Detail>

    @Getter
    @Setter
    public static class Detail implements Serializable {
        private static final long serialVersionUID = -215630283490856530L;

        private String selfStudyHomeworkId;                 // 订正任务id
        private Date finishAt;                              // 订正完成时间
    }

    public static String cacheKeyFromId(String id) {
        return CacheKeyGenerator.generateCacheKey(SelfStudyAccomplishment.class, id);
    }
}
