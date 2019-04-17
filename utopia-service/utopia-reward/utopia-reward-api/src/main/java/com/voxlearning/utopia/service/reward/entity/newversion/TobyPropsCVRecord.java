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
@DocumentCollection(collection = "vox_toby_props_cv_record")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180730")
public class TobyPropsCVRecord implements Serializable {
    @DocumentId
    private String id;
    private Long userId;
    private Long propsId;
    private Integer status;
    private Long expiryTime;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(TobyPropsCVRecord.class, "USER_ID", userId);
    }

    public static String ck_propsId(Long propsId) {
        return CacheKeyGenerator.generateCacheKey(TobyPropsCVRecord.class, "PROPS_ID", propsId);
    }

    public enum Status{
        DEFAULT(0),
        OWNED(1),
        USING(2);
        private int status;
        Status(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    public static TobyPropsCVRecord build (long id, long userId, long activeTimeMs){
        TobyPropsCVRecord record = new TobyPropsCVRecord();
        record.setPropsId(id);
        record.setUserId(userId);
        record.setStatus(TobyPropsCVRecord.Status.USING.getStatus());
        record.setExpiryTime(activeTimeMs);
        return record;
    }
}
