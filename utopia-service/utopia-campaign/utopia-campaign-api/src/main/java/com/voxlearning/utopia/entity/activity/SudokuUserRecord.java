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
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.LinkedList;

/**
 * 数独用户参与记录表
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-activity")
@DocumentCollection(collection = "vox_activity_sudoku_user{}", dynamic = true)
@UtopiaCacheRevision("20181102")
@DocumentIndexes({
        @DocumentIndex(def = "{'userId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class SudokuUserRecord implements CacheDimensionDocument {

    private static final long serialVersionUID = -4775869009469002151L;

    @DocumentId
    private String id;
    private String activityId;                              // 活动 ID
    private String curDate;                                 // 参与日期
    private Long userId;                                    // 用户 ID
    private Integer countdown;                              // 当日答题还剩多久(得分模式下判断是否允许重新进入)
    private Date beginTime;                                 // 开始时间
    private Date endTime;                                   // 完全结束时间 (所有题都答完的那个时间)
    private Integer correctCount;                           // 已经答对的题目数量
    private LinkedList<QuestionTime> times;                 // 每道题的时间统计
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                ckActivityIdUserId(activityId, userId)
        };
    }

    public static String ckActivityIdUserId(String activityId,Long userId) {
        return CacheKeyGenerator.generateCacheKey(SudokuUserRecord.class, new String[]{"AID", "UID"}, new Object[]{activityId, userId});
    }

    public SudokuUserRecord generateId() {
        id = ActivityShardingUtils.generateId(activityId);
        return this;
    }

    @Getter
    @Setter
    public static class QuestionTime implements java.io.Serializable {
        public QuestionTime() {
        }

        public QuestionTime(Date beginTime) {
            this.beginTime = beginTime;
        }

        private Date beginTime;  // 开始时间
        private Date endTime;    // 结束时间
        private String time;     // 耗时(前端有暂停功能,不能直接减,取前端传入的耗时)
    }
}
