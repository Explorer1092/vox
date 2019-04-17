package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class LiveEnrollmentOrderView {
    private String orderId;
    private String deliveryId;

    private Long parentId;
    private String parentName;
    private String phoneNo;

    private Long studentId;
    private String studentName;

    private Long schoolId;
    private String schoolName;

    private Date orderTime;

    private Integer orderStatus;

    private List<Map<String, Object>> partners;

    private Integer giftReceivedStatus;  //礼物领取状态（1: 已领取 2: 未领取）
    private String receiptAddress;

    private Boolean nonCalculateTime;

    private Integer courseType;  //课程类型（1: 低价课 2: 正价课）

    private Long payPrice;   //支付金额（单位分）

    private List<Integer> courseGrades;   //  1-9 分别表示 小学1-6年级及 初中 1-3 年级
    private String courseSubject;        // 英语、语文、数学、物理、化学、生物
    private String courseName;           // 课程名称
    private String courseStage;          // 小学、初中、高中

}
