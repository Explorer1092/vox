package com.voxlearning.utopia.service.ai.entity;

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


@Data
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-xcx")
@DocumentCollection(collection = "tobbit_math_share_history")
@DocumentIndexes({
        @DocumentIndex(def = "{'openId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TobbitMathShareHistory implements Serializable {

    private static final long serialVersionUID = -154402381579037097L;

    @DocumentId
    private String id;

    private String openId;  // 用户OPENID
    private Long uid;  // 用户Id

    private String qid; // 扫描题目id

    @DocumentCreateTimestamp
    private Date createTime;



    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathShareHistory.class, id);
    }

    public static String ck_uid(Long uid) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathShareHistory.class, "UID", uid);
    }

    public static String ck_openId(String openid) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathShareHistory.class, "OPENID", openid);
    }



}
