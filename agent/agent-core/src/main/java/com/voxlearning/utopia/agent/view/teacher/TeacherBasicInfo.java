package com.voxlearning.utopia.agent.view.teacher;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.utopia.agent.persist.entity.tag.AgentTag;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TeacherBasicInfo
 *
 * @author song.wang
 * @date 2018/4/18
 */
@Getter
@Setter
public class TeacherBasicInfo {
    private Long teacherId;
    private String teacherName;
    private Long schoolId;
    private String schoolName;
    private SchoolLevel schoolLevel;
    private Integer authState;
    private Boolean isRealTeacher;
    private Boolean isNewTeacher;
    private Boolean isHidden;
    private Boolean isSchoolQuizBankAdmin;
    private Boolean isSubjectLeader;
    private Boolean isParent;               // 是否同时是家长
    private String avatarImgUrl;
    private Date createTime;
    private Date authTime;
    private String mobile;
    private Integer vacnHwGroupCount;       //布置假期作业的班组数
    private Integer termReviewGroupCount;   //布置期末作业的班组数

    // 老师包班的情况下，会有多个学科
    private List<TeacherSubject> subjects = new ArrayList<>();

    private List<AgentTag> tagList;

}
