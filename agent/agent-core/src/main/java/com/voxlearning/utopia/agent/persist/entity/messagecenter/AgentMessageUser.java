package com.voxlearning.utopia.agent.persist.entity.messagecenter;

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
 * Created by xianlong.zhang
 * on 2018/7/18.
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_message_user")
@UtopiaCacheRevision("20180716")
public class AgentMessageUser implements CacheDimensionDocument {
    @DocumentId
    private String id;
    private String messageId;           //对应push消息的id
    private Long userId;                //接收人id
    private Boolean readFlag;           // 阅读状态0:未阅 1:已阅
    private Boolean disabled;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
            newCacheKey(this.id),
            newCacheKey(new String[]{"userId", "messageId"}, new Object[]{userId, messageId}),
            newCacheKey("messageId",this.messageId)
        };
    }
}
