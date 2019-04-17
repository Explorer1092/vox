package com.voxlearning.utopia.agent.view.activity.palace;

import lombok.Data;

import java.util.Date;

@Data
public class PalaceRecordDataView {

    private Long userId;                    // 领取优惠券的UserId
    private String userName;
    private Date couponTime;                // 优惠券领取时间
    private Boolean isNewUser;              //

    private String mobile;                  // 手机号
    private Long studentId;
    private String studentName;
    private Long schoolId;
    private String schoolName;

    private Boolean hasOrder;               // 是否已下单
    private Boolean attendClass;            // 是否已上课

    private Date businessTime;              // 业务时间
}
