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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * AgentWeekly
 *
 * @author song.wang
 * @date 2016/8/11
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_weekly")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160902")
public class AgentWeekly implements Serializable {
    private static final long serialVersionUID = 4576034398288234264L;
    @DocumentId
    private String id;
    private Integer day;
    private Long userId;
    private String userName;
    private Long groupId;
    private String groupName;
    private String title; //
    private Double juniorSascFloat; // 小学单活涨幅(比率)
    private Double juniorDascFloat; // 小学双活涨幅(比率)
    private Double middleSascFloat; // 中学单活涨幅(比率)
    private Double juniorSascCompleteRate; // 小学单活完成率
    private Double juniorDascCompleteRate; // 小学双活完成率
    private Double middleSascCompleteRate; // 中学单活完成率

    private Integer ranking; // 排名
    private Integer preWeekRanking;// 上周排名

    private List<AgentWeekSubordinateData> subordinateDataList; // 下属的数据列表

    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    public static String ck_uid_day(Long userId, Integer day) {
        return CacheKeyGenerator.generateCacheKey(AgentWeekly.class,
                new String[]{"uid", "day"},
                new Object[]{userId, day});
    }

}
