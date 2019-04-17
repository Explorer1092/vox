package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

//周报告班级的信息
@Setter
@Getter
public class WeekReportForClazz implements Serializable {
    private static final long serialVersionUID = -5785253220946696497L;

    private String groupIdToReportId;

    private String startTime;  //开始时间

    private String endTime;    //结束时间

    private Subject subject;

    private String subjectName;

    private int checkedNum;          //查看家长人数

//    private boolean shared;          //是否分享到微信和班级

    private int homeworkNum;         //作业数目


    private List<StudentWeekReportBrief> childInfos = new LinkedList<>();

    private List<StudentWeekReportBrief> studentWeekReportBriefs = new LinkedList<>();//学生报告信息

    @Setter
    @Getter
    public static class StudentWeekReportBrief implements Serializable {
        private static final long serialVersionUID = 627224186057384734L;
        private Long sid;//

        private String studentReportId;

        private String sname;//当时家长的孩子的sname是name，不然是sid

        private int finishedNum;

        private int finishedRate;

        private int avgScore;

        private String avgScoreStr;
    }

}