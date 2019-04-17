package com.voxlearning.utopia.agent.persist.entity.authority;

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
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_record_authority")
@UtopiaCacheRevision("20190319")
public class AgentRecordAuthority implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private String recordId;
    private Integer recordType;                // AgentAuthorityType

    private List<Integer> roleIds;         // 发布角色列表
    private List<Long> groupIds;           // 发布部门列表
    private List<Long> userIds;

    private Integer rule;                    // 0： 部门和角色取交集，   1：部门和角色取并集，  部门和角色运算之后和userId取并集为拥有改记录的用户集

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"rid", "t"}, new Object[]{this.recordId, this.recordType}),
        };
    }
}
