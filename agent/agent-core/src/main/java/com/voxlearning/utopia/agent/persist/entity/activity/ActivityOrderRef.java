package com.voxlearning.utopia.agent.persist.entity.activity;

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
 * 保存订单对应的活动和专员信息（保存mq消息）
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_activity_order_ref")
public class ActivityOrderRef implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private String activityId;              // 对应的活动ID

    private String orderId;
    private Date orderPayTime;              // 订单支付时间
    private Long orderUserId;
    private Integer layer;                  // 链式推广传播的层数
    private Long userId;                    // 市场人员ID

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("oid", this.orderId)
        };
    }
}
