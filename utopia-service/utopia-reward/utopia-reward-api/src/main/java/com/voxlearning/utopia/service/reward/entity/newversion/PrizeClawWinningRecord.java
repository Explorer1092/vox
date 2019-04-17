package com.voxlearning.utopia.service.reward.entity.newversion;

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

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-reward")
@DocumentCollection(collection = "vox_prize_claw_winning_record")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180726")
public class PrizeClawWinningRecord implements Serializable {
    @DocumentId
    private String id;
    private Long prizeClawId;
    private Boolean isPrize;
    private Long userId;
    private Integer consumeNum;
    private String prizeName;
    private Integer prizeType;
    private Long prize;
    private Integer site;

    @DocumentCreateTimestamp
    private Date createTime;

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(PrizeClawWinningRecord.class, "USER_ID", userId);
    }
}
