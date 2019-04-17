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
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-reward")
@DocumentCollection(collection = "vox_toby")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180726")
public class RewardCenterToby implements Serializable {
    @DocumentId
    private String id;
    private Long userId;
    private Long imageId;
    private String imageName;
    private Long countenanceId;
    private Long propsId;
    private String imageUrl;
    private String countenanceUrl;
    private String propsUrl;
    private Long accessoryId;
    private String accessoryUrl;
    private Long accessoryExpiryTimeStamp;
    private Long countenanceExpiryTimeStamp;
    private Long imageExpiryTimeStamp;
    private Long propsExpiryTimeStamp;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    public Boolean isExpiry(Long expiryTimeStamp) {
        if (expiryTimeStamp==null || (expiryTimeStamp>0 && expiryTimeStamp<new Date().getTime())) {
            return true;
        }
        return false;
    }

    public static RewardCenterToby defaultBean(long userId) {
        RewardCenterToby myToby = new RewardCenterToby();
        myToby.setUserId(userId);

        myToby.setImageId(0l);
        myToby.setImageName("托比");
        myToby.setImageExpiryTimeStamp(Long.MAX_VALUE);

        return myToby;
    }

    public static void defaultImage(RewardCenterToby myToby) {
        myToby.setImageUrl(StringUtils.EMPTY);
        myToby.setImageId(0l);
        myToby.setImageName("托比");
        myToby.setImageExpiryTimeStamp(0L);
    }

    public static void defaultCountenance(RewardCenterToby myToby) {
        myToby.setCountenanceUrl(StringUtils.EMPTY);
        myToby.setCountenanceId(0l);
        myToby.setCountenanceExpiryTimeStamp(0L);
    }

    public static void defaultProps(RewardCenterToby myToby) {
        myToby.setPropsUrl(StringUtils.EMPTY);
        myToby.setPropsId(0l);
        myToby.setPropsExpiryTimeStamp(0L);
    }

    public static void defaultAccessory(RewardCenterToby myToby) {
        myToby.setAccessoryUrl(StringUtils.EMPTY);
        myToby.setAccessoryId(0l);
        myToby.setAccessoryExpiryTimeStamp(0L);
    }

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(RewardCenterToby.class, "UID", userId);
    }

}
