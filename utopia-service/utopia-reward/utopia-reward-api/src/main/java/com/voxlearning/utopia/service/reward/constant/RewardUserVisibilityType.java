package com.voxlearning.utopia.service.reward.constant;

import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.Getter;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2018-11-02 14:41
 **/
public enum RewardUserVisibilityType {
    PRIMARY_TEACHER(1, "小学老师"),
    PRIMARY_STUDENT(2, "小学学生"),
    JUNIOR_TEACHER(4, "中学老师"),
    JUNIOR_STUDENT(8, "中学学生"),
    ;

    @Getter
    private Integer type;
    @Getter
    private String name;
    RewardUserVisibilityType(Integer type, String name) {
        this.name = name;
        this.type = type;
    }

    public Integer intType() {
        return type;
    }

    public static Boolean isPrimaryTeacherFlag(Integer type) {
        return (type & RewardUserVisibilityType.PRIMARY_TEACHER.intType()) > 0;
    }

    public static Boolean isPrimaryStudentFlag(Integer type) {
        return (type & RewardUserVisibilityType.PRIMARY_STUDENT.intType()) > 0;
    }

    public static Boolean isJuniorTeacherFlag(Integer type) {
        return (type & RewardUserVisibilityType.JUNIOR_TEACHER.intType()) > 0;
    }

    public static Boolean isJuniorStudentFlag(Integer type) {
        return (type & RewardUserVisibilityType.JUNIOR_STUDENT.intType()) > 0;
    }

    public static Boolean isVisible(Integer type, Integer visibleValue) {
        return (type & visibleValue) > 0;
    }

    public static Integer getVisibleType(User user) {
        if (user.isStudent()) {
            StudentDetail studentDetail = (StudentDetail) user;
            if (studentDetail.isPrimaryStudent()) {
                return RewardUserVisibilityType.PRIMARY_STUDENT.intType();
            } else {
                return RewardUserVisibilityType.JUNIOR_STUDENT.intType();
            }
        } else if (user.isTeacher()) {
            TeacherDetail teacherDetail = (TeacherDetail) user;
            if (teacherDetail.isPrimarySchool()) {
                return RewardUserVisibilityType.PRIMARY_TEACHER.intType();
            } else {
                return RewardUserVisibilityType.JUNIOR_TEACHER.intType();
            }
        } else {
            return 0;
        }
    }
}
