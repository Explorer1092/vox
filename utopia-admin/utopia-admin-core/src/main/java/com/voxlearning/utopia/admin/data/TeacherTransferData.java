package com.voxlearning.utopia.admin.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 老师转校审核
 * Created by yaguang.wang on 2016/9/18.
 */
@Getter
@Setter
@NoArgsConstructor
public class TeacherTransferData implements Serializable {
    private static final long serialVersionUID = 4645673303799178805L;

    private String id;                      // 记录ID
    private String transferType;            // 转校类型
    private Date transferDate;              // 转校时间
    private String teacherName;             // 转校老师姓名
    private Long teacherId;                 // 转校老师ID
    private String teacherMobile;           // 转校老师手机号
    private String authenticationState;     // 老师的认证状态
    private String transferOutSchoolName;   // 转出学校姓名
    private Long transferOutSchoolId;       // 转出学校的ID
    private Boolean isEmphasisOutSchool;    // 转出学校是否是重点学校
    private String transferInSchoolName;    // 转入学校姓名
    private Long transferInSchoolId;        // 转入学校ID
    private Boolean isEmphasisInSchool;     // 转入学校是否是重点学校
    private List<String> broughtClass;      // 所带班级
    private String applicantName;           // 申请人姓名
    private String applicantMobile;         // 申请人电话
    private String taskContent;             // 申请任务内容
    private String executorName;            // 操作人
    //private String operateContent;          // 操作问题描述
    // 扩展字段
    private String otherLinkMan;            // 其它联系人
    private Boolean affirmTransferSchool;   // 转校是否正确
    private Boolean affirmTransferClass;    // 转班是否正确
    private String transferSchoolReason;    // 转校原因
    private String remark;                  // 备注
    private Boolean isProof;                // 是否已经被审核
}
