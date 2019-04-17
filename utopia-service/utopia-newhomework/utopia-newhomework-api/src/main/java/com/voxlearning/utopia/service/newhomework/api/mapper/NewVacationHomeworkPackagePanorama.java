package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

//整份假期作业整个班的信息
@Setter
@Getter
public class NewVacationHomeworkPackagePanorama implements Serializable {
    private static final long serialVersionUID = -3287253435708462807L;

    private Integer totalStudentNum;            //一共学生人数

    private Integer finishedStudentNum;         //完成人数

    private Integer beginVacationHomeworkNum;  //开始假期作业人数

    private String packageId;                  //整份假期作业包ID

    private Date startTime;

    private boolean ableToDelete;

    private boolean jztTodayHasShare;

    private boolean weiXinTodayHasShare;

    private boolean remindStudent;

    private boolean ableToShare;

    private Subject subject;

    private String subjectName;

    private Date endTime;

    private String clazzName;                 //班级名称

    private List<Integer> channels;         //分享渠道

    private List<NewVacationHomeworkStudentPanorama> vacationHomeworkStudentPanoramas = new LinkedList<>();  //整份假期作业单个学生详情的汇总
}
