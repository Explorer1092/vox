package com.voxlearning.utopia.agent.persist.entity.activity.palace;

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

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_palace_activity_record")
public class PalaceActivityRecord implements CacheDimensionDocument {
    @DocumentId
    private String id;

    private String activityId;              // 对应的活动ID

    private String couponId;                // 优惠券ID
    private String couponName;              // 优惠券名称
    private Long couponUserId;              // 领取优惠券的UserId
    private Date couponTime;                // 优惠券领取时间
    private Boolean isNewUser;              // 领取优惠券的用户是否是新用户(领取优惠券前30天内注册的用户)

    private String orderId;                 // 订单ID
    private Date orderPayTime;              // 订单支付时间
    private BigDecimal orderAmount;         // 订单金额
    private BigDecimal orderPayAmount;      // 订单支付金额
    private Long orderUserId;               // 下单的用户ID

    private Long studentId;                 // 学生ID
    private String studentName;             // 学生姓名
    private Long schoolId;                  // 学校ID
    private String schoolName;              // 学校名称

//    private Boolean attendClass;            // 是否已上课


    private Integer attendClassLatestDay;       // 最近一次参加课程日期

    private Integer attendClassDayCount;       // 参加课程天数

    private Integer attendClassCourseCount;    // 参加课程的累计节数(参加一次课程算一次，不去重)

    private Date businessTime;              // 业务时间

    private Long userId;                    // 市场人员ID
    private String userName;                // 市场人员姓名

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }
}
