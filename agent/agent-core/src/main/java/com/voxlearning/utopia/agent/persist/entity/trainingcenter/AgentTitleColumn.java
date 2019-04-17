package com.voxlearning.utopia.agent.persist.entity.trainingcenter;

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
 * 培训中心栏目管理实体
 * @author xianlong.zhang
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_title_column")
@UtopiaCacheRevision("20180706")
public class AgentTitleColumn implements CacheDimensionDocument {

    private static final long serialVersionUID = -7546124065975914090L;
    @DocumentId
    private String id;
    private String name;
    private Integer level;//栏目级别  1 一级 2 二级
    private String parentId;//父级栏目ID
    private Integer sortId;     // 排序ID
    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(this.id),                 // 在ID上创建索引
                newCacheKey("pid",this.parentId),                 // 在ID上创建索引
                newCacheKey("all")
        };
    }
}
