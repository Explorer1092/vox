package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_user_oral_test_schedule")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190312")
public class ChipsUserOralTestSchedule implements Serializable {

    private static final long serialVersionUID = 5685560169096425118L;
    public static final String SEP = "-";
    @DocumentId
    private String id;                 //userId-productId
    private Long userId;
    private String productId;
    private Long clazzId;
    private Date testBeginTime;
    private Date testEndTime;

    @DocumentUpdateTimestamp
    @DocumentField
    private Date updateTime;
    @DocumentCreateTimestamp
    @DocumentField
    private Date createTime;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(ChipsUserOralTestSchedule.class, id);
    }

    public static String ck_clazzId(Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(ChipsUserOralTestSchedule.class, "CID", clazzId);
    }

    public static String genId(Long userId, String productId) {
        return userId + SEP + productId;
    }
}
