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
import java.util.List;

/**
 * AgentSelfHelp
 *
 * @author song.wang
 * @date 2018/6/7
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_self_help")
@UtopiaCacheRevision("20180607")
public class AgentSelfHelp implements CacheDimensionDocument {
    private static final long serialVersionUID = -8937862385672264160L;

    @DocumentId
    private String id;

    private String typeId;                // 类型
    private String title;                         // 事项
    private String contact;                       // 联系人
    private String email;                         // 邮箱
    private String wechatGroup;                   // 微信群
    private String comment;                       // 说明

    private List<String> contentPacketIds;        // 关联的资料包ID

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(this.id),                 // 在ID上创建索引
                newCacheKey("type", this.typeId),
                newCacheKey("all")
        };
    }
}
