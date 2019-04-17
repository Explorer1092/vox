package com.voxlearning.utopia.service.campaign.impl.internal.filter;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 课程列表查询过滤类
 */
@Getter
@Setter
@ToString
public class CourseQueryFilter {
    private String title;
    private List<Long> grade;
    private List<Long> subject;

    public boolean isGradeNotEmpty() {
        if (grade != null && !grade.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isSubjectNotEmpty() {
        if (subject != null && subject.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        if (StringUtils.isBlank(title)
                && (grade == null || grade.isEmpty())
                && (subject == null || subject.isEmpty())) {
            return true;
        }
        return false;
    }

}
