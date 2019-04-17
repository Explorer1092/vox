package com.voxlearning.utopia.agent.bean.storagebox;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *  吸纳咯是认证进度表内容
 * Created by yaguang.wang on 2016/12/7.
 */
@Getter
@Setter
@NoArgsConstructor
public class TeacherAuthInfo implements Serializable {
    private static final long serialVersionUID = 5794784741794837538L;
    private Long teacherId;             // 老师ID
    private String teacherName;         // 老师姓名
    private String schoolName;         //学校名称
    private Date registerDate;        // 注册时间
    private int classCount;   //带班数量
    private int stuCount;     //学生数
    private int registerDayNum;  //注册天数
    private List<String> inviterNames; //邀请人列表
    private int last30DaysHwSc;     //
    private boolean unUsed;
    private boolean unAuthIn10Days;
    private boolean maybeFakeTeacher;
    private List<Subject> subjects;
    private String mobile;
    private Integer vacnHwGroupCount;       //布置假期作业的班组数
    private Integer termReviewGroupCount;   //布置期末作业的班组数
    private boolean studentJoin;        //学生加入 班均人数>1 （学生数 除以 班级数）
}
