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
import com.voxlearning.utopia.entity.crm.constants.UserPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentPerformanceGoalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Agent业绩目标服务记录
 * @author chunlin.yu
 * @create 2017-10-30 19:42
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_performance_service_record")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20171030")
public class AgentPerformanceServiceRecord  implements CacheDimensionDocument {

    private static final long serialVersionUID = -5876162251865518687L;
    @DocumentId
    private String id;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    private Boolean disabled;

    private Long targetId;

    private String targetName;

    private Integer month;

    private AgentPerformanceGoalType agentPerformanceGoalType;

    private Long operatorId;
    private String operatorName;
    private UserPlatformType userPlatformType;
    /**
     * 版更内容
     */
    private String content;
    /**
     * 补充说明
     */
    private String additions;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"m", "t", "a"}, new Object[]{month, targetId, agentPerformanceGoalType})
        };
    }
}
