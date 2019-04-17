package com.voxlearning.utopia.agent.view.grade;

import lombok.Getter;
import lombok.Setter;

/**
 * GradeClassKlxInfoView
 *
 * @author song.wang
 * @date 2018/3/15
 */
@Getter
@Setter
public class GradeClassKlxInfoView {
    private Long classId;                                   // 班级ID
    private String className;                               // 班级名称

    private int klxTnCount;                                             // 快乐学考号数
    private int tmFinTpGte1StuCount;                                             // 本月作答1次及以上任意科目试卷学生数

    private int tmGte2Num;  //本月周测≥2套

}
