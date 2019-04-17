package com.voxlearning.utopia.mapper;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Maofeng Lu
 * @serial
 * @since 2013-12-10 5:44PM
 */
@Data
public class DisplayQuizMapper implements Serializable {
    private static final long serialVersionUID = -2001535038846192517L;

    private Long quizId;                    // 测验ID
    private String quizName;                // 测验名称
    private String teacherExamPaperId;      // 试卷ID
    private Long clazzId;
    private String clazzName;
    private String startDateTime;           // 测验时间
    private String endDateTime;             // 结束时间
    private Date checkedTime;               // 检查时间
    private Date createTime;                //创建时间
    private int totalQuestionNum;           // 测验总题量
    private int doneQuestionNum;            // 完成题量
    private int doneRightQuestionNum;       // 做对题量
    private long millisecondToEnd = 0;      // 剩余多少毫秒结束
    private long millisecondToStart = 0;    // 当前时间离开始时间的毫秒
    private String subjectName;
    private Boolean finished;               //是否完成
    private String state;                   //状态
    private String homeworkType;

}
