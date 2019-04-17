package com.voxlearning.utopia.agent.persist.entity.tag;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.agent.constants.AgentTagTargetType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 标签与服务对象关系
 *
 * @author deliang.che
 * @since  2019/3/21
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_tag_target")
public class AgentTagTarget implements CacheDimensionDocument{

    private static final long serialVersionUID = -2043162051287689091L;
    @DocumentId
    private String id;

    private Long tagId;
    private AgentTagTargetType targetType;      // 标签关联对象的类型
    private String targetId;

    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("tagId", this.tagId),
                newCacheKey(new String[]{"targetId","targetType"}, new Object[]{targetId,targetType})
        };
    }
}
