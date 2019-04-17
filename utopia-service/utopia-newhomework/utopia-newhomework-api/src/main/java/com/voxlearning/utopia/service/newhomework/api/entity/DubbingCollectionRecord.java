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
import java.util.Date;
import java.util.Map;

/**
 * @Description: 趣味配音收藏记录
 * @author: Mr_VanGogh
 * @date: 2018/8/23 下午2:23
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "dubbing_collection_record")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180907")
public class DubbingCollectionRecord implements Serializable {
    private static final long serialVersionUID = -3060673660128441663L;

    @DocumentId
    private String id;
    private Long teacherId;                                 // 老师id
    private Subject subject;                                // 学科
    private Integer year;                                   // 学年
    private Term term;                                      // 学期
    private Map<String, Date> dubbingCollectionInfo;        // 配音收藏<DubbingId，收藏时间(null:否；时间戳:是)>
    @DocumentCreateTimestamp
    private Long createAt;                                  // 创建时间
    @DocumentUpdateTimestamp
    private Long updateAt;                                  // 更新时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(DubbingCollectionRecord.class, id);
    }
}
