package com.voxlearning.utopia.service.piclisten.api.entity;

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
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 小程序打卡
 * @author ra
 */
@Data
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-xcx")
@DocumentCollection(collection = "miniprogram_check")
@DocumentIndexes({
        @DocumentIndex(def = "{'uid':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class MiniProgramCheck implements Serializable {
    private static final long serialVersionUID = -154402381579037097L;

    @DocumentId
    private String id;

    private Long uid;  // 孩子Id
    private Long pid;  // 家长Id

    private Integer checking; // 连续打卡次数

    private Integer checked; // 总打卡次数

    @DocumentCreateTimestamp
    private Date createTime;


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MiniProgramCheck.class, id);
    }

    public static String ck_uid(Long uid) {
        return CacheKeyGenerator.generateCacheKey(MiniProgramCheck.class, "UID", uid);
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
