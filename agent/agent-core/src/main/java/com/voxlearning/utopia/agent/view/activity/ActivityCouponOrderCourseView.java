package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

import java.util.Date;

@Data
public class ActivityCouponOrderCourseView {

    private Long userId;              // 领取优惠券的UserId
    private String userName;          // 领取优惠券的userName

    private Boolean isNewUser;              //

    private String courseName;             // 课程名称

    private String mobile;                  // 手机号
    private Long studentId;
    private String studentName;
    private Long schoolId;
    private String schoolName;

    private String orderId;

    private Boolean hasOrder;               // 是否已下单

    private Boolean attendClass;            // 是否已上课

    private Boolean hasGift;                // 是否可以赠送礼品
    private Boolean giftReceived;           // 礼品是否已领取

    private Date businessTime;              // 业务时间


}
