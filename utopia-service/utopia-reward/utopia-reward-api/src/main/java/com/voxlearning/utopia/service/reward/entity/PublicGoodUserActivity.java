package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

/**
 * 公益活动 - 收集实体
 *
 * @author haitian.gan
 */
@Getter
@Setter
@EqualsAndHashCode(of = {"userId", "activityId"})
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-public-good")
@DocumentCollection(collection = "vox_public_good_user_activity_{}", dynamic = true)
@UtopiaCacheRevision("20180720")
public class PublicGoodUserActivity implements CacheDimensionDocument {

    private static final long serialVersionUID = 7426131009686814378L;

    @DocumentId
    private String id;
    private Long userId;
    private Long activityId;
    private Long likeNum;
    private Long moneyNum;
    private Boolean guide;
    private Set<Long> likedUser; // 给谁点过赞

    @DocumentCreateTimestamp
    private Long createTime;
    @DocumentUpdateTimestamp
    private Long updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                "UID", String.valueOf(this.userId)
        };
    }

    public PublicGoodUserActivity generateId() {
        Objects.requireNonNull(userId);
        id = userId + "-" + RandomUtils.nextObjectId();
        return this;
    }
}
