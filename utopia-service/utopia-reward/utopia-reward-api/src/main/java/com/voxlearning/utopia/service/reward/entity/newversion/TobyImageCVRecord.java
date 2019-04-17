package com.voxlearning.utopia.service.reward.entity.newversion;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-reward")
@DocumentCollection(collection = "vox_toby_image_cv_record")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180730")
public class TobyImageCVRecord implements Serializable {
    @DocumentId
    private String id;
    private Long userId;
    private Long imageId;
    private Integer status;
    private Long expiryTime;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(TobyImageCVRecord.class, "USER_ID", userId);
    }

    public static String ck_imageId(Long imageId) {
        return CacheKeyGenerator.generateCacheKey(TobyImageCVRecord.class, "IMAGE_ID", imageId);
    }

    public enum Status{
        DEFAULT(0),
        OWNED(1),
        USING(2),
        CANCEL(3),
            ;
        private int status;
        Status(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    public static TobyImageCVRecord build (long imgaeId, long userId, long activeTimeMs){
        TobyImageCVRecord record = new TobyImageCVRecord();
        record.setImageId(imgaeId);
        record.setUserId(userId);
        record.setStatus(TobyImageCVRecord.Status.USING.getStatus());
        record.setExpiryTime(activeTimeMs);
        return record;
    }
}
