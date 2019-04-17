package com.voxlearning.utopia.service.newhomework.api.entity.vacation;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
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
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2016/11/25
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-winter-vacation-2019")
@DocumentCollection(collection = "vacation_homework_process_result_{}", dynamic = true)
@DocumentIndexes({
        @DocumentIndex(def = "{'subject':1,'homeworkId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181128")
public class VacationHomeworkProcessResult extends BaseHomeworkProcessResult implements Serializable {

    private static final long serialVersionUID = 3707135780432210630L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    private Date createAt;                          // 创建时间
    @DocumentUpdateTimestamp
    private Date updateAt;                          // 修改时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(VacationHomeworkProcessResult.class, id);
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"time", "randomId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {
        private static final long serialVersionUID = -2418821819635365365L;

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
