package com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "reading_dubbing_recommend")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180409")
public class ReadingDubbingRecommend implements Serializable {
    private static final long serialVersionUID = -1795337913295727991L;
    @DocumentId
    private String id;                                  // 与作业id+ type + 绘本ID pictureId;

    private String pictureId;

    private ObjectiveConfigType type;

    private List<ReadingDubbing> readingDubbings;

    private Long teacherId;

    private String recommendComment;


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(ReadingDubbingRecommend.class, id);
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {
        private static final long serialVersionUID = 1761342947251257529L;

        private String hid;
        private ObjectiveConfigType type;
        private String pictureId;

        @Override
        public String toString() {
            return hid + "-" + type + "-" + pictureId;
        }
    }


    @Getter
    @Setter
    public static class ReadingDubbing implements Serializable {
        private static final long serialVersionUID = -2758759631349451413L;
        private String dubbingId;
        private AppOralScoreLevel dubbingScoreLevel;
        private int score;
        private long duration;
        private Long userId;
        private String userName;
    }
}
