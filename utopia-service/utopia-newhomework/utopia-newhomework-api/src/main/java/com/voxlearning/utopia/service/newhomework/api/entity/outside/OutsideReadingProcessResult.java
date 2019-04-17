package com.voxlearning.utopia.service.newhomework.api.entity.outside;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-outside")
@DocumentCollection(collection = "outside_reading_process_result_{}", dynamic = true)
@UtopiaCacheExpiration(604800)
@UtopiaCacheRevision("20181114")
public class OutsideReadingProcessResult extends BaseHomeworkProcessResult implements Serializable {
    private static final long serialVersionUID = 6424184530310322125L;

    @DocumentId
    private String id;

    public String readingId;                         // 课外阅读ID
    public String missionId;                         // 关卡ID
    @DocumentCreateTimestamp
    private Date createAt;                          // 创建时间
    @DocumentUpdateTimestamp
    private Date updateAt;                          // 修改时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(OutsideReadingProcessResult.class, id);
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"time", "randomId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = 2760486728173793935L;
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
