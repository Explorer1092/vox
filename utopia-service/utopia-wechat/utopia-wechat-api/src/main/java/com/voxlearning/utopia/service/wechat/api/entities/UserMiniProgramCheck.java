package com.voxlearning.utopia.service.wechat.api.entities;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 小程序打卡
 */
@Data
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-xcx")
@DocumentCollection(collection = "mini_program_check")
@DocumentIndexes({
        @DocumentIndex(def = "{'uid':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class UserMiniProgramCheck implements Serializable {
    private static final long serialVersionUID = -154402381579037097L;

    @DocumentId
    private String id;

    private Long uid;  // 用户Id

    private Integer checking; // 连续打卡次数

    private Integer checked; // 总打卡次数

    private MiniProgramType type;

    @DocumentCreateTimestamp
    private Date createTime;


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(UserMiniProgramCheck.class, id);
    }

    public static String ck_uid(Long uid) {
        return CacheKeyGenerator.generateCacheKey(UserMiniProgramCheck.class, "UID", uid);
    }

    public static String ck_type(MiniProgramType type) {
        return CacheKeyGenerator.generateCacheKey(UserMiniProgramCheck.class, "TYPE", type);
    }


    // !!!Notice
    // Please implement concurrency issues
    public void incrChecked() {
        checked++;
    }
    public void increChecking() {
        checking++;
    }
}
