package com.voxlearning.washington.mapper.studentheadline;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xinxin
 * @since 16/8/2016
 */
@Getter
@Setter
public class StudentClazzAchievementHeadlineMapper extends StudentHeadlineMapper {
    private static final long serialVersionUID = -7607025934384212723L;

    private String userImg;            // 学生头像
    private String headWearImg;        // 学生头饰

    // ============ 以上是过一段时间后可以清除的字段 2017-11-14 ==================

    private Long userId;               // 学生ID
    private String userName;           // 学生姓名
    private String avatar;             // 学生头像
    private String headWear;           // 学生头饰
    private String achievementTitle;   // 成就名称
    private String achievementType;    // 成就类型
    private Integer level;             // 成就等级
}
