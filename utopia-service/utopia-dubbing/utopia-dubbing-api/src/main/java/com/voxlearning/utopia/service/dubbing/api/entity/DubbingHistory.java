package com.voxlearning.utopia.service.dubbing.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Created by jiang wei on 2017/8/24.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "dubbing_history")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
@UtopiaCacheRevision("20180116")
public class DubbingHistory implements CacheDimensionDocument {

    private static final long serialVersionUID = -8600584258219495628L;
    @DocumentId
    private String id; //categoryId_userId_videoId_clazzId_ObjectId

    private String fixId;

    private Long userId;

    private String dubbingId;

    private String categoryId;

    private Long clazzId;

    private String videoUrl;

    private Boolean isPublished;

    private Boolean disabled;

    private Boolean isHomework; //是否是作业

    private String homeworkId;

    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id),
                newCacheKey("UID", userId),
                newCacheKey("UID_COUNT", userId),
                newCacheKey(new String[]{"UID", "CAID"}, new Object[]{userId, categoryId}),
                newCacheKey(new String[]{"UID", "DID"}, new Object[]{userId, dubbingId}),
                newCacheKey(new String[]{"CID", "DID"}, new Object[]{clazzId, dubbingId}),
                newCacheKey(new String[]{"UID", "DID", "HID"}, new Object[]{userId, dubbingId, homeworkId})
        };
    }


    public static String generateId(Long userId, String dubbingId, Long clazzId, String categoryId) {
        if (userId == null || userId == 0L || StringUtils.isBlank(dubbingId) || StringUtils.isBlank(categoryId)) {
            throw new RuntimeException("参数错误");
        }
        if (clazzId == null || clazzId == 0L) {
            return categoryId + "_" + userId + "_" + dubbingId + "_" + RandomUtils.nextObjectId();
        } else {
            return categoryId + "_" + userId + "_" + dubbingId + "_" + clazzId + "_" + RandomUtils.nextObjectId();
        }
    }

    public static String generateFixId(Long userId, String dubbingId) {
        if (userId == null || userId == 0L || StringUtils.isBlank(dubbingId)) {
            throw new RuntimeException("参数错误");
        }
        return userId + "_" + dubbingId + "_" + RandomUtils.nextObjectId();
    }
}
