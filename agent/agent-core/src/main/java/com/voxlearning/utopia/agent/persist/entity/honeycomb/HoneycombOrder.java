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

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_honeycomb_order")
public class HoneycombOrder implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private String activityId;              // 推广活动或商品ID

    private String orderId;                 // 订单ID
    private Date payTime;                   // 订单支付时间

    private Long honeycombId;              // 蜂巢用户ID

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
