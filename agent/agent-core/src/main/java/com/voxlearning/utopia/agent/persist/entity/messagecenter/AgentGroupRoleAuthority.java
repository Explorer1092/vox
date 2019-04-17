package com.voxlearning.utopia.agent.persist.entity.messagecenter;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.agent.constants.AgentAuthorityType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 选择部门角色的做成一个公共的模块  再做相似的有选择角色 部门的都存到这个表
 * Created by xianlong.zhang
 * on 2018/7/16.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_group_role_authority")
@UtopiaCacheRevision("20180716")
public class AgentGroupRoleAuthority implements CacheDimensionDocument {
    @DocumentId
    private String id;
    private String sourceId;  //来源id 如果 AgentAuthorityType类型为 MESSAGE 则对应的sourceId为 AgentMessage的id
    private List<AgentRoleType> roleTypeList;  // 发布角色列表
    private List<Long> groupIdList;            // 发布部门列表
    private List<Long> userIds;
    private AgentAuthorityType agentAuthorityType;
    private Boolean disabled;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(this.id),
                newCacheKey("sourceId",this.sourceId)
        };
    }
}
