package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

//整份假期作业每个包每个学生的信息
@Setter
@Getter
public class NewVacationHomeworkStudentPanorama implements Serializable {
    private static final long serialVersionUID = -3568906047779134078L;

    private String studentName;     //学生名称

    private Long studentId;       //学生ID

    private Integer finishedHomeworkNum; //学生完成包的数量

    private Integer totalHomeworkNum; // 假期作业包含的全部包数量

    private Boolean beginPackage;//是否开始假期作业

    private Integer beginHomeworkNum; //开始的作业的数目

    private String avatarUrl;   //头像地址

    private Date endTime; //最后一份的完成时间

    private Integer avgScore;//平均分
}
