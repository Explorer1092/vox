package com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher;

import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.TeacherCouponEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class TeacherCouponMapper implements Serializable{
    private Integer teacherNewLevel;
    private List<TeacherCouponEntity> teacherCouponList;
}
