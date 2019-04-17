package com.voxlearning.utopia.service.newhomework.api.entity;

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
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 学生完成作业后在家长端可以领取的学豆奖励
 *
 * @author shiwe.liao
 * @since 2016-8-26
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "homework_finish_reward_parent")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
@UtopiaCacheRevision("20161104")
public class NewHomeworkFinishRewardInParentApp implements CacheDimensionDocument {


    private static final long serialVersionUID = -5236131905852320099L;
    @DocumentId
    private Long id;        //studentId
    private Map<String, RewardDetail> notReceivedRewardMap;     //可领取的奖励=key-homeworkId,value-奖励数量和奖励过期时间
    private Map<String, Integer> hadReceivedRewardMap;          //已领取的奖励=key-homeworkId,value-integralCount
    private Map<String, Integer> timeoutRewardMap;              //过期的学豆奖励=key-homeworkId,value-integralCount
    private Map<String,RewardDetail> changeGroupRewardMap;     //换班产生的学豆记录过期
    @DocumentCreateTimestamp
    private Date createTime;            //创建时间
    @DocumentUpdateTimestamp
    private Date updateTime;            //更新时间
    private Boolean disabled;           //是否删除

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(NewHomeworkFinishRewardInParentApp.class, id);
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class RewardDetail implements Serializable {

        private static final long serialVersionUID = 4099204434701207239L;

        private Integer rewardCount;
        private Date expire;
        private Long groupId;       //这个字段是后加的。
    }
}
