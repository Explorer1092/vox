package com.voxlearning.utopia.agent.persist.entity.publish;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * AgentPublishControl
 *
 * @author song.wang
 * @date 2018/4/23
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_publish_control")
public class AgentPublishControl implements CacheDimensionDocument {

    @DocumentId
    private String id;
    private String publishId;

    private Boolean isGrouped;                 // 是否分组
    private Boolean allowDownload;             // 允许下载
    private Boolean allowViewSubordinateData;  // 运行查看下属数据
    private List<AgentRoleType> roleTypeList;  // 角色列表
    private List<Long> groupIdList;            // 部门列表

    private Boolean disabled;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("pid", this.publishId)
        };
    }
}

