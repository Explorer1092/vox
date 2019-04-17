package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * 老师布置新绘本历史
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "picture_book_plus_history")
@DocumentIndexes({
        @DocumentIndex(def = "{'teacherId', -1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180206")
public class PictureBookPlusHistory implements Serializable {
    private static final long serialVersionUID = -756089468104432041L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    private Long teacherId;                                 // 老师id
    private Integer year;                                   // 学年
    private Term term;                                      // 学期
    private Subject subject;                                // 学科
    private LinkedHashMap<String, Date> pictureBookInfo;    // 绘本信息  {绘本id，布置时间}

    @DocumentCreateTimestamp
    private Date createAt;                                  // 创建时间
    @DocumentUpdateTimestamp
    private Date updateAt;                                  // 更新时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(PictureBookPlusHistory.class, id);
    }
}
