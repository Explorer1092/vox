package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动排行榜点赞
 * @author chensn
 * @date 2018-10-30 16:02
 */
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_activity_rank_like_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
@UtopiaCacheRevision("20181030")
public class ClazzActivityRankLikeRecord implements Serializable {

    private static final long serialVersionUID = 9032105696360538230L;
    /**
     * userId_activityId_rankType_toId
     */
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    private Integer activityId;
    private Integer rankType;
    private String toObjectId;
    private Long userId;
    private String dailyString;
    @DocumentCreateTimestamp
    private Date ct;

    public void generateId() {
        if (StringUtils.isNoneBlank(dailyString)) {
            this.id = userId + "_" + activityId + "_" + rankType + "_" + toObjectId + "_" + dailyString;
        } else {
            this.id = userId + "_" + activityId + "_" + rankType + "_" + toObjectId;
        }

    }

    public static String generateId(Long userId, Integer activityId, Integer rankType, String toObjectId, String dailyString) {
        if (StringUtils.isNoneBlank(dailyString)) {
            return userId + "_" + activityId + "_" + rankType + "_" + toObjectId + "_" + dailyString;
        } else {
            return userId + "_" + activityId + "_" + rankType + "_" + toObjectId;
        }
    }

    public static String ck_userId_actityId(Long userId, Integer activityId) {
        return CacheKeyGenerator.generateCacheKey(ClazzActivityRankLikeRecord.class, new String[]{"userId", "activityId"}, new Object[]{userId, activityId});
    }
}
