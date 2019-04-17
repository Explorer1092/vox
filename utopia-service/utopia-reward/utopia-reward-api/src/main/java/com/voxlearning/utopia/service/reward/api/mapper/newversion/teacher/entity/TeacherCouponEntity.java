package com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class TeacherCouponEntity implements Serializable{
    public TeacherCouponEntity (Double discount, String couponUserRefId) {
        this.discount = new BigDecimal(discount/10).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        this.couponUserRefId = couponUserRefId;
    }
    private Double discount;
    private String couponUserRefId;
}
