package com.voxlearning.utopia.service.ai.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
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

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-xcx")
@DocumentCollection(collection = "tobbit_math_history")
@DocumentIndexes({
        @DocumentIndex(def = "{'openId':1,'uid:1'}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TobbitMathHistory implements Serializable {

    private static final long serialVersionUID = -154402381579037097L;

    @DocumentId
    private String id;

    private String openId;  // 用户OPENID

    private Long uid; // 用户Id

    private String img; // 图片

    private String json; // 标注

    private String originJson; // 原始标注

    private Integer errorCount; // 错题数
    private Integer totalCount; // 总题数

    private Boolean disabled;


    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;

    @DocumentRevision
    private Long version;



    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathHistory.class, id);
    }

    public static String ck_uid(Long uid) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathHistory.class, "UID", uid);
    }

    public static String ck_openId(String openId) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathHistory.class, "OPENID", openId);
    }




}
