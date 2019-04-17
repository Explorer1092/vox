package com.voxlearning.utopia.agent.persist.entity;

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
import com.voxlearning.utopia.agent.utils.MathUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * AgentPerformanceRanking
 *
 * @author song.wang
 * @date 2016/7/18
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_performance_ranking")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180329")
public class AgentPerformanceRanking implements Serializable {
    private static final long serialVersionUID = 9163536592081439338L;
    @DocumentId
    private String id;
    private Integer type; // 排行榜类型： 1： 部门排行榜 2：市经理排行榜   3：专员排行榜
    private Integer day;    // 日期
    private Integer indicatorType;//指标类型；1：JUNIOR_REG，2：JUNIOR_INC，3：MIDDLE_ENGLISH_REG
    private Double indicatorValue;//指标的值
    private Integer rannkingDateType;//排行时间维度；1：日榜，2：月榜

    private Long userId; // 用户ID
    private String userName;//
    private Long groupId;//
    private String groupName;//
    private Integer ranking;// 排名
    private Integer rankingFloat;// 排名浮动
    private Integer totalCount; // 参与排名的总数
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    public static String ck_uid_type_day(Long userId, Integer type, Integer day) {
        return CacheKeyGenerator.generateCacheKey(AgentPerformanceRanking.class,
                new String[]{"uid", "type", "day"},
                new Object[]{userId, type, day});
    }

    public static String ck_type_day(Integer type, Integer day) {
        return  CacheKeyGenerator.generateCacheKey(AgentPerformanceRanking.class,
                new String[]{"type", "day"},
                new Object[]{type, day});
    }

    // 在排行榜中的位置（比例）
    public double calRankingRate(){
        return MathUtils.doubleDivide(this.ranking, this.totalCount);
    }




}
