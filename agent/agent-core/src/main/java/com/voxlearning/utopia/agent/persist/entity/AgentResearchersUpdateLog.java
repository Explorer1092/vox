package com.voxlearning.utopia.agent.persist.entity;

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
import java.util.Map;

/**
 * AgentUserResearchers
 *
 * @author xianlong.zhang
 * @date 2018/8/3
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_researchers_user_update_log")
@UtopiaCacheRevision("20180803")
public class AgentResearchersUpdateLog implements CacheDimensionDocument {
    @DocumentId
    private String id;

    private Long researchersId;//教研员id
    private Long userId;//修改人id
    private String updateName;//修改人id
    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    List<Map<String,Object>> updateItems;
    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("researchersId", this.researchersId)
        };
    }
}
