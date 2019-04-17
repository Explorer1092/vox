package com.voxlearning.utopia.service.ai.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentRevision;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-xcx")
@DocumentCollection(collection = "tobbit_math_boost_history")

@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TobbitMathBoostHistory implements Serializable {

    private static final long serialVersionUID = -154402381579037097L;

    @DocumentId
    private String id;


    private String bid; // 助力对象 bill id

    private String openId;

    private String avatar; // 助力者头像

    private String name; // 助力者昵称


    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentRevision
    private Long version;


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathBoostHistory.class, id);
    }


    public static String ck_openId(String openid) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathBoostHistory.class, "OPENID", openid);
    }

    public static String ck_bid(String bid) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathBoostHistory.class, "BID", bid);
    }


}
