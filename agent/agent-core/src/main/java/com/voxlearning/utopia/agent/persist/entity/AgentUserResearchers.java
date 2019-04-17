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
@DocumentCollection(collection = "agent_user_researchers")
@UtopiaCacheRevision("20180803")
public class AgentUserResearchers implements CacheDimensionDocument {
    @DocumentId
    private String id;

    private Long researchersId;//教研员id
    private Long userId;//专员id
//    private String researchersPhone;//教研员电话
    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("userId", this.userId),
                newCacheKey(new String[]{"userId", "researchersId"}, new Object[]{userId, researchersId}),
                newCacheKey( "researchersId", researchersId)
//                newCacheKey("all")
        };
    }
}
