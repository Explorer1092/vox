package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.service.crm.api.entities.agent.CrmTeacherVisitInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/7/8.
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolRecordInfo implements Serializable {
    private static final long serialVersionUID = 7143490263412050030L;

    private String schoolRecordId;  //进校记录Id
    private Date workTime;          //进校时间
    private Long workerId;          //进校人ID
    private String workerName;      //拜访人
    private Long partnerId;         //陪访人
    private String partnerName;     //陪访人姓名
    private String workTitle;       //进校主题
    private Integer instructorCount;//关键人数量
    private String followingPlan;   //待办事项

    //选呢下属拜访记录属性
    private String workType;        //工作记录类型
    private String visitInfo;     //备注
    private Date followingTime;     //计划下次时间
    private List<CrmTeacherVisitInfo> visitTeacherList; //拜访记录详情
    private Long schoolId;
    private String schoolName;

    private Integer recordType;     //新或旧的记录 1.为新 2.为旧 3.不需要跳转
    private String workContent;     //工作内容
}
