package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaffDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;

import java.util.LinkedHashMap;

/**
 * Created by XiaoPeng.Yang on 14-7-30.
 */
public class CouponContext extends LinkedHashMap<String, Object> {
    private static final long serialVersionUID = -3428369612066749795L;

    private CouponContext(CouponProductionName couponProductionName) {
        put("couponProductionName", couponProductionName);
    }

    public static CouponContext of(CouponProductionName couponProductionName) {
        return new CouponContext(couponProductionName);
    }

    public CouponContext with(String name, Object value) {
        Validate.notBlank(name);
        put(name, value);
        return this;
    }

    public CouponProductionName getCouponProductionName() {
        return (CouponProductionName) get("couponProductionName");
    }

    public CouponContext withUserId(Long userId) {
        with("userId", userId);
        return this;
    }

    public Long getUserId() {
        return (Long) get("userId");
    }

    public CouponContext withCouponDetailId(Long couponDetailId) {
        with("couponDetailId", couponDetailId);
        return this;
    }

    public Long getCouponDetailId() {
        return (Long) get("couponDetailId");
    }

    public CouponContext withStudentDetail(StudentDetail studentDetail) {
        with("studentDetail", studentDetail);
        return this;
    }

    public CouponContext withTeacherDetail(TeacherDetail teacherDetail) {
        with("teacherDetail", teacherDetail);
        return this;
    }

    public CouponContext withResearchStaffDetail(ResearchStaffDetail researchStaffDetail) {
        with("researchStaffDetail", researchStaffDetail);
        return this;
    }

    public StudentDetail getStudentDetail() {
        return (StudentDetail) get("studentDetail");
    }

    public TeacherDetail getTeacherDetail() {
        return (TeacherDetail) get("teacherDetail");
    }

    public ResearchStaffDetail getRstaffDetail() {
        return (ResearchStaffDetail) get("researchStaffDetail");
    }

    public CouponContext withMobile(String mobile) {
        with("mobile", mobile);
        return this;
    }

    public String getMobile() {
        return (String) get("mobile");
    }
}
