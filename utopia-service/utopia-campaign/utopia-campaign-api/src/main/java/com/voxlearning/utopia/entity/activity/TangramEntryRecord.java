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
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 七巧板活动参赛记录
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-activity")
@DocumentCollection(collection = "vox_student_tangram_record{}", dynamic = true)
@UtopiaCacheRevision("20180329")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TangramEntryRecord implements CacheDimensionDocument{

    private static final long serialVersionUID = -4775869009469002151L;

    @DocumentId
    private String id;                      // ID
    private String activityCode;            // 活动代码， 区分不同活动的数据
    private Long userId;                    // 用户ID
    private Long startTime;                 // 上次参加比赛的开始时间
    //private BitSet leftPuzzles;           // 120题中未完成的迷题
    private long[] puzzles;                 // 120题中未完成的迷题
    private Map<Long,Integer> scoreMap;     // 分数表(每天的)

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"USER_ID", "CODE"}, new Object[]{userId, activityCode})
        };
    }

    public static String ckUserCode(Long userId, String code) {
        return CacheKeyGenerator.generateCacheKey(TangramEntryRecord.class, new String[]{"USER_ID", "CODE"}, new Object[]{userId, code});
    }

    public TangramEntryRecord generateId() {
        id = ActivityShardingUtils.generateId(activityCode);
        return this;
    }

    public void registerScore(Date date,int score){
        if(date == null || score == 0) return;
        if(scoreMap == null) scoreMap = new HashMap<>();

        // 某一天的数据都累计在答对的第一题的时间点上
        DayRange thatDay = DayRange.newInstance(date.getTime());
        AtomicBoolean exists = new AtomicBoolean(false);
        scoreMap.forEach((d,orgScore) -> {
            if(thatDay.contains(d)){
                exists.set(true);
                scoreMap.put(d, orgScore + score);
            }
        });

        if(!exists.get()){
            scoreMap.put(date.getTime(), score);
        }
    }
}
