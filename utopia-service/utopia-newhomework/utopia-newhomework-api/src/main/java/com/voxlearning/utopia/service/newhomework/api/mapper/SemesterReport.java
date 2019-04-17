package com.voxlearning.utopia.service.newhomework.api.mapper;


import com.voxlearning.utopia.service.newhomework.api.entity.SemesterStudentReportFormData;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 学生学期错题返回格式
 */
@Setter
@Getter
public class SemesterReport implements Serializable {


    private static final long serialVersionUID = 819818213100824186L;

    private SemesterHomeworkInformation semesterHomeworkInformation;//作业成绩模块

    private Long wrongQuestionNum;//错题量

    private StudentBookInfo studentBookInfo;//教材信息

    private List<SemesterStudentReportFormData> semesterStudentReportFormData;//作业形式模块


    private List<WrongQuestionStatisticsForUnitData> wrongQuestionStatisticsForUnitDatas;//错题单元分布模块

    @Setter
    @Getter
    public static class SemesterHomeworkInformation implements Serializable {
        private static final long serialVersionUID = 2481749936238846621L;
        private Integer finishedHomeworkNum;//完成作业数
        private Integer unFinishedHomeworkNum;//未完成作业数
        private Integer avgScore; //学生学期成绩
        private Integer clazzAvgScore;//班级成绩
        private Integer clazzMaxScore;//班级最高成绩
        private String description;//评语

    }

    @Setter
    @Getter
    public static class StudentBookInfo implements Serializable {
        private static final long serialVersionUID = -4532647831517035174L;
        private String bookId;//课本ID
        private String bookName;//课本名字
        private String bookBrief;//课本知识点等简介
    }

    @Setter
    @Getter
    public static class WrongQuestionStatisticsForUnitData implements Serializable {

        private static final long serialVersionUID = 6308304676275898299L;
        private String unitId;//单元ID
        private Integer unitRank;//单元rank
        private Integer wrongQuestionNum;//错题数


    }


}
