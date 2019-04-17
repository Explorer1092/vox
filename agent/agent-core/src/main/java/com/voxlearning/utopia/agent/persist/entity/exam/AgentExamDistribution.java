/**
 * Author:   xianlong.zhang
 * Date:     2018/9/14 17:53
 * Description: 大考分配情况
 * History:
 */
package com.voxlearning.utopia.agent.persist.entity.exam;

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

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_exam_distribution")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180921")
public class AgentExamDistribution implements CacheDimensionDocument {
    @DocumentId
    private String id;
    private String examId;
    private String name;
    private Integer grade;
    private Integer regionCode;
    private Long schoolId;
//    private List<AgentExamUserInfo> examUserInfos;
    private Long agentId;//代理id
    private String agentName;//代理id
    private Boolean evaluateState;    // 是否评价
    private Boolean distributionState; //是否分配  分配完后更新下这个数据
    private Boolean disabled;
    private Date examCreateDate;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    private Integer scanType; // 1 暂未标记 2 集中扫描 3 分开扫描
    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("examId",examId)
        };
    }
}
