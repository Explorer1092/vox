package com.voxlearning.utopia.service.crm.api.entities.agent;

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
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentPerformanceGoalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 天玑各业绩目标
 *
 * @author chunlin.yu
 * @create 2017-10-26 11:57
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_performance_goal")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20171201")
public class AgentPerformanceGoal implements CacheDimensionDocument {

    private static final long serialVersionUID = 2405585881191210629L;

    @DocumentId
    private String id;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    private Boolean disabled;

    /** 业绩目标类型 */
    private AgentPerformanceGoalType agentPerformanceGoalType;

    /** 大区ID */
    private Long regionGroupId;

    /** 分区ID */
    private Long subRegionGroupId;

    /** 用户ID */
    private Long userId;

    /** 业绩目标月份（格式：201709）*/
    private Integer month;

    /** 小单新增目标 */
    private Integer sglSubjIncGoal;

    /** 小单长回目标 */
    private Integer sglSubjLtBfGoal;

    /** 小单短回目标 */
    private Integer sglSubjStBfGoal;

    /** 业绩目标是否被确认 */
    private Boolean confirm;

    public int fetchSumGoal(){
        int sum = 0;
        if (null != sglSubjIncGoal){
            sum += sglSubjIncGoal;
        }
        if (null != sglSubjLtBfGoal){
            sum += sglSubjLtBfGoal;
        }
        if (null != sglSubjStBfGoal){
            sum += sglSubjStBfGoal;
        }
        return sum;
    }

    @Override
    public String[] generateCacheDimensions() {

        return new String[]{
                newCacheKey("m", month),
                newCacheKey(new String[]{"m", "c"}, new Object[]{month, confirm})
        };
    }
}
