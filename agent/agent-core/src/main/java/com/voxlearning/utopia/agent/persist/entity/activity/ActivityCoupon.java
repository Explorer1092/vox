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
 * 保存活动优惠券领取情况
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_activity_coupon")
public class ActivityCoupon implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private String activityId;              // 对应的活动ID

    private String couponId;                // 优惠券ID
    private String couponName;              // 优惠券名称
    private Long couponUserId;              // 领取优惠券的UserId
    private Date couponUserRegTime;          // 领券用户的注册时间
    private Date couponTime;                // 优惠券领取时间

    private String orderId;                 // 消费后对应的订单ID


    private Long userId;                    // 市场人员ID
    private String userName;                // 市场人员姓名

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"cid", "cuid"}, new Object[]{this.couponId, this.couponUserId}),
                newCacheKey(new String[]{"aid", "uid"}, new Object[]{this.activityId, this.userId})
        };
    }
}
