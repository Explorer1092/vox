package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.clazz;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @Description: 语文报告
 * @author: Mr_VanGogh
 * @date: 2018/11/12 下午7:46
 */
@Getter
@Setter
public class BasicReviewHomeworkClazzStageChineseReport implements Serializable{

    private static final long serialVersionUID = -5367362229025249510L;

    private Integer stageId;        // 关卡id
    private String stageName;       // 关卡名
    private String homeworkId;      // 关联的作业id
    private Subject subject;
    private String subjectName;
    private BasicReviewHomeworkClazzStageChineseReport.StudentPart studentPart;
    private String clazzName;
    private Long clazzId;
    private boolean share;

    @Getter
    @Setter
    public static class StudentPart implements Serializable {
        private static final long serialVersionUID = -44125506885469076L;
        private List<String> wrongMostUserName = new LinkedList<>();
        private List<String> tabList = Collections.singletonList("达标篇章");
        private List<BasicReviewHomeworkClazzStageChineseReport.StudentPersonalAnalysis> studentPersonalAnalysisList = new LinkedList<>();
    }

    @Getter
    @Setter
    public static class StudentPersonalAnalysis implements Serializable {
        private static final long serialVersionUID = -2113150457793365643L;
        private Long userId;
        private String userName;
        //未完成的学生达标篇数默认是-1，为了排序
        private long graspNum = -1;
        private List<String> details = new LinkedList<>();
    }
}
