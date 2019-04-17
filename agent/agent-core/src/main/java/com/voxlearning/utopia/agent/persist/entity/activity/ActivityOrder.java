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

import java.math.BigDecimal;
import java.util.Date;

/**
 * 保存活动产生的订单信息
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_activity_order")
public class ActivityOrder implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private String activityId;              // 对应的活动ID

    private String orderId;                 // 订单ID
    private Date orderPayTime;              // 订单支付时间
    private BigDecimal orderPayAmount;      // 订单支付金额
    private Long orderUserId;               // 下单的用户ID
    private Date orderUserRegTime;          // 下单用户的注册时间

    private Long userId;                    // 市场人员ID
    private String userName;                // 市场人员姓名

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("oid", this.orderId),
                newCacheKey(new String[]{"aid", "uid"}, new Object[]{this.activityId, this.userId})
        };
    }
}
