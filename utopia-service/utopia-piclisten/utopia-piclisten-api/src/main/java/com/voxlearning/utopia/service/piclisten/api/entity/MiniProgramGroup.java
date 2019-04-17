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
 * 小程序群关系
 * @author ra
 */
@Data
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-xcx")
@DocumentCollection(collection = "miniprogram_group")
@DocumentIndexes({
        @DocumentIndex(def = "{'gid':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class MiniProgramGroup implements Serializable {
    private static final long serialVersionUID = -154402381579037097L;

    @DocumentId
    private String id;

    private Long uid;  // 孩子Id
    private Long pid; // 家长Id
    private String gid;  // 微信群Id


    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;



    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MiniProgramGroup.class, id);
    }

    public static String ck_uid(Long uid) {
        return CacheKeyGenerator.generateCacheKey(MiniProgramGroup.class, "UID", uid);
    }
    public static String ck_gid(String gid) {
        return CacheKeyGenerator.generateCacheKey(MiniProgramGroup.class, "GID", gid);
    }

}
