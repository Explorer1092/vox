package com.voxlearning.utopia.service.ai.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.*;
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
@DocumentCollection(collection = "tobbit_math_address")

@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TobbitMathAddress implements Serializable {

    private static final long serialVersionUID = -154402381579037097L;

    @DocumentId
    private String id;


    private Long uid;

    private String openId;

    private String name;

    private String tel; // 联系方式

    private String city; // 省/市

    private String addr; // 详细地址

    private Boolean disabled;


    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;

    @DocumentRevision
    private Long version;


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathAddress.class, id);
    }

    public static String ck_uid(Long uid) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathAddress.class, "UID", uid);
    }

    public static String ck_openId(String openid) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathAddress.class, "OPENID", openid);
    }


}
