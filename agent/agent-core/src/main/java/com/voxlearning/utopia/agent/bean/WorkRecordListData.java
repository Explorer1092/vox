package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.service.crm.api.constants.agent.AgentWorkRecordType;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmMeetingType;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 工作记录列表展示中需要的信息
 * @author tao.zang
 * @since 2017/3/1
 */
@Getter
@Setter
public class WorkRecordListData implements Serializable {
    private String  workRecordId;//工作记录id标识
    private AgentWorkRecordType workRecordType;//工作记录类型
    private Boolean intoSchoolMultiSubject; //进校是否是跨科进校
    private Boolean intoSchoolVisitKp;      //进校是否拜访KP
    private String workRecordRemarks;   //工作记录备注展示
    private String workRecordTime;      //工作记录日期
    private Integer visitTeacherCount;  //拜访老师数

    private Integer visitEngTeacherCount;   //拜访英语老师数
    private Integer visitMathTeacherCount;  //拜访数学老师数
    private Integer visitChiTeacherCount;   //拜访语文老师数

    private Integer newRegTeacherCount;     //新注册老师数
    private Integer newRegEngTeacherCount;  //新注册老师数（英语）
    private Integer newRegMathTeacherCount; //新注册老师数（数学）
    private Integer newRegChiTeacherCount;  //新注册老师数（语文）

    private Boolean frequentlyVisit;        //频繁拜访

    private double workload;    //工作量

    private CrmMeetingType meetingType; //会议级别
    private Integer meetingPersonCount; //会议参与人数
    private Integer meetingLength;      //会议时长  1:小于15分钟，2:15-60分钟，3:大于1个小时

    private String visitResearcherManageRegion; //拜访教研员负责区域
    private String accompanyVisitPerson;        //陪访对象

    private Integer visitSchoolType;  //进校类型   1：校级会议进校 2：拜访老师进校

}
