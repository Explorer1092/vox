package com.voxlearning.utopia.agent.bean.resource;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * TeacherListCard
 *
 * @author song.wang
 * @date 2017/4/6
 */
@Getter
@Setter
@NoArgsConstructor
public class TeacherListCard implements Serializable {

    // 老师账号信息
    private Long teacherId;              // 老师ID
    private String realName;             // 老师姓名
    private String mobile;               // 老师手机号码
    // 是否是快乐学老师给前端模板使用的字段
    @Deprecated private Boolean isKLXTeacher;        //是否是快乐学老师  该字段已不适用，仅能区分是否是初高中老师， 可用 schoolLevel替代 20180316 by 王松

    // 学校信息
    private Long schoolId;               // 所在学校ID
    private String schoolName;           // 所在学校名
    private String schoolShortName;      // 所在学校名简称
    private SchoolLevel schoolLevel;         // 所在学校级别 JUNIOR.小学 MIDDLE.中学 null 不存在


    // 老师的标签
    private Integer authState;             // 认证状态
    private Boolean isRecentRegister;      // 是否是新注册老师
    private List<String> tagList;          // 市场定义的标签列表
    private Set<Subject> subjects;          // 老师科目列表
    private Boolean isAmbassador;           // 是否校园大使
    private Boolean isSchoolQuizBankAdmin;  // 是否是校本题库管理员
    private Boolean subjectLeaderFlag;      // 是否是学科组长
    private Boolean isFakeTeacher;          // 是否是假老师

    // 17模式下业绩相关
    private Integer regStuCount;        // 注册学生数
    private Integer authStudentCount;   // 认证学生总数
    private Integer tmAuStuCsMauc;      // 当前科目月活
    private Integer lmAuStuCsMauc;      // 上月科目月活
    private boolean oftenAssignHw;      // 频繁布置作业
    private Integer tmHwSc;             // 本月布置所有作业套数
    private Integer tmGroupMaxHwSc;    //本月布置所有作业班组最大套数
    private Integer tmGroupMinHwSc;    //本月布置所有作业班组最小套数


    // 快乐学模式下业绩相关
    @Deprecated private Integer stuKlxTnCount;       // 学生快乐学考号数
    @Deprecated private Integer tmCsAnshGte2StuCount;// 当月答题卡作答2次及以上该科目试卷学生使用数（数扫）
    @Deprecated private Integer lmCsAnshGte2StuCount;// 上月答题卡作答2次及以上该科目试卷学生使用数（数扫）

    private Integer tmScanTpCount;       // 本月扫描试卷数
    private Integer lmScanTpCount;       // 上月扫描试卷数
    private Integer tmFinCsTpGte1StuCount;  // 普通扫描（ >= 1）

    // 共有字段
    private Integer clazzCount;         // 老师所带班级数

    // 隐藏相关
    private Boolean isHidden;           // 是否是隐藏的


    // 此字段不定时更新，是否布置暑假作业。
    private Boolean isAssignSHW = false;
    private Integer vacationHwGroupCount;  // 老师布置假期作业的班组数

    // 功能性的排序字段
    private Integer sorted1 = 0;
    private Integer sorted2 = 0;

    //private Boolean isEnglish;           // 是否英语老师
    //private Boolean isChinese;           // 是否语文老师
    //private Boolean isMath;              // 是否数学老师

//    public boolean isKlxTeacher() {
//        return schoolLevel == SchoolLevel.HIGH || schoolLevel == SchoolLevel.MIDDLE;
//    }
}
