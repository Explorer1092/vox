package com.voxlearning.utopia.agent.bean.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 老师资源卡片
 * Created by Yuechen.Wang on 2016/7/11.
 */
@Getter
@Setter
@NoArgsConstructor
public class TeacherResourceCard implements Serializable {

    // 老师账号信息
    private Long teacherId;              // 老师ID
    private String realName;             // 老师姓名
    private String mobile;               // 老师手机号码
    private Boolean isKLXTeacher;        //是否是快乐学老师
    private List<Long> subAccountList;   // 子账号列表
    private Date registerTime;           // 注册时间
    private String registerTimeStr;      // 注册时间
    private Integer authState;           // 认证状态
    private Date authTime;               // 认证时间
    private String authTimeStr;          // 认证时间
    private Boolean auth1Achieved;       // 老师认证条件1, 绑定手机是否达成
    private Boolean auth2Achieved;       // 老师认证条件2, 3名同学绑定手机是否达成
    private Boolean auth3Achieved;       // 老师认证条件3,   8人3次作业是否达成
    private Boolean isSchoolQuizBankAdmin;  // 是否是校本题库管理员
    private Boolean subjectLeaderFlag;   // 是否是学科组长
    // 老师教学信息
    private Long schoolId;               // 所在学校ID
    private String schoolName;           // 所在学校名
    private String schoolShortName;      // 所在学校名简称
    private String schoolLevel;         // 所在学校级别 JUNIOR.小学 MIDDLE.中学 null 不存在
    //private Boolean isEnglish;           // 是否英语老师
    //private Boolean isChinese;           // 是否语文老师
    //private Boolean isMath;              // 是否数学老师
    private Set<Subject> subjects;       // 老师学科列表
    private Boolean isAmbassador;        // 是否校园大使
    private Date latestAssignHomeworkDate;     // 最近一次布置作业日期
    private String latestAssignHomeworkDateStr;// 最近一次布置作业日期

    private Date latestScanTpDate;               // 最后一次扫描试卷日期
    private String latestScanTpDateStr;               // 最后一次扫描试卷日期

    private Boolean isRecentRegister;          // 15天内新注册老师
    //private Integer totalGroupCount;           // 老师名下所有班组
    //private Integer hwAssignedGroupCount;      // 布置作业班组总数
    // 业绩相关
    //private Integer potentialSasc;      // 单活可挖数量
    //private Integer potentialDasc;      // 双活可挖数量
    //private Integer yesterdaySasc;      // 昨日单活日浮
    //private Integer yesterdayDasc;      // 昨日双活日浮


    // 17模式下业绩相关
    private Integer regStuCount;       // 注册学生数
    private Integer authStudentCount;                 // 认证学生总数
    private Integer tmAuStuCsMauc;     // 当前科目月活
    //private Integer lmAuStuCsMauc;// 上月月活

    private Integer finCsHwGte3AuStuCount; //认证学生当月完成3套及以上当前科目作业学生数  >=3 认证学生当前科目月活数

    // 快乐学模式下业绩相关
    private Integer stuKlxTnCount;//学生快乐学考号数
    private Integer tmScanTpCount;//老师当月扫描试卷数
    private Integer tmFinCsTpGte1StuCount;   // 普通扫描 ( >= 1)
    private Integer tmFinCsTpGte3StuCount;   // 普通扫描 ( >= 3)
    //private Integer tmCsAnshGte2StuCount;//当月答题卡作答2次及以上该科目试卷学生使用数（数扫）
    //private Integer lmCsAnshGte2StuCount; // 上月扫描学生数

    private Boolean monthVisited;       // 本月是否访问
    // 其他信息
    //private Integer recordCnt;          // 关联记录数

    // 隐藏相关
    private Boolean isHideAble;         //可以隐藏
    private Boolean isShowAble;         //可以显示

    //假老师说明
    private Boolean isFakeTeacher;        // 是否是假老师

    private Date fakeTime;              //判假时间

    private String fakeCreatorName;     //判假操作人员姓名

    private String fakeDesc;            //判假说明

    private Boolean haveWaitingReviewFakeRecord;//是否有等待判假的申请

    private int finLowCsAnshEq1StuCount; //低标=1 (当前科目)
    private int finLowCsAnshGte2StuCount; //低标≥2 (当前科目)
    private int finHighCsAnshEq1StuCount; //高标=1 (当前科目)
    private int finHighCsAnshGte2StuCount; //高标≥2 (当前科目)

    // 该方法在前端有用
    @JsonIgnore
    public boolean isAuthedTeacher() {
        return SafeConverter.toBoolean(auth1Achieved) && SafeConverter.toBoolean(auth2Achieved) && SafeConverter.toBoolean(auth3Achieved);
        //&& SafeConverter.toBoolean(auth4Achieved) && SafeConverter.toBoolean(auth5Achieved);
    }

    // 该方法在前端有用
    @JsonIgnore
    public Integer authedCount() {
        int i = 0;
        if (SafeConverter.toBoolean(auth1Achieved)) {
            i++;
        }
        if (SafeConverter.toBoolean(auth2Achieved)) {
            i++;
        }
        if (SafeConverter.toBoolean(auth3Achieved)) {
            i++;
        }
        return i;
    }

}
