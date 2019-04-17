package com.voxlearning.utopia.agent.view.grade;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.agent.persist.entity.tag.AgentTag;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2018/1/31
 */
@Getter
@Setter
public class ClassTeacher17InfoView {
    private Long groupId;
    private Long teacherId;
    private String teacherName;
    private Subject subject;
    private String subjectName;
    private Boolean isAuth;
    private boolean vacnHwFlag;
    private int tmHwSc;
    private int lmHwSc;
    private int finCsHwGte3AuStuCount;
    private int finCsHwEq1AuStuCount;
    private int finCsHwEq2AuStuCount;

    private int vacnHwGroupCount;       //布置假期作业的班组数
    private int termReviewGroupCount;   //布置期末作业的班组数

    private List<AgentTag> tagList;

}
