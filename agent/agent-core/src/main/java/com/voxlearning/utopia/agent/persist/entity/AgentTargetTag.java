package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.agent.constants.AgentTag;
import com.voxlearning.utopia.agent.constants.AgentTargetType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2017/5/23
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_target_tag")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20170523")
public class AgentTargetTag implements CacheDimensionDocument{

    private static final long serialVersionUID = 5454980263348861029L;

    @DocumentId
    private String id;

    @DocumentField private AgentTargetType targetType;      // 标签关联对象的类型
    @DocumentField private Long targetId;
    @DocumentField private List<AgentTag> tags;

    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"TID", "TYPE"}, new Object[]{targetId, targetType})
        };
    }

    public boolean hasTag(AgentTag tag) {
        return CollectionUtils.isNotEmpty(this.tags) && this.tags.contains(tag);
    }

    public boolean addTag(AgentTag tag){
        if(!hasTag(tag)){
            if(this.tags == null){
                this.tags = new ArrayList<>();
            }
            tags.add(tag);
            return true;
        }
        return false;
    }


}
