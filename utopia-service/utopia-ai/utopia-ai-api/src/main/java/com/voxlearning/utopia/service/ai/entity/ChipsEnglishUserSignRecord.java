package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户签到表
 */

@Getter
@Setter
@ToString
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_english_user_sign_record")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180928")
public class ChipsEnglishUserSignRecord implements Serializable {
    private static final long serialVersionUID = 2109321088931279169L;

    @DocumentId
    private String id;
    private Long userId;
    private String unitId;
    private String bookId;
    private Boolean current; //是否是当天打卡
    @DocumentCreateTimestamp
    private Date createTime;

    //缓存key
    public static String ck_user_id(Long userId) {
        return CacheKeyGenerator.generateCacheKey(ChipsEnglishUserSignRecord.class, "UID", userId);
    }
}
