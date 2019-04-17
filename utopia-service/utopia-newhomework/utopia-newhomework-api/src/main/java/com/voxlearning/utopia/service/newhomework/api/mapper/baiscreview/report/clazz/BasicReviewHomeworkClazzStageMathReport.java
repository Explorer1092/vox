package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.clazz;


import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * 数学报告
 */
@Getter
@Setter
public class BasicReviewHomeworkClazzStageMathReport implements Serializable {
    private static final long serialVersionUID = -6911286887953474084L;
    private Integer stageId;        // 关卡id
    private String stageName;       // 关卡名
    private String homeworkId;      // 关联的作业id
    private Subject subject;
    private String subjectName;
    private StudentPart studentPart;
    private String clazzName;
    private Long clazzId;
    private boolean share;

    @Getter
    @Setter
    public static class StudentPersonalAnalysis implements Serializable {
        private static final long serialVersionUID = -2113150457793365643L;
        private Long userId;
        private String userName;
        //未完成的学生错题数默认是-1，为了排序
        private long wrongNum = -1;
        private List<String> details = new LinkedList<>(); //本单元计算专练-错题数
    }


    @Getter
    @Setter
    public static class StudentPart implements Serializable {
        private static final long serialVersionUID = -44125506885469076L;
        private List<String> wrongMostUserName = new LinkedList<>();
        private List<String> tabList = Collections.singletonList("本单元计算专练-错题数");
        private List<StudentPersonalAnalysis> studentPersonalAnalysisList = new LinkedList<>();
    }

}
