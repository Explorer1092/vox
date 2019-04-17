package com.voxlearning.utopia.agent.persist.entity.daily;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * AgentDailyScoreStatistics
 *
 * @author deliang.che
 * @since  2018/11/23
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_daily_score_statistics")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181123")
public class AgentDailyScoreStatistics implements CacheDimensionDocument {

    private static final long serialVersionUID = 3735453666463248000L;
    @DocumentId
    private String id;
    private Integer date;                            // 格式： 20181123    dateType=2时 date为周一所在的日期（20181119）， dataType=3时date为当月的第一天（20181101）
    private Integer dateType;                        // 日期类型  1： 日  2：周  3：月

    private Integer groupOrUser;                     // 团队，个人   1：团队  2：个人

    private Long userId;
    private String userName;                         // 用户名

    private Long groupId;                            // 部门ID
    private String groupName;                        // 部门名称

    private Long parentGroupId;                      // 上级部门ID
    private String parentGroupName;                  // 上级部门名称

    private Double dailyScore;                       //日报得分

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;


    public AgentDailyScoreStatistics(Integer date, Integer dateType, Integer groupOrUser, Long groupId, String groupName){
        this.date = date;
        this.dateType = dateType;
        this.groupOrUser = groupOrUser;
        this.groupId = groupId;
        this.groupName = groupName;
        this.disabled = false;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"gid", "d","dt"}, new Object[]{groupId, date,dateType}),
                newCacheKey(new String[]{"uid", "d","dt"}, new Object[]{userId, date,dateType})
        };
    }
}
