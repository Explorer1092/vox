package com.voxlearning.washington.mapper.studentheadline;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xinxin
 * @since 16/8/2016
 */
@Getter
@Setter
public class StudentHomeworkHeadlineMapper extends StudentHeadlineMapper {
    private static final long serialVersionUID = 1964510542253087574L;

    private String subject;      // 学科
    private String subjectName;  // 学科名称
    private Integer finishCount; // 完成人数
    private Integer totalCount;  // 总人数
    private String homeworkId;   // 作业ID
    private Boolean timeLimit;   // 是否有限时口算训练
}
