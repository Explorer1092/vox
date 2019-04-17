package com.voxlearning.utopia.service.mizar.api.entity.shop;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Summer Yang on 2016/9/6.
 * 机构收集评论活动 投票详情
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_o2o")
@DocumentCollection(collection = "vox_shop_like")
@DocumentIndexes({
        @DocumentIndex(def = "{'shop_id':1, 'activity_id':1}", background = true),
        @DocumentIndex(def = "{'user_id':1}", background = true),
        @DocumentIndex(def = "{'activity_id':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160919")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarShopLike implements Serializable {

    private static final long serialVersionUID = 4754731801435187705L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    private String id;
    @DocumentField("shop_id") private String shopId;                 // 机构ID
    @DocumentField("shop_name") private String shopName;             // 机构名称
    @DocumentField("first_category") private String firstCategory;   // 一级分类
    @DocumentField("user_id") private Long userId;                   // 用户ID
    @DocumentField("activity_id") private Integer activityId;        // 活动ID   @Enum MizarRatingActivity

    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(MizarShopLike.class, "userId", userId);
    }

    public static String ck_activityId(Integer activityId) {
        return CacheKeyGenerator.generateCacheKey(MizarShopLike.class, "activityId", activityId);
    }

    public static String ck_shopIdAndActivityId(String shopId, Integer activityId) {
        return CacheKeyGenerator.generateCacheKey(MizarShopLike.class,
                new String[]{"shopId", "activityId"},
                new Object[]{shopId, activityId});
    }
}
