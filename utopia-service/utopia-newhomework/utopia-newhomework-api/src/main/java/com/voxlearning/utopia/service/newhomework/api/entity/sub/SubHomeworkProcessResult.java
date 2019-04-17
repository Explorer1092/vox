package com.voxlearning.utopia.service.newhomework.api.entity.sub;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.QuestionWrongReason;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2017/1/13
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "homework_process_result_{}", dynamic = true)
@DocumentIndexes({
        @DocumentIndex(def = "{'subject':1,'homeworkId':1}", background = true)
})
@UtopiaCacheExpiration(172800)
@UtopiaCacheRevision("20190214")
public class SubHomeworkProcessResult extends BaseHomeworkProcessResult implements Serializable {

    private static final long serialVersionUID = -6510419414359057527L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    private Date createAt;                          // 创建时间
    @DocumentUpdateTimestamp
    private Date updateAt;                          // 修改时间
    private String sourceQuestionId;                // 原题id
    private QuestionWrongReason wrongReason;        // 错题原因

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(SubHomeworkProcessResult.class, id);
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"time", "randomId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = 965122411794950351L;
        private String randomId = RandomUtils.nextObjectId();
        private String time;

        public ID(Date createTime) {
            this.time = Long.toString(createTime.getTime());
        }

        @Override
        public String toString() {
            return randomId + "-" + time;
        }
    }
}
