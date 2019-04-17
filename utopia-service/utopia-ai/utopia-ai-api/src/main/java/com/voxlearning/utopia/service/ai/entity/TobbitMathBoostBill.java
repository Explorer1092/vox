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
@DocumentCollection(collection = "tobbit_math_boost_bill")

@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TobbitMathBoostBill implements Serializable {

    private static final long serialVersionUID = -154402381579037097L;

    @DocumentId
    private String id;


    private Long uid;
    private String openId; // 发起者openId


    private String aid; // 地址Id

    private String bid; // book id

    private Integer status; // 0: 等待助力/助力过期, 1: 助力成功


    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentRevision
    private Long version;


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathBoostBill.class, id);
    }

    public static String ck_uid(Long uid) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathBoostBill.class, "UID", uid);
    }

    public static String ck_openId(String openid) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathBoostBill.class, "OPENID", openid);
    }


}
