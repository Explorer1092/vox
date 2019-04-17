package com.voxlearning.utopia.agent.view.grade;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.agent.persist.entity.tag.AgentTag;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * ClassTeacherKlxInfoView
 *
 * @author song.wang
 * @date 2018/3/15
 */
@Getter
@Setter
public class ClassTeacherKlxInfoView {
    private Long groupId;
    private Long teacherId;
    private String teacherName;
    private Subject subject;
    private Boolean isAuth;

    private int tmScanTpCount;                                             // 本月扫描试卷数
    private int lmScanTpCount;                                             // 上月扫描试卷数
    private int tmFinCsTpGte1StuCount;                                             // 本月作答1次及以上当前科目试卷学生数
    private int lmFinCsTpGte1StuCount;                                             // 上月作答1次及以上当前科目试卷学生数

    private int tmGte2Num;  //本月周测≥2套
    private int lmGte2Num;  //上月周测≥2套

    private int tmGte1Num;  //本月周测≥1套
    private int lmGte1Num;  //上月周测≥1套

    private List<AgentTag> tagList;
}
