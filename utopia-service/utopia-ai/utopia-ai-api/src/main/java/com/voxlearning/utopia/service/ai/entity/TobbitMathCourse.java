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
@DocumentCollection(collection = "tobbit_math_course")

@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TobbitMathCourse implements Serializable {

    private static final long serialVersionUID = -154402381579037097L;

    @DocumentId
    private String id;


    private String name; // 课程名称

    private String cover;  // 封面

    private Integer seq; // 序号

    private String videoUrl; // 视频地址

    private String keyPoint; // 知识点

    private Integer credit; // 需要的积分

    private Boolean trail; // 试用

    private Boolean disabled;


    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;

    @DocumentRevision
    private Long version;


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TobbitMathCourse.class, id);
    }


}
