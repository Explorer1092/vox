package com.voxlearning.utopia.agent.persist.entity.selfhelp;

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
 * AgentSelfHelpType
 *
 * @author song.wang
 * @date 2018/6/7
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_self_help_type")
@UtopiaCacheRevision("20180615")
public class AgentSelfHelpType  implements CacheDimensionDocument {
    private static final long serialVersionUID = -8711271915008446426L;

    @DocumentId
    private String id;

    private String typeName;    // 类型名称
    private Integer sortId;     // 排序ID
    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("all")
        };
    }
}
