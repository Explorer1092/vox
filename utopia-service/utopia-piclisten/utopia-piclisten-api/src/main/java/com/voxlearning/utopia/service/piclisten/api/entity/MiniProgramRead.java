package com.voxlearning.utopia.service.piclisten.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 小程序点读
 * @author ra
 */
@Data
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-xcx")
@DocumentCollection(collection = "miniprogram_read")
@DocumentIndexes({
        @DocumentIndex(def = "{'uid':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class MiniProgramRead implements Serializable {
    private static final long serialVersionUID = -154402381579037097L;

    @DocumentId
    private String id;

    private Long uid;  // 孩子Id
    private Long pid;  // 家长Id

    private Integer readWords; // 点读句子

    private Long readTimes; // 点读时间(存毫秒，返回分钟)

    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;

    @DocumentRevision
    private Long version;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MiniProgramRead.class, id);
    }

    public static String ck_uid(Long uid) {
        return CacheKeyGenerator.generateCacheKey(MiniProgramRead.class, "UID", uid);
    }



}
