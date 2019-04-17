package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 大区寄语
 *
 * @author song.wang
 * @date 2016/7/27
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_region_message")
public class AgentRegionMessage implements Serializable {

    @DocumentId
    private String id;
    private Long groupId;
    private String message;
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    public static String ck_gid(Long groupId) {
        return CacheKeyGenerator.generateCacheKey(AgentRegionMessage.class,
                new String[]{"gid"},
                new Object[]{groupId});
    }

}
