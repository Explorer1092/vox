package com.voxlearning.washington.mapper.studentheadline;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author xinxin
 * @since 16/8/2016
 */
@Getter
@Setter
public class StudentAchievementHeadlineMapper extends StudentHeadlineMapper implements Serializable {
    private static final long serialVersionUID = -1151885268189617520L;

    private List<Map<String, Object>> userInfos;  // 学生信息
    private String achievementType;               // 成就类型
    private String achievementTitle;              // 成就名称
    private Integer level;                        // 成就等级

}
