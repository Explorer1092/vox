package com.voxlearning.utopia.service.newhomework.api.mapper;


import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

//假期作业一个包对应学生的信息
@Setter
@Getter
public class NewVacationHomeworkStudentDetail implements Serializable {
    private static final long serialVersionUID = -9196184147391706273L;
    private String homeworkId;      //作业ID

    private Integer score;         //得分

    private String scoreLevel;     //得分等级

    private Long finishTime;     //完成时间

    private String finishAt;

    private String crmFinishAt;  //crm 显示全部的时间信息

    private Long duration;         //分钟

    private boolean finish = false;        // 完成了作业

    private boolean repair = false;

    private boolean begin = false;         //有没有开始作业

    private List<ObjectiveConfigType> objectiveConfigTypes; //作业类型信息

    private List<String> objectiveConfigTypeCn; //作业类型信息

}
