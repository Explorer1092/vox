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
 * on 2018/7/16.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_message")
@UtopiaCacheRevision("20180716")
public class AgentMessage implements CacheDimensionDocument {
    @DocumentId
    private String id;

//    private Integer messageType; //消息类型 1 push消息 2 系统消息
    private String pushContent;//push内容
    private Integer expireTime; //过期时间  （push消息在极光的有效期 超过多久未发送失效）

    private String linkUrl; //跳转链接地址
    private Integer sendRange;// 1 指定部门 2 指定用户

    private Long createUserId; //创建人Id
    private String createUserName; //创建人
    private Integer msgStatus; //0 草稿 1 已发送 -1 已删除
    private Date sendDatetime; //发送时间
    private Integer sendNum; //发送量
    private Integer openNum; //打开量

    private Boolean disabled;
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
