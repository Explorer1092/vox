package com.voxlearning.utopia.entity.activity;

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
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.api.constant.QuestionType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 24点游戏参数记录
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-activity")
@DocumentCollection(collection = "vox_student_twofourpoint_record{}", dynamic = true)
@UtopiaCacheRevision("20180521")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TwoFourPointEntityRecord implements CacheDimensionDocument {

    private static final long serialVersionUID = -4775869009469002151L;

    @DocumentId
    private String id;                      // ID
    private String code;                      // 活动 code
    private Long userId;                    // 用户ID
    private Long startTime;                 // 上次参加比赛的开始时间
    private Map<Long, Integer> scoreMap;    // 分数表(每天的)
    private Map<QuestionType, Integer> questionTypeOffset; //题型偏移量
    private Long skipCount;
    private Long resetCount;

    @DocumentCreateTimestamp
    private Long createTime;
    @DocumentUpdateTimestamp
    private Long updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{newCacheKey(new String[]{"USER_ID","CODE"}, new Object[]{userId,code})};
    }

    public static String ckUserCode(Long userId, String code) {
        return CacheKeyGenerator.generateCacheKey(TwoFourPointEntityRecord.class, new String[]{"USER_ID", "CODE"}, new Object[]{userId, code});
    }

    public TwoFourPointEntityRecord generateId() {
        id = ActivityShardingUtils.generateId(code);
        return this;
    }

    public void registerScore(Date date, int score) {
        if (date == null || score == 0) return;
        if (scoreMap == null) scoreMap = new HashMap<>();

        // 某一天的数据都累计在答对的第一题的时间点上
        DayRange thatDay = DayRange.newInstance(date.getTime());
        AtomicBoolean exists = new AtomicBoolean(false);
        scoreMap.forEach((d, orgScore) -> {
            if (thatDay.contains(d)) {
                exists.set(true);
                scoreMap.put(d, orgScore + score);
            }
        });

        if (!exists.get()) {
            scoreMap.put(date.getTime(), score);
        }
    }
}