package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "dubbing_recommend_record")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180521")
public class DubbingRecommendRecord implements Serializable {

    private static final long serialVersionUID = 8084891487337597332L;

    @DocumentId
    private String id;
    private Long teacherId;                                 // 老师id
    private Subject subject;                                // 学科
    private Integer year;                                   // 学年
    private Term term;                                      // 学期
    private Map<String, Integer> dubbingRecommendInfo;      // 配音推荐次数<配音docId，推荐次数>
    @DocumentCreateTimestamp
    private Long createAt;                                  // 创建时间
    @DocumentUpdateTimestamp
    private Long updateAt;                                  // 更新时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(DubbingRecommendRecord.class, id);
    }
}
