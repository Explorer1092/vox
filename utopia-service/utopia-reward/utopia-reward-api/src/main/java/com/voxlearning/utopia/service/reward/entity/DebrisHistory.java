package com.voxlearning.utopia.service.reward.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.ObjectIdEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_DEBRIS_HISTORY_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180831")
public class DebrisHistory extends ObjectIdEntity implements Serializable, CacheDimensionDocument {

    private static final long serialVersionUID = -2091074456187840950L;

    @DocumentField("USER_ID")
    private Long userId;                                        // 用户 id
    @DocumentField("DEBRIS_TYPE")
    private Integer debrisType;                                 // 积分类型
    @DocumentField("DEBRIS")
    private Long debris;                                        // 积分
    @DocumentField("COMMENT")
    private String comment;                                     // 备注
    @DocumentField("ADD_DEBRIS_USER_ID")
    private Long addDebrisUserId;                               // 添加积分人 id
    @DocumentField("TOTAL_DEBRIS_BEFORE")
    private Long totalDebrisBefore;                             // 变更前 total  debris
    @DocumentField("USABLE_DEBRIS_BEFORE")
    private Long usableDebrisBefore;                            // 变更前 usable debris
    @DocumentField("TOTAL_DEBRIS_AFTER")
    private Long totalDebrisAfter;                              // 变更后 total  debris
    @DocumentField("USABLE_DEBRIS_AFTER")
    private Long usableDebrisAfter;                             // 变更后 usable debris

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(DebrisHistory.class, "U", userId);
    }

    public DebrisHistory() {
    }

    public DebrisHistory(Long userId, Integer integralType) {
        this.userId = userId;
        this.debrisType = integralType;
    }

    public DebrisHistory(Long userId, DebrisType debrisType) {
        this(userId, debrisType.getType());
    }

    public DebrisHistory(Long userId, Integer debrisType, Long debris) {
        this.userId = userId;
        this.debrisType = debrisType;
        this.debris = debris;
    }

    public DebrisHistory(Long userId, DebrisType debrisType, Long integral) {
        this(userId, debrisType.getType(), integral);
    }

    public DebrisType toIntegralType() {
        return DebrisType.of(debrisType);
    }

    @JsonIgnore
    public long getDebrisValue() {
        return debris == null ? 0 : debris;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                ck_userId(userId)
        };
    }

}
