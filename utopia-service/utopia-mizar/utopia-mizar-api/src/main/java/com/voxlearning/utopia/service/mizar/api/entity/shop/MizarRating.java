package com.voxlearning.utopia.service.mizar.api.entity.shop;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.mizar.api.constants.MizarRatingStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer Yang on 2016/8/15.
 * 机构导流——用户点评
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_o2o")
@DocumentCollection(collection = "vox_rating")
@DocumentIndexes({
        @DocumentIndex(def = "{'shop_id':1}", background = true),
        @DocumentIndex(def = "{'rating_time':1}", background = true),
        @DocumentIndex(def = "{'activity_id':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161020")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarRating implements Serializable {

    private static final long serialVersionUID = 4615347395681687181L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    private String id;
    @DocumentField("shop_id") private String shopId;                 // 机构ID
    @DocumentField("user_name") private String userName;             // 用户姓名
    @DocumentField("user_avatar") private String userAvatar;         // 用户头像
    @DocumentField("rating") private Integer rating;                 // 打分
    @DocumentField("rating_content") private String ratingContent;   // 评论文字
    @DocumentField("photo") private List<String> photo;              // 评论图片
    @DocumentField("cost") private Double cost;                      // 费用
    @DocumentField("rating_time") private Long ratingTime;           // 评论时间
    @DocumentField("activity_id") private Integer activityId;        // 评论收集ID @link MizarRatingActivity
    @DocumentField("good_rating") private Boolean goodRating;        // 是否优质评论
    @DocumentField("user_id") private Long userId;                   // 用户ID 老的评论没有用户ID
    @DocumentField("status") private String status;                  // 评论状态 @link MizarRatingStatus

    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

    public MizarRatingStatus fetchStatus() {
        return MizarRatingStatus.parse(status);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MizarRating.class, id);
    }

    public static String ck_shopId(String shopId) {
        return CacheKeyGenerator.generateCacheKey(MizarRating.class, "shopId", shopId);
    }

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(MizarRating.class, "userId", userId);
    }

    public static Map<String, Object> toRatingMap(MizarRating rating) {
        Map<String, Object> ratingMap = new HashMap<>();
        ratingMap.put("userName", rating.getUserName());
        ratingMap.put("avatar", rating.getUserAvatar());
        ratingMap.put("rating", rating.getRating());
        ratingMap.put("content", rating.getRatingContent());
        if (rating.getRatingTime() != null) {
            ratingMap.put("ratingDate", DateUtils.dateToString(new Date(rating.getRatingTime()), "yyyy年MM月dd日"));
        }
        ratingMap.put("photos", rating.getPhoto());
        ratingMap.put("cost", rating.getCost());
        return ratingMap;
    }

}

