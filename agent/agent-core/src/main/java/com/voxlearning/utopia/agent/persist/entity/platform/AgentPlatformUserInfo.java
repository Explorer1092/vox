package com.voxlearning.utopia.agent.persist.entity.platform;

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

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_platform_user_info")
public class AgentPlatformUserInfo implements CacheDimensionDocument {

    @DocumentId
    private Long id;                // 家长或老师ID
    private Date registerTime;      // 当前 ID 的注册时间

    private Boolean isParent;            // 是否是家长
    private Date beParentTime;           // 成为家长的时间
    private Date isParentUpdTime;        // 更新 isParent 字段的时间

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(this.id)
        };
    }
}
