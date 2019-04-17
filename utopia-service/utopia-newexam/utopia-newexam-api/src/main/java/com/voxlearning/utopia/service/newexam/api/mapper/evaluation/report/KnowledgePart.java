package com.voxlearning.utopia.service.newexam.api.mapper.evaluation.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class KnowledgePart implements Serializable {
    private static final long serialVersionUID = 8794935942479460022L;
    //班级平均成绩
    private int clazzAvgScore;
    //市区平均成绩，大数据不存在数据的情况，默认给0
    private int cityAvgScore;
    //市区前10平均成绩，
    private int cityTopTenAvgScore;

    //知识点列表
    private List<KnowledgePoint> knowledgePoints = new LinkedList<>();


    //学生列表
    private List<StudentReportRecord> studentReportRecords = new LinkedList<>();


    @Getter
    @Setter
    public static class KnowledgePoint implements Serializable {
        private static final long serialVersionUID = -1591639894042666287L;

        private String kid;
        private String kName;

        private String tName;

        //班级知识点正确率
        private int clazzRightRate;

        //市知识点正确率，可以为null，表示不存在
        private int cityRightRate;
    }

    @Getter
    @Setter
    public static class StudentReportRecord implements Serializable {
        private static final long serialVersionUID = -8357779544035300324L;
        private Long sid;
        private String sName;
        private int score;
        private boolean finished;
        private boolean begin;
        private String scoreLevel;
        private String duration = "--";
        //未完成的显示--
        private String finishTime = "--";
    }


    @Getter
    @Setter
    public static class KnowledgePointBO implements Serializable {


        private static final long serialVersionUID = -7810373593248937970L;
        private String kid;
        private String kName;
        private String tName;
        private int totalNum;
        private int rightNum;
    }

    @Getter
    @Setter
    public static class StudentReportRecordBO implements Serializable {
        private static final long serialVersionUID = -2032841154026952837L;
        private Long sid;
        private String sName;
        private boolean begin;
        private int score;
        private long duration = Long.MAX_VALUE;
        private boolean finished;
        //未完成的显示--
        private Date finishAt;
    }


}
