package com.voxlearning.utopia.agent.mockexam.service.dto.input;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 考试计划查询条件
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Data
public class ExamPlanQueryParams implements Serializable {
    private Long planId;                // 测评ID
    private String name;                // 测评名称
    private String type;                // 测评级别
    private String form;                // 测评形式
    private String grade;               // 测评年级
    private String subject;             // 测评学科
    private String creatorId;           // 申请人
    private String creatorName;         // 创建人姓名
    private String paperId;             // 试卷ID
    private String status;              // 测评状态
    private Date createStartTime;       // 起始创建申请时间
    private Date createEndTime;         // 结束创建申请时间
    private Date planStartTime;         // 测评开始大于时间
    private Date planEndTime;           // 测评开始小于时间
    private Date deadlineStartTime;     // 测评截止大于时间
    private Date deadlineEndTime;       // 测评截止小于时间
    private Boolean withCreator = true;               // 是否查询指定创建者的测评, 默认true
    private Long currentUserId;       // 当前用户id
    private List<Integer> currentUserRole;    // 当前用户角色
}
