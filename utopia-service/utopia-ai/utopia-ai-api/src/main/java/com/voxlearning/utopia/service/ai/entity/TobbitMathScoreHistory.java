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
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-xcx")
@DocumentCollection(collection = "tobbit_math_score_history")
@DocumentIndexes({
        @DocumentIndex(def = "{'uid:1','cid:1'}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TobbitMathScoreHistory implements Serializable {

    private static final long serialVersionUID = -154402381579037097L;

    @DocumentId
    private String id;


    private Long uid; // 用户Id

    private Long score;  // 积分

    private Long totalScore; // 总积分

    private Integer type; // 类型 TobbitScoreType.type

    private String cid; // 课程ID

    private String[] ext;

    private Boolean disabled;


    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;

    @DocumentRevision
    private Long version;


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathScoreHistory.class, id);
    }

    public static String ck_uid(Long uid) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathScoreHistory.class, "UID", uid);
    }

    public static String ck_cid(String cid) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathScoreHistory.class, "CID", cid);
    }

}
