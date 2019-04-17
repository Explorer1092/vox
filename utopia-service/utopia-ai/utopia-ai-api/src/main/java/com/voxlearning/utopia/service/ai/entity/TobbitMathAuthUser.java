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
@DocumentCollection(collection = "tobbit_math_auth_user")
@DocumentIndexes({
        @DocumentIndex(def = "{'openId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TobbitMathAuthUser implements Serializable {

    private static final long serialVersionUID = -154402381579037097L;

    @DocumentId
    private String id;

    private String openId;  // 用户OPENID
    private String name;
    private String avatar;


    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;

    @DocumentRevision
    private Long version;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathAuthUser.class, id);
    }


    public static String ck_openId(String openid) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathAuthUser.class, "OPENID", openid);
    }



}
