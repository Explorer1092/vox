package com.voxlearning.utopia.agent.persist.entity.honeycomb;

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
 * 保存蜂巢的用户, 表中数据主要为市场人员对应的蜂巢用户ID , 及市场人员在蜂巢的直接粉丝， 其他蜂巢用户数据不关注
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_honeycomb_user")
public class HoneycombUser implements CacheDimensionDocument {

    @DocumentId
    private Long id;                      // 蜂巢用户ID

    private Long agentUserId;             // 对应的天玑账号ID

//    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(this.id),
                newCacheKey("uid", this.agentUserId)
        };
    }
}
